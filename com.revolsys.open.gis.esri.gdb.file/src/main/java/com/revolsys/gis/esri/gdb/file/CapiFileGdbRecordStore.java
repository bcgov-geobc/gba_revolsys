package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.types.DataType;
import com.revolsys.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.format.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.format.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.format.esri.gdb.xml.model.DEFeatureDataset;
import com.revolsys.format.esri.gdb.xml.model.DETable;
import com.revolsys.format.esri.gdb.xml.model.DataElement;
import com.revolsys.format.esri.gdb.xml.model.Domain;
import com.revolsys.format.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.format.esri.gdb.xml.model.EsriGdbXmlSerializer;
import com.revolsys.format.esri.gdb.xml.model.EsriXmlRecordDefinitionUtil;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.format.esri.gdb.xml.model.Index;
import com.revolsys.format.esri.gdb.xml.model.SpatialReference;
import com.revolsys.format.esri.gdb.xml.model.Workspace;
import com.revolsys.format.esri.gdb.xml.model.WorkspaceDefinition;
import com.revolsys.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.AbstractMultiCondition;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.CollectionValue;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.ILike;
import com.revolsys.gis.data.query.In;
import com.revolsys.gis.data.query.LeftUnaryCondition;
import com.revolsys.gis.data.query.Like;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.data.query.RightUnaryCondition;
import com.revolsys.gis.data.query.SqlCondition;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.esri.gdb.file.capi.FileGdbDomainCodeTable;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Envelope;
import com.revolsys.gis.esri.gdb.file.capi.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.capi.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.BinaryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DateFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DoubleFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.FloatFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GeometryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GlobalIdFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GuidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.IntegerFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.OidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.ShortFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.StringFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.XmlFieldDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Path;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Writer;
import com.revolsys.io.xml.XmlProcessor;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.DateUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;

public class CapiFileGdbRecordStore extends AbstractRecordStore implements
  FileGdbRecordStore {
  private static final Object API_SYNC = new Object();

  private static final Logger LOG = LoggerFactory.getLogger(CapiFileGdbRecordStore.class);

  private static final Map<FieldType, Constructor<? extends AbstractFileGdbFieldDefinition>> ESRI_FIELD_TYPE_ATTRIBUTE_MAP = new HashMap<FieldType, Constructor<? extends AbstractFileGdbFieldDefinition>>();

  static {
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeInteger,
      IntegerFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeSmallInteger,
      ShortFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeDouble,
      DoubleFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeSingle,
      FloatFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeString,
      StringFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeDate,
      DateFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGeometry,
      GeometryFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeOID,
      OidFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeBlob,
      BinaryFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGlobalID,
      GlobalIdFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGUID,
      GuidFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeXML,
      XmlFieldDefinition.class);

  }

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  private static final String CATALOG_PATH_PROPERTY = CapiFileGdbRecordStore.class
    + ".CatalogPath";

  private static void addFieldTypeAttributeConstructor(
    final FieldType fieldType,
    final Class<? extends AbstractFileGdbFieldDefinition> attributeClass) {
    try {
      final Constructor<? extends AbstractFileGdbFieldDefinition> constructor = attributeClass.getConstructor(Field.class);
      ESRI_FIELD_TYPE_ATTRIBUTE_MAP.put(fieldType, constructor);
    } catch (final SecurityException e) {
      LOG.error("No public constructor for ESRI type " + fieldType, e);
    } catch (final NoSuchMethodException e) {
      LOG.error("No public constructor for ESRI type " + fieldType, e);
    }

  }

  public static SpatialReference getSpatialReference(
    final GeometryFactory geometryFactory) {
    if (geometryFactory == null || geometryFactory.getSRID() == 0) {
      return null;
    } else {
      final String wkt;
      synchronized (API_SYNC) {
        wkt = EsriFileGdb.getSpatialReferenceWkt(geometryFactory.getSRID());
      }
      final SpatialReference spatialReference = SpatialReference.get(
        geometryFactory, wkt);
      return spatialReference;
    }
  }

  private final Object apiSync = new Object();

  private final Map<String, AtomicLong> idGenerators = new HashMap<String, AtomicLong>();

  private Map<String, List<String>> domainColumNames = new HashMap<String, List<String>>();

  private String defaultSchemaPath = "/";

  private Geodatabase geodatabase;

  private String fileName;

  private boolean createMissingRecordStore = true;

  private boolean createMissingTables = true;

  private Resource template;

  private final Map<String, Table> tablesToClose = new HashMap<>();

  private final Map<String, Integer> lockedTables = new HashMap<>();

  private final Map<String, Integer> tableReferenceCounts = new HashMap<>();

  private final Set<EnumRows> enumRowsToClose = new HashSet<>();

  private boolean initialized;

  private boolean closed;

  private final Map<String, String> catalogPathByPath = new HashMap<>();

  protected CapiFileGdbRecordStore(final File file) {
    this.fileName = file.getAbsolutePath();
    setConnectionProperties(Collections.singletonMap("url",
      FileUtil.toUrl(file).toString()));
    this.catalogPathByPath.put("/", "\\");
  }

  public void addChildSchema(final String path) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (this.geodatabase != null) {
          final VectorOfWString childDatasets = this.geodatabase.getChildDatasets(
            path, "Feature Dataset");
          for (int i = 0; i < childDatasets.size(); i++) {
            final String childPath = childDatasets.get(i);
            addFeatureDatasetSchema(childPath);
          }
        }
      }
    }
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    super.addCodeTable(codeTable);
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (codeTable instanceof Domain) {
          final Domain domain = (Domain)codeTable;
          createDomain(domain);
        }
      }
    }
  }

  private RecordStoreSchema addFeatureDatasetSchema(final String path) {
    final String schemaName = path.replaceAll("\\\\", "/");
    final RecordStoreSchema schema = new RecordStoreSchema(this, schemaName);
    schema.setProperty(CATALOG_PATH_PROPERTY, path);
    addSchema(schema);
    addChildSchema(path);
    return schema;
  }

  private void addTableDefinition(final String schemaName, final String path) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (this.geodatabase != null) {
          final String tableDefinition = this.geodatabase.getTableDefinition(path);
          final RecordDefinition metaData = getRecordDefinition(schemaName,
            path, tableDefinition);
          addRecordDefinition(metaData);
        }
      }
    }
  }

  public void alterDomain(final CodedValueDomain domain) {
    final String domainDefinition = EsriGdbXmlSerializer.toString(domain);
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (!isClosed()) {
          getGeodatabase().alterDomain(domainDefinition);
        }
      }
    }
  }

  public void appendQueryValue(final Query query, final StringBuilder buffer,
    final QueryValue condition) {
    if (condition instanceof Like || condition instanceof ILike) {
      final BinaryCondition like = (BinaryCondition)condition;
      final QueryValue left = like.getLeft();
      final QueryValue right = like.getRight();
      buffer.append("UPPER(CAST(");
      appendQueryValue(query, buffer, left);
      buffer.append(" AS VARCHAR(4000))) LIKE ");
      if (right instanceof Value) {
        final Value valueCondition = (Value)right;
        final Object value = valueCondition.getValue();
        buffer.append("'");
        if (value != null) {
          final String string = StringConverterRegistry.toString(value);
          buffer.append(string.toUpperCase().replaceAll("'", "''"));
        }
        buffer.append("'");
      } else {
        appendQueryValue(query, buffer, right);
      }
    } else if (condition instanceof LeftUnaryCondition) {
      final LeftUnaryCondition unaryCondition = (LeftUnaryCondition)condition;
      final String operator = unaryCondition.getOperator();
      final QueryValue right = unaryCondition.getQueryValue();
      buffer.append(operator);
      buffer.append(" ");
      appendQueryValue(query, buffer, right);
    } else if (condition instanceof RightUnaryCondition) {
      final RightUnaryCondition unaryCondition = (RightUnaryCondition)condition;
      final QueryValue left = unaryCondition.getValue();
      final String operator = unaryCondition.getOperator();
      appendQueryValue(query, buffer, left);
      buffer.append(" ");
      buffer.append(operator);
    } else if (condition instanceof BinaryCondition) {
      final BinaryCondition binaryCondition = (BinaryCondition)condition;
      final QueryValue left = binaryCondition.getLeft();
      final String operator = binaryCondition.getOperator();
      final QueryValue right = binaryCondition.getRight();
      appendQueryValue(query, buffer, left);
      buffer.append(" ");
      buffer.append(operator);
      buffer.append(" ");
      appendQueryValue(query, buffer, right);
    } else if (condition instanceof AbstractMultiCondition) {
      final AbstractMultiCondition multipleCondition = (AbstractMultiCondition)condition;
      buffer.append("(");
      boolean first = true;
      final String operator = multipleCondition.getOperator();
      for (final QueryValue subCondition : multipleCondition.getQueryValues()) {
        if (first) {
          first = false;
        } else {
          buffer.append(" ");
          buffer.append(operator);
          buffer.append(" ");
        }
        appendQueryValue(query, buffer, subCondition);
      }
      buffer.append(")");
    } else if (condition instanceof In) {
      final In in = (In)condition;
      if (in.isEmpty()) {
        buffer.append("1==1");
      } else {
        final QueryValue left = in.getLeft();
        appendQueryValue(query, buffer, left);
        buffer.append(" IN (");
        appendQueryValue(query, buffer, in.getValues());
        buffer.append(")");
      }
    } else if (condition instanceof Value) {
      final Value valueCondition = (Value)condition;
      final Object value = valueCondition.getValue();
      appendValue(buffer, value);
    } else if (condition instanceof CollectionValue) {
      final CollectionValue collectionValue = (CollectionValue)condition;
      final List<Object> values = collectionValue.getValues();
      boolean first = true;
      for (final Object value : values) {
        if (first) {
          first = false;
        } else {
          buffer.append(", ");
        }
        appendValue(buffer, value);
      }
    } else if (condition instanceof Column) {
      final Column column = (Column)condition;
      final Object name = column.getName();
      buffer.append(name);
    } else if (condition instanceof SqlCondition) {
      final SqlCondition sqlCondition = (SqlCondition)condition;
      final String where = sqlCondition.getSql();
      final List<Object> parameters = sqlCondition.getParameterValues();
      if (parameters.isEmpty()) {
        if (where.indexOf('?') > -1) {
          throw new IllegalArgumentException(
            "No arguments specified for a where clause with placeholders: "
              + where);
        } else {
          buffer.append(where);
        }
      } else {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(where);
        int i = 0;
        while (matcher.find()) {
          if (i >= parameters.size()) {
            throw new IllegalArgumentException(
              "Not enough arguments for where clause with placeholders: "
                + where);
          }
          final Object argument = parameters.get(i);
          final StringBuffer replacement = new StringBuffer();
          matcher.appendReplacement(replacement,
            StringConverterRegistry.toString(argument));
          buffer.append(replacement);
          appendValue(buffer, argument);
          i++;
        }
        final StringBuffer tail = new StringBuffer();
        matcher.appendTail(tail);
        buffer.append(tail);
      }
    } else {
      condition.appendSql(buffer);
    }
  }

  public void appendValue(final StringBuilder buffer, final Object value) {
    if (value == null) {
      buffer.append("''");
    } else if (value instanceof Number) {
      buffer.append(value);
    } else if (value instanceof java.util.Date) {
      final String stringValue = DateUtil.format("yyyy-MM-dd",
        (java.util.Date)value);
      buffer.append("DATE '" + stringValue + "'");
    } else {
      final String stringValue = StringConverterRegistry.toString(value);
      buffer.append("'");
      buffer.append(stringValue.replaceAll("'", "''"));
      buffer.append("'");
    }
  }

  @Override
  @PreDestroy
  public void close() {
    if (!FileGdbRecordStoreFactory.release(this.fileName)) {
      doClose();
    }
  }

  protected void closeEnumRows() {
    synchronized (this.apiSync) {
      for (final Iterator<EnumRows> iterator = this.enumRowsToClose.iterator(); iterator.hasNext();) {
        final EnumRows rows = iterator.next();
        try {
          rows.Close();
        } catch (final Throwable e) {
        } finally {
          rows.delete();
        }
        iterator.remove();
      }
      this.enumRowsToClose.clear();
    }
  }

  public void closeEnumRows(final EnumRows rows) {
    synchronized (this.apiSync) {
      if (isOpen(rows)) {
        try {
          rows.Close();
        } catch (final Throwable e) {
        } finally {
          try {
            rows.delete();
          } catch (final Throwable t) {
          }
        }
        this.enumRowsToClose.remove(rows);
      }
    }
  }

  protected void closeRow(final Row row) {
    if (row != null) {
      synchronized (this.apiSync) {
        row.delete();
      }
    }
  }

  public boolean closeTable(final String typePath) {
    synchronized (this.apiSync) {
      int count = Maps.getInteger(this.tableReferenceCounts, typePath, 0);
      count--;
      if (count <= 0) {
        this.tableReferenceCounts.remove(typePath);
        final Table table = this.tablesToClose.remove(typePath);
        closeTable(typePath, table);
        return true;
      } else {
        this.tableReferenceCounts.put(typePath, count);
        return false;
      }
    }
  }

  private void closeTable(final String typePath, final Table table) {
    synchronized (API_SYNC) {
      if (table != null) {
        try {
          if (!isClosed()) {
            getGeodatabase().closeTable(table);
          }
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Cannot close Table " + typePath, e);
        } finally {
          this.tablesToClose.remove(typePath);
          this.tableReferenceCounts.remove(typePath);
          try {
            table.delete();
          } catch (final Throwable t) {
          }
        }
      }
      if (this.tablesToClose.isEmpty() && this.geodatabase != null) {
        try {
          EsriFileGdb.CloseGeodatabase(this.geodatabase);
        } finally {
          this.geodatabase = null;
        }
      }
    }
  }

  protected void closeTables() {
    synchronized (this.apiSync) {
      final Map<String, Table> tables = new HashMap<>(this.tablesToClose);
      for (final Entry<String, Table> entry : tables.entrySet()) {
        final String typePath = entry.getKey();
        final Table table = entry.getValue();
        closeTable(typePath, table);
      }
      this.tablesToClose.clear();
    }
  }

  public synchronized void createDomain(final Domain domain) {
    synchronized (this.apiSync) {
      if (!isClosed()) {
        final String domainName = domain.getDomainName();
        if (!this.domainColumNames.containsKey(domainName)) {
          synchronized (API_SYNC) {
            final String domainDef = EsriGdbXmlSerializer.toString(domain);
            try {
              getGeodatabase().createDomain(domainDef);
            } catch (final Exception e) {
              LOG.debug(domainDef);
              LOG.error("Unable to create domain", e);
            }
            loadDomain(domain.getDomainName());
          }
        }
      }
    }
  }

  @Override
  public AbstractIterator<Record> createIterator(final Query query,
    final Map<String, Object> properties) {
    String typePath = query.getTypeName();
    RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      typePath = query.getTypeName();
      recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        throw new IllegalArgumentException("Type name does not exist "
          + typePath);
      }
    } else {
      typePath = recordDefinition.getPath();
    }
    final BoundingBox boundingBox = query.getBoundingBox();
    final Map<String, Boolean> orderBy = query.getOrderBy();
    final StringBuilder whereClause = getWhereClause(query);
    StringBuilder sql = new StringBuilder();
    if (orderBy.isEmpty() || boundingBox != null) {
      if (!orderBy.isEmpty()) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to sort on " + recordDefinition.getPath() + " "
            + orderBy.keySet()
            + " as the ESRI library can't sort with a bounding box query");
      }
      sql = whereClause;
    } else {
      sql.append("SELECT ");

      final List<String> fieldNames = query.getFieldNames();
      if (fieldNames.isEmpty()) {
        CollectionUtil.append(sql, recordDefinition.getFieldNames());
      } else {
        CollectionUtil.append(sql, fieldNames);
      }
      sql.append(" FROM ");
      sql.append(JdbcUtils.getTableName(typePath));
      if (whereClause.length() > 0) {
        sql.append(" WHERE ");
        sql.append(whereClause);
      }
      boolean first = true;
      for (final Entry<String, Boolean> entry : orderBy.entrySet()) {
        final String column = entry.getKey();
        final DataType dataType = recordDefinition.getFieldType(column);
        if (dataType != null
          && !Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
          if (first) {
            sql.append(" ORDER BY ");
            first = false;
          } else {
            sql.append(", ");
          }
          sql.append(column);
          final Boolean ascending = entry.getValue();
          if (!ascending) {
            sql.append(" DESC");
          }

        } else {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to sort on " + recordDefinition.getPath() + "." + column
              + " as the ESRI library can't sort on " + dataType + " columns");
        }
      }
    }

    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
      typePath, sql.toString(), boundingBox, query, query.getOffset(),
      query.getLimit());
    return iterator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    synchronized (this.apiSync) {
      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        return null;
      } else {
        final String idFieldName = recordDefinition.getIdFieldName();
        if (idFieldName == null) {
          return null;
        } else if (!idFieldName.equals("OBJECTID")) {
          AtomicLong idGenerator = this.idGenerators.get(typePath);
          if (idGenerator == null) {
            long maxId = 0;
            for (final Record object : query(typePath)) {
              final Object firstId = object.getIdValue();
              if (firstId instanceof Number) {
                final Number number = (Number)firstId;
                if (number.longValue() > maxId) {
                  maxId = number.longValue();
                }
              }
            }
            idGenerator = new AtomicLong(maxId);
            this.idGenerators.put(typePath, idGenerator);
          }
          return (T)(Object)idGenerator.incrementAndGet();
        } else {
          return null;
        }
      }
    }
  }

  protected Row createRowObject(final Table table) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        return table.createRowObject();
      } else {
        return null;
      }
    }
  }

  private RecordStoreSchema createSchema(final DETable table) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase == null) {
          return null;
        } else {
          final String catalogPath = table.getParentCatalogPath();
          final List<DEFeatureDataset> datasets = EsriXmlRecordDefinitionUtil.createDEFeatureDatasets(table);
          for (final DEFeatureDataset dataset : datasets) {
            final String path = dataset.getCatalogPath();
            final String datasetDefinition = EsriGdbXmlSerializer.toString(dataset);
            try {
              geodatabase.createFeatureDataset(datasetDefinition);
              addFeatureDatasetSchema(path);
            } catch (final Throwable t) {
              if (LOG.isDebugEnabled()) {
                LOG.debug(datasetDefinition);
              }
              throw new RuntimeException("Unable to create feature dataset "
                + path, t);
            }
          }
          return getSchema(catalogPath.replaceAll("\\\\", "/"));
        }
      }
    }
  }

  public void createSchema(final String schemaName,
    final GeometryFactory geometryFactory) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          final SpatialReference spatialReference = getSpatialReference(geometryFactory);
          final List<DEFeatureDataset> datasets = EsriXmlRecordDefinitionUtil.createDEFeatureDatasets(
            schemaName.replaceAll("/", ""), spatialReference);
          for (final DEFeatureDataset dataset : datasets) {
            final String path = dataset.getCatalogPath();
            final String datasetDefinition = EsriGdbXmlSerializer.toString(dataset);
            try {
              geodatabase.createFeatureDataset(datasetDefinition);
              addFeatureDatasetSchema(path);
            } catch (final Throwable t) {
              if (LOG.isDebugEnabled()) {
                LOG.debug(datasetDefinition);
              }
              throw new RuntimeException("Unable to create feature dataset "
                + path, t);
            }
          }
        }
      }
    }
  }

  protected RecordDefinitionImpl createTable(final DETable deTable) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase == null) {
          return null;
        } else {
          String schemaPath = deTable.getParentCatalogPath();
          String schemaName = schemaPath.replaceAll("\\\\", "/");
          RecordStoreSchema schema = getSchema(schemaName);
          if (schema == null) {
            if (schemaName.length() > 1 && deTable instanceof DEFeatureClass) {
              schema = createSchema(deTable);
            } else {
              schema = new RecordStoreSchema(this, schemaName);
              addSchema(schema);
            }
          } else if (schema.getProperty(CATALOG_PATH_PROPERTY) == null) {
            if (schemaName.length() > 1 && deTable instanceof DEFeatureClass) {
              createSchema(deTable);
            }
          }
          if (schemaName.equals(this.defaultSchemaPath)) {
            if (!(deTable instanceof DEFeatureClass)) {
              schemaPath = "\\";
              // @TODO clone
              deTable.setCatalogPath("\\" + deTable.getName());

            }
          } else if (schemaName.equals("")) {
            schemaName = this.defaultSchemaPath;
          }
          for (final Field field : deTable.getFields()) {
            final String fieldName = field.getName();
            final CodeTable codeTable = getCodeTableByFieldName(fieldName);
            if (codeTable instanceof FileGdbDomainCodeTable) {
              final FileGdbDomainCodeTable domainCodeTable = (FileGdbDomainCodeTable)codeTable;
              field.setDomain(domainCodeTable.getDomain());
            }
          }
          final String tableDefinition = EsriGdbXmlSerializer.toString(deTable);
          try {
            final Table table = geodatabase.createTable(tableDefinition,
              schemaPath);
            try {
              final RecordDefinitionImpl recordDefinition = getRecordDefinition(
                schemaName, schemaPath, tableDefinition);
              addRecordDefinition(recordDefinition);
              return recordDefinition;
            } finally {
              geodatabase.closeTable(table);
            }

          } catch (final Throwable t) {
            if (LOG.isDebugEnabled()) {
              LOG.debug(tableDefinition);
            }
            throw new RuntimeException("Unable to create table "
              + deTable.getCatalogPath(), t);
          }
        }
      }
    }
  }

  private RecordDefinition createTable(final RecordDefinition objectDefinition) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final GeometryFactory geometryFactory = objectDefinition.getGeometryFactory();
        final SpatialReference spatialReference = getSpatialReference(geometryFactory);

        final DETable deTable = EsriXmlRecordDefinitionUtil.getDETable(
          objectDefinition, spatialReference);
        final RecordDefinitionImpl tableDefinition = createTable(deTable);
        final String idAttributeName = objectDefinition.getIdFieldName();
        if (idAttributeName != null) {
          tableDefinition.setIdFieldName(idAttributeName);
        }
        return tableDefinition;
      }
    }
  }

  @Override
  public FileGdbWriter createWriter() {
    return new FileGdbWriter(this);
  }

  @Override
  public void delete(final Record object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    if (object.getState() == RecordState.Persisted
      || object.getState() == RecordState.Modified) {
      object.setState(RecordState.Deleted);
      final Writer<Record> writer = getWriter();
      writer.write(object);
    }
  }

  protected void deletedRow(final String typePath, final Table table,
    final Row row) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        if (isLoadOnly(typePath)) {
          table.setLoadOnlyMode(false);
          table.deleteRow(row);
          table.setLoadOnlyMode(true);
        } else {
          table.deleteRow(row);
        }
      }
    }
  }

  @Override
  public void deleteGeodatabase() {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final String fileName = this.fileName;
        try {
          doClose();
        } finally {
          if (new File(fileName).exists()) {
            EsriFileGdb.DeleteGeodatabase(fileName);
          }
        }
      }
    }
  }

  public void doClose() {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        try {
          if (!isClosed()) {
            if (this.geodatabase != null) {
              final Writer<Record> writer = getSharedAttribute("writer");
              if (writer != null) {
                writer.close();
                setSharedAttribute("writer", null);
              }
              closeEnumRows();
              closeTables();
              try {
                if (this.geodatabase != null) {
                  EsriFileGdb.CloseGeodatabase(this.geodatabase);
                }
              } finally {
                this.geodatabase = null;
              }
            }
          }
        } finally {
          this.closed = true;
          super.close();
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }

  protected void freeWriteLock(final String typePath) {
    synchronized (this.apiSync) {
      final Table table = this.tablesToClose.get(typePath);
      if (table != null) {
        if (Maps.decrementCount(this.lockedTables, typePath) <= 0) {
          table.freeWriteLock();
          table.setLoadOnlyMode(false);
        }
      }
    }
  }

  protected String getCatalogPath(final String path) {
    final String catalogPath = this.catalogPathByPath.get(path);
    if (Property.hasValue(catalogPath)) {
      return catalogPath;
    } else {
      return path.replaceAll("/", "\\\\");
    }
  }

  private VectorOfWString getChildDatasets(final Geodatabase geodatabase,
    final String catalogPath, final String datasetType) {
    try {
      return geodatabase.getChildDatasets(catalogPath, datasetType);
    } catch (final RuntimeException e) {
      if ("-2147211775\tThe item was not found.".equals(e.getMessage())) {
        return null;
      } else {
        throw e;
      }
    }
  }

  public String getDefaultSchema() {
    return this.defaultSchemaPath;
  }

  public Map<String, List<String>> getDomainColumNames() {
    return this.domainColumNames;
  }

  public String getFileName() {
    return this.fileName;
  }

  private Geodatabase getGeodatabase() {
    if (isClosed()) {
      return null;
    } else {
      synchronized (this.apiSync) {
        if (this.geodatabase == null) {
          synchronized (API_SYNC) {
            this.geodatabase = EsriFileGdb.openGeodatabase(this.fileName);
          }
        }
        return this.geodatabase;
      }
    }
  }

  @Override
  public RecordDefinition getRecordDefinition(
    final RecordDefinition objectMetaData) {
    synchronized (this.apiSync) {
      RecordDefinition metaData = super.getRecordDefinition(objectMetaData);
      if (this.createMissingTables && metaData == null) {
        metaData = createTable(objectMetaData);
      }
      return metaData;
    }
  }

  public RecordDefinitionImpl getRecordDefinition(final String schemaName,
    final String path, final String tableDefinition) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        try {
          final XmlProcessor parser = new EsriGdbXmlParser();
          final DETable deTable = parser.process(tableDefinition);
          final String tableName = deTable.getName();
          final String typePath = PathUtil.toPath(schemaName, tableName);
          final RecordStoreSchema schema = getSchema(schemaName);
          final RecordDefinitionImpl metaData = new RecordDefinitionImpl(this,
            schema, typePath);
          for (final Field field : deTable.getFields()) {
            final String fieldName = field.getName();
            final FieldType type = field.getType();
            final Constructor<? extends AbstractFileGdbFieldDefinition> attributeConstructor = ESRI_FIELD_TYPE_ATTRIBUTE_MAP.get(type);
            if (attributeConstructor != null) {
              try {
                final AbstractFileGdbFieldDefinition attribute = JavaBeanUtil.invokeConstructor(
                  attributeConstructor, field);
                attribute.setRecordStore(this);
                metaData.addField(attribute);
                if (attribute instanceof GlobalIdFieldDefinition) {
                  metaData.setIdFieldName(fieldName);
                }
              } catch (final Throwable e) {
                LOG.error(tableDefinition);
                throw new RuntimeException("Error creating attribute for "
                    + typePath + "." + field.getName() + " : " + field.getType(),
                    e);
              }
            } else {
              LOG.error("Unsupported field type " + fieldName + ":" + type);
            }
          }
          final String oidFieldName = deTable.getOIDFieldName();
          metaData.setProperty(
            EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME, oidFieldName);
          if (deTable instanceof DEFeatureClass) {
            final DEFeatureClass featureClass = (DEFeatureClass)deTable;
            final String shapeFieldName = featureClass.getShapeFieldName();
            metaData.setGeometryFieldName(shapeFieldName);
          }
          metaData.setProperty(CATALOG_PATH_PROPERTY, path);
          for (final Index index : deTable.getIndexes()) {
            if (index.getName().endsWith("_PK")) {
              for (final Field field : index.getFields()) {
                final String fieldName = field.getName();
                metaData.setIdFieldName(fieldName);
              }
            }
          }
          addRecordDefinitionProperties(metaData);
          if (metaData.getIdFieldIndex() == -1) {
            metaData.setIdFieldName(deTable.getOIDFieldName());
          }

          return metaData;
        } catch (final RuntimeException e) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(tableDefinition);
          }
          throw e;
        }
      }
    }
  }

  @Override
  public int getRowCount(final Query query) {
    if (query == null) {
      return 0;
    } else {
      synchronized (this.apiSync) {
        String typePath = query.getTypeName();
        RecordDefinition recordDefinition = query.getRecordDefinition();
        if (recordDefinition == null) {
          typePath = query.getTypeName();
          recordDefinition = getRecordDefinition(typePath);
          if (recordDefinition == null) {
            return 0;
          }
        } else {
          typePath = recordDefinition.getPath();
        }
        final StringBuilder whereClause = getWhereClause(query);
        final BoundingBox boundingBox = query.getBoundingBox();

        if (boundingBox == null) {
          final StringBuilder sql = new StringBuilder();
          sql.append("SELECT OBJECTID FROM ");
          sql.append(JdbcUtils.getTableName(typePath));
          if (whereClause.length() > 0) {
            sql.append(" WHERE ");
            sql.append(whereClause);
          }

          final EnumRows rows = query(sql.toString(), false);
          if (rows == null) {
            return 0;
          } else {
            try {
              int count = 0;
              for (Row row = rows.next(); row != null; row = rows.next()) {
                count++;
                row.delete();
              }
              return count;
            } finally {
              closeEnumRows(rows);
            }
          }
        } else {
          final GeometryFieldDefinition geometryField = (GeometryFieldDefinition)recordDefinition.getGeometryField();
          if (geometryField == null || boundingBox.isEmpty()) {
            return 0;
          } else {
            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT " + geometryField.getName() + " FROM ");
            sql.append(JdbcUtils.getTableName(typePath));
            if (whereClause.length() > 0) {
              sql.append(" WHERE ");
              sql.append(whereClause);
            }

            final EnumRows rows = query(sql.toString(), false);
            try {
              int count = 0;
              for (Row row = rows.next(); row != null; row = rows.next()) {
                final Geometry geometry = (Geometry)geometryField.getValue(row);
                if (geometry != null) {
                  final BoundingBox geometryBoundingBox = BoundingBox.getBoundingBox(geometry);
                  if (geometryBoundingBox.intersects(boundingBox)) {
                    count++;
                  }
                }
                row.delete();
              }
              return count;
            } finally {
              closeEnumRows(rows);
            }
          }
        }
      }
    }
  }

  protected Table getTable(final String typePath) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (isClosed() || getRecordDefinition(typePath) == null) {
          return null;
        } else {
          final String tableName = getCatalogPath(typePath);
          try {

            Table table = this.tablesToClose.get(typePath);
            if (table == null) {
              table = getGeodatabase().openTable(tableName);
              this.tablesToClose.put(typePath, table);
            }
            if (table != null) {
              int count = Maps.getInteger(this.tableReferenceCounts, typePath,
                0);
              count++;
              this.tableReferenceCounts.put(typePath, count);
            }

            try {
              throw new RuntimeException();
            } catch (final Throwable e) {
            }
            return table;
          } catch (final RuntimeException e) {
            throw new RuntimeException("Unable to open table " + typePath, e);
          }
        }
      }
    }
  }

  public Resource getTemplate() {
    return this.template;
  }

  protected StringBuilder getWhereClause(final Query query) {
    final StringBuilder whereClause = new StringBuilder();
    final Condition whereCondition = query.getWhereCondition();
    if (whereCondition != null) {
      appendQueryValue(query, whereClause, whereCondition);
    }
    return whereClause;
  }

  @Override
  public FileGdbWriter getWriter() {
    synchronized (this.apiSync) {
      FileGdbWriter writer = getSharedAttribute("writer");
      if (writer == null) {
        writer = createWriter();
        setSharedAttribute("writer", writer);
      }
      return writer;
    }
  }

  protected boolean hasCatalogPath(final String path) {
    final String catalogPath = this.catalogPathByPath.get(path);
    return catalogPath != null;
  }

  private boolean hasChildDataset(final Geodatabase geodatabase,
    final String parentCatalogPath, final String datasetType,
    final String childCatalogPath) {
    try {
      final VectorOfWString childDatasets = geodatabase.getChildDatasets(
        parentCatalogPath, datasetType);
      for (int i = 0; i < childDatasets.size(); i++) {
        final String catalogPath = childDatasets.get(i);
        if (catalogPath.equals(childCatalogPath)) {
          return true;
        }
      }
      return false;
    } catch (final RuntimeException e) {
      if ("-2147211775\tThe item was not found.".equals(e.getMessage())) {
        return false;
      } else {
        throw e;
      }
    }
  }

  @Override
  @PostConstruct
  public void initialize() {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (!this.initialized) {
          this.initialized = true;
          try {
            super.initialize();
            final File file = new File(this.fileName);
            if (file.exists()) {
              if (file.isDirectory()) {
                if (new File(this.fileName, "gdb").exists()) {
                  this.geodatabase = EsriFileGdb.openGeodatabase(this.fileName);
                } else {
                  throw new IllegalArgumentException(
                    FileUtil.getCanonicalPath(file)
                      + " is not a valid ESRI File Geodatabase");
                }
              } else {
                throw new IllegalArgumentException(
                  FileUtil.getCanonicalPath(file)
                    + " ESRI File Geodatabase must be a directory");
              }
            } else if (this.createMissingRecordStore) {
              if (this.template == null) {
                this.geodatabase = EsriFileGdb.createGeodatabase(this.fileName);
              } else if (this.template.exists()) {
                if (this.template instanceof FileSystemResource) {
                  final FileSystemResource fileResource = (FileSystemResource)this.template;
                  final File templateFile = fileResource.getFile();
                  if (templateFile.isDirectory()) {
                    try {
                      FileUtil.copy(templateFile, file);
                    } catch (final Throwable e) {
                      throw new IllegalArgumentException(
                        "Unable to copy template ESRI geodatabase "
                          + this.template, e);
                    }
                    this.geodatabase = EsriFileGdb.openGeodatabase(this.fileName);
                  }
                }
                if (this.geodatabase == null) {
                  this.geodatabase = EsriFileGdb.createGeodatabase(this.fileName);
                  final Workspace workspace = EsriGdbXmlParser.parse(this.template);
                  final WorkspaceDefinition workspaceDefinition = workspace.getWorkspaceDefinition();
                  for (final Domain domain : workspaceDefinition.getDomains()) {
                    createDomain(domain);
                  }
                  for (final DataElement dataElement : workspaceDefinition.getDatasetDefinitions()) {
                    if (dataElement instanceof DETable) {
                      final DETable table = (DETable)dataElement;
                      createTable(table);
                    }
                  }
                }
              } else {
                throw new IllegalArgumentException("Template does not exist "
                  + this.template);
              }
            } else {
              throw new IllegalArgumentException(
                "ESRI file geodatabase not found " + this.fileName);
            }
            final VectorOfWString domainNames = this.geodatabase.getDomains();
            for (int i = 0; i < domainNames.size(); i++) {
              final String domainName = domainNames.get(i);
              loadDomain(domainName);
            }
          } catch (final Throwable e) {
            this.geodatabase = null;
            this.closed = true;
            ExceptionUtil.throwUncheckedException(e);
          }
        }
      }
    }
  }

  @Override
  public void insert(final Record object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    getWriter().write(object);
  }

  protected void insertRow(final Table table, final Row row) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        table.insertRow(row);
      }
    }
  }

  public boolean isClosed() {
    return this.closed;
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  public boolean isCreateMissingTables() {
    return this.createMissingTables;
  }

  public boolean isLoadOnly(final String typePath) {
    synchronized (this.apiSync) {
      return this.lockedTables.containsKey(typePath);
    }
  }

  public boolean isNull(final Row row, final String name) {
    synchronized (this.apiSync) {
      return row.isNull(name);
    }
  }

  public boolean isOpen(final EnumRows enumRows) {
    synchronized (this.apiSync) {
      if (enumRows == null) {
        return false;
      } else {
        return this.enumRowsToClose.contains(enumRows);
      }
    }
  }

  public boolean isOpen(final Table table) {
    synchronized (this.apiSync) {
      if (table == null) {
        return false;
      } else {
        return this.tablesToClose.containsValue(table);
      }
    }
  }

  @Override
  public Record load(final String typePath, final Object... id) {
    synchronized (this.apiSync) {
      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        throw new IllegalArgumentException("Unknown type " + typePath);
      } else {
        final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
          typePath, recordDefinition.getIdFieldName() + " = " + id[0]);
        try {
          if (iterator.hasNext()) {
            return iterator.next();
          } else {
            return null;
          }
        } finally {
          iterator.close();
        }
      }
    }
  }

  protected void loadDomain(final String domainName) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (!isClosed()) {
          final String domainDef = getGeodatabase().getDomainDefinition(
            domainName);
          final Domain domain = EsriGdbXmlParser.parse(domainDef);
          if (domain instanceof CodedValueDomain) {
            final CodedValueDomain codedValueDomain = (CodedValueDomain)domain;
            final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(
              this, codedValueDomain);
            super.addCodeTable(codeTable);
            final List<String> columnNames = this.domainColumNames.get(domainName);
            if (columnNames != null) {
              for (final String columnName : columnNames) {
                addCodeTable(columnName, codeTable);
              }
            }
          }
        }
      }
    }
  }

  @Override
  protected void loadSchemaDataObjectMetaData(final RecordStoreSchema schema,
    final Map<String, RecordDefinition> metaDataMap) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final String schemaName = schema.getPath();
        if (schemaName.equals(this.defaultSchemaPath)) {
          loadSchemaRecordDefinition(metaDataMap, schemaName, "\\",
            "Feature Class");
          loadSchemaRecordDefinition(metaDataMap, schemaName, "\\", "Table");
        }
        final String path = schemaName.replaceAll("/", "\\\\");
        loadSchemaRecordDefinition(metaDataMap, schemaName, path,
          "Feature Class");
        loadSchemaRecordDefinition(metaDataMap, schemaName, path, "Table");
      }
    }
  }

  public void loadSchemaRecordDefinition(
    final Map<String, RecordDefinition> metaDataMap, final String schemaName,
    final String path, final String datasetType) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            final VectorOfWString childFeatureClasses = geodatabase.getChildDatasets(
              path, datasetType);
            for (int i = 0; i < childFeatureClasses.size(); i++) {
              final String childPath = childFeatureClasses.get(i);
              addTableDefinition(schemaName, childPath);
            }
          } catch (final RuntimeException e) {
            final String message = e.getMessage();
            if (message == null
              || !message.equals("-2147211775\tThe item was not found.")) {
              throw e;
            }
          }
        }
      }
    }
  }

  @Override
  protected void loadSchemas(final Map<String, RecordStoreSchema> schemaMap) {
    synchronized (this.apiSync) {
      addSchema(new RecordStoreSchema(this, this.defaultSchemaPath));
      addChildSchema("\\");
    }
  }

  protected Row nextRow(final EnumRows rows) {
    synchronized (this.apiSync) {
      if (isOpen(rows)) {
        return rows.next();
      } else {
        return null;
      }
    }
  }

  public EnumRows query(final String sql, final boolean recycling) {
    synchronized (this.apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase == null) {
        return null;
      } else {
        try {
          final EnumRows enumRows = geodatabase.query(sql, recycling);
          this.enumRowsToClose.add(enumRows);
          return enumRows;
        } catch (final Throwable t) {
          throw new RuntimeException("Error running sql: " + sql, t);
        }
      }
    }
  }

  public EnumRows search(final Table table, final String fields,
    final String whereClause, final boolean recycling) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        try {
          final EnumRows rows = table.search(fields, whereClause, recycling);
          this.enumRowsToClose.add(rows);
          return rows;
        } catch (final Throwable t) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to execute query " + fields + " WHERE " + whereClause, t);

        }
      }
      return null;
    }
  }

  public EnumRows search(final Table table, final String fields,
    final String whereClause, final Envelope boundingBox,
    final boolean recycling) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        try {
          final EnumRows rows = table.search(fields, whereClause, boundingBox,
            recycling);
          this.enumRowsToClose.add(rows);
          return rows;
        } catch (final Exception e) {
          LOG.error("ERROR executing query SELECT " + fields + " WHERE "
            + whereClause + " AND " + boundingBox, e);
        }
      }
      return null;
    }
  }

  @Override
  public void setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
  }

  @Override
  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  @Override
  public void setDefaultSchema(final String defaultSchema) {
    synchronized (this.apiSync) {
      if (Property.hasValue(defaultSchema)) {
        if (!defaultSchema.startsWith("/")) {
          this.defaultSchemaPath = "/" + defaultSchema;
        } else {
          this.defaultSchemaPath = defaultSchema;
        }
      } else {
        this.defaultSchemaPath = "/";
      }
      refreshSchema();
    }
  }

  public void setDomainColumNames(
    final Map<String, List<String>> domainColumNames) {
    this.domainColumNames = domainColumNames;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public void setNull(final Row row, final String name) {
    synchronized (this.apiSync) {
      row.setNull(name);
    }
  }

  public void setTemplate(final Resource template) {
    this.template = template;
  }

  protected void setWriteLock(final String typePath) {
    synchronized (this.apiSync) {
      final Table table = this.tablesToClose.get(typePath);
      if (table != null) {
        table.setLoadOnlyMode(true);
        Maps.addCount(this.lockedTables, typePath);
        table.setWriteLock();
      }
    }
  }

  protected String toPath(final String catalogPath) {
    return Path.clean(catalogPath);
  }

  @Override
  public String toString() {
    return this.fileName;
  }

  @Override
  public void update(final Record object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    getWriter().write(object);
  }

  protected void updateRow(final String typePath, final Table table,
    final Row row) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        if (isLoadOnly(typePath)) {
          table.setLoadOnlyMode(false);
          table.updateRow(row);
          table.setLoadOnlyMode(true);
        } else {
          table.deleteRow(row);
        }
      }
    }
  }
}
