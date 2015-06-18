package com.revolsys.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;
import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.io.Path;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class EsriXmlRecordDefinitionUtil implements EsriGeodatabaseXmlConstants {
  private static final String DE_TABLE_PROPERTY = EsriXmlRecordDefinitionUtil.class + ".DETable";

  public static final EsriGeodatabaseXmlFieldTypeRegistry FIELD_TYPES = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  private static Field addField(final DETable table, final FieldDefinition attribute) {
    final String fieldName = attribute.getName();
    final DataType dataType = attribute.getType();
    final EsriGeodatabaseXmlFieldType fieldType = FIELD_TYPES.getFieldType(dataType);
    if (fieldType == null) {
      throw new RuntimeException("Data type not supported " + dataType + " for " + table.getName()
        + "." + fieldName);
    } else {
      final Field field = new Field();
      field.setName(fieldName);
      field.setType(fieldType.getEsriFieldType());
      field.setIsNullable(!attribute.isRequired());
      field.setRequired(attribute.isRequired());
      int length = fieldType.getFixedLength();
      if (length < 0) {
        length = attribute.getLength();
      }
      field.setLength(length);
      final int precision;
      if (fieldType.isUsePrecision()) {
        precision = attribute.getLength();
      } else {
        precision = 0;
      }
      field.setPrecision(precision);
      final int scale = attribute.getScale();
      field.setScale(scale);
      table.addField(field);
      return field;
    }
  }

  private static void addField(final RecordDefinitionImpl metaData, final DETable deTable,
    final String tableName, final Field field, final String fieldName) {
    final FieldType fieldType = field.getType();
    final int precision = field.getPrecision();
    final DataType dataType;
    if (fieldType == FieldType.esriFieldTypeGeometry && deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      final GeometryType shapeType = featureClass.getShapeType();
      switch (shapeType) {
        case esriGeometryPoint:
          dataType = DataTypes.POINT;
        break;
        case esriGeometryMultipoint:
          dataType = DataTypes.MULTI_POINT;
        break;
        case esriGeometryPolyline:
          dataType = DataTypes.MULTI_LINE_STRING;
        break;
        case esriGeometryPolygon:
          dataType = DataTypes.POLYGON;
        break;

        default:
          throw new RuntimeException("Unknown geometry type" + shapeType + " for " + tableName
            + "." + fieldName);
      }

    } else if (precision > 0
      && (fieldType.equals(FieldType.esriFieldTypeSingle) || fieldType.equals(FieldType.esriFieldTypeDouble))) {
      dataType = DataTypes.DECIMAL;
    } else {
      dataType = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE.getDataType(fieldType);
    }
    final int scale = field.getScale();
    int length = field.getLength();
    if (precision != 0) {
      length = precision;
    }
    final Boolean required = !field.isIsNullable()
      || BooleanStringConverter.getBoolean(field.getRequired());
    final FieldDefinition attribute = new FieldDefinition(fieldName, dataType, length, scale,
      required);

    metaData.addField(attribute);
    if (fieldName.equals(tableName + "_ID")) {
      metaData.setIdFieldName(fieldName);
    }
  }

  private static void addGeometryField(final GeometryType shapeType, final DETable table,
    final FieldDefinition attribute) {
    final Field field = addField(table, attribute);
    final DEFeatureClass featureClass = (DEFeatureClass)table;
    final SpatialReference spatialReference = featureClass.getSpatialReference();
    final GeometryDef geometryDef = new GeometryDef(shapeType, spatialReference);
    field.setGeometryDef(geometryDef);

    table.addIndex(field, false, "FDO_GEOMETRY");
  }

  private static void addObjectIdField(final DETable table) {
    final Field field = new Field();
    field.setName(table.getOIDFieldName());
    field.setType(FieldType.esriFieldTypeOID);
    field.setIsNullable(false);
    field.setLength(4);
    field.setRequired(true);
    field.setEditable(false);
    table.addField(field);

    table.addIndex(field, true, "FDO_OBJECTID");
  }

  public static DEFeatureDataset createDEFeatureDataset(final String schemaName,
    final SpatialReference spatialReference) {
    final DEFeatureDataset dataset = new DEFeatureDataset();
    String name;
    final int slashIndex = schemaName.lastIndexOf('\\');
    if (slashIndex == -1) {
      name = schemaName;
    } else {
      name = schemaName.substring(slashIndex + 1);
    }
    dataset.setCatalogPath("\\" + schemaName);
    dataset.setName(name);

    final EnvelopeN envelope = new EnvelopeN(spatialReference);
    dataset.setExtent(envelope);
    dataset.setSpatialReference(spatialReference);
    return dataset;
  }

  public static List<DEFeatureDataset> createDEFeatureDatasets(final DETable table) {
    final String parentPath = table.getParentCatalogPath();
    if (parentPath.equals("\\")) {
      return Collections.emptyList();
    } else if (table instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)table;
      final String schemaName = parentPath.substring(1);
      final SpatialReference spatialReference = featureClass.getSpatialReference();
      return createDEFeatureDatasets(schemaName, spatialReference);
    } else {
      throw new IllegalArgumentException("Expected a " + DEFeatureClass.class.getName());
    }
  }

  public static List<DEFeatureDataset> createDEFeatureDatasets(final String schemaName,
    final SpatialReference spatialReference) {
    final List<DEFeatureDataset> datasets = new ArrayList<DEFeatureDataset>();
    String path = "";
    for (final String name : schemaName.split("\\\\")) {
      path += name;
      final DEFeatureDataset dataset = createDEFeatureDataset(path, spatialReference);
      datasets.add(dataset);
      path += "\\";
    }
    return datasets;
  }

  public static DETable createDETable(final RecordDefinition metaData,
    final SpatialReference spatialReference) {
    final String typePath = metaData.getPath();
    final String schemaPath = Path.getPath(typePath).replaceAll("/", "\\\\");

    return createDETable(schemaPath, metaData, spatialReference);
  }

  public static DETable createDETable(final String schemaPath, final RecordDefinition metaData,
    final SpatialReference spatialReference) {
    DETable table;
    final FieldDefinition geometryAttribute = metaData.getGeometryField();
    boolean hasGeometry = false;
    DataType geometryDataType = null;
    GeometryType shapeType = null;
    if (geometryAttribute != null) {
      if (spatialReference == null) {
        throw new IllegalArgumentException(
          "A Geometry Factory with a coordinate system must be specified.");
      }
      geometryDataType = geometryAttribute.getType();
      if (FIELD_TYPES.getFieldType(geometryDataType) != null) {
        hasGeometry = true;
        // TODO Z,m
        if (geometryDataType.equals(DataTypes.POINT)) {
          shapeType = GeometryType.esriGeometryPoint;
        } else if (geometryDataType.equals(DataTypes.MULTI_POINT)) {
          shapeType = GeometryType.esriGeometryMultipoint;
        } else if (geometryDataType.equals(DataTypes.LINE_STRING)) {
          shapeType = GeometryType.esriGeometryPolyline;
        } else if (geometryDataType.equals(DataTypes.MULTI_LINE_STRING)) {
          shapeType = GeometryType.esriGeometryPolyline;
        } else if (geometryDataType.equals(DataTypes.POLYGON)) {
          shapeType = GeometryType.esriGeometryPolygon;
        } else if (geometryDataType.equals(DataTypes.MULTI_POLYGON)) {
          shapeType = GeometryType.esriGeometryPolygon;
        } else {
          throw new IllegalArgumentException("Unable to detect geometry type");
        }
      }
    }

    final String path = metaData.getPath();
    final String name = Path.getName(path);
    if (hasGeometry) {
      final DEFeatureClass featureClass = new DEFeatureClass();
      table = featureClass;
      featureClass.setShapeType(shapeType);
      featureClass.setShapeFieldName(geometryAttribute.getName());
      final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      featureClass.setSpatialReference(spatialReference);
      featureClass.setHasM(geometryFactory.hasM());
      featureClass.setHasZ(geometryFactory.hasZ());
      final EnvelopeN envelope = new EnvelopeN(spatialReference);
      featureClass.setExtent(envelope);

    } else {
      table = new DETable();
    }

    String oidFieldName = metaData.getProperty(EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME);
    if (!Property.hasValue(oidFieldName)) {
      oidFieldName = "OBJECTID";
    }
    final String catalogPath;
    if (schemaPath.equals("\\")) {
      catalogPath = "\\" + name;
    } else {
      catalogPath = schemaPath + "\\" + name;
    }
    table.setCatalogPath(catalogPath);
    table.setName(name);
    table.setHasOID(true);
    table.setOIDFieldName(oidFieldName);

    addObjectIdField(table);
    final FieldDefinition idAttribute = metaData.getIdField();
    for (final FieldDefinition attribute : metaData.getFields()) {
      if (attribute == geometryAttribute) {
        addGeometryField(shapeType, table, attribute);
      } else {
        final String attributeName = attribute.getName();
        if (!attributeName.equals(oidFieldName)) {
          final Field field = addField(table, attribute);
          if (idAttribute == attribute) {
            table.addIndex(field, true, attributeName + "_PK");
          }
        }
      }
    }
    table.setAliasName(name);
    return table;
  }

  public static DETable getDETable(final RecordDefinition metaData,
    final SpatialReference spatialReference) {
    DETable table = metaData.getProperty(DE_TABLE_PROPERTY);
    if (table == null) {
      table = createDETable(metaData, spatialReference);
    }
    return table;
  }

  public static RecordDefinition getMetaData(final String schemaName,
    final CodedValueDomain domain, final boolean appendIdToName) {
    final String tableName;
    if (appendIdToName) {
      tableName = domain.getName() + "_ID";
    } else {
      tableName = domain.getName();
    }
    final String typePath = Path.toPath(schemaName, tableName);
    final RecordDefinitionImpl metaData = new RecordDefinitionImpl(typePath);
    final FieldType fieldType = domain.getFieldType();
    final DataType dataType = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE.getDataType(fieldType);
    int length = 0;
    for (final CodedValue codedValue : domain.getCodedValues()) {
      length = Math.max(length, codedValue.getCode().toString().length());
    }
    metaData.addField(tableName, dataType, length, true);
    metaData.addField("DESCRIPTION", DataTypes.STRING, 255, true);
    metaData.setIdFieldIndex(0);
    return metaData;
  }

  /**
   * Get a metaData instance for the table definition excluding any ESRI
   * specific fields.
   *
   * @param schemaName
   * @param deTable
   * @return
   */
  public static RecordDefinition getMetaData(final String schemaName, final DETable deTable) {
    return getMetaData(schemaName, deTable, true);
  }

  public static RecordDefinition getMetaData(final String schemaName, final DETable deTable,
    final boolean ignoreEsriFields) {
    final String tableName = deTable.getName();
    final String typePath = Path.toPath(schemaName, tableName);
    final RecordDefinitionImpl metaData = new RecordDefinitionImpl(typePath);
    final List<String> ignoreFieldNames = new ArrayList<String>();
    if (ignoreEsriFields) {
      ignoreFieldNames.add(deTable.getOIDFieldName());

      if (deTable instanceof DEFeatureClass) {
        final DEFeatureClass featureClass = (DEFeatureClass)deTable;
        ignoreFieldNames.add(featureClass.getLengthFieldName());
        ignoreFieldNames.add(featureClass.getAreaFieldName());
      }
    }
    for (final Field field : deTable.getFields()) {
      final String fieldName = field.getName();
      if (!ignoreFieldNames.contains(fieldName)) {
        addField(metaData, deTable, tableName, field, fieldName);
      }
    }
    for (final Index index : deTable.getIndexes()) {
      final String indexName = index.getName();
      if (indexName.endsWith("_PK")) {
        final List<Field> indexFields = index.getFields();
        final Field indexField = CollectionUtil.get(indexFields, 0);
        final String idName = indexField.getName();
        metaData.setIdFieldName(idName);
      }
    }
    if (deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      final String shapeFieldName = featureClass.getShapeFieldName();
      metaData.setGeometryFieldName(shapeFieldName);
      final SpatialReference spatialReference = featureClass.getSpatialReference();
      GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      if (featureClass.isHasM()) {
        geometryFactory = GeometryFactory.getFactory(geometryFactory.getSRID(), 4,
          geometryFactory.getScaleXY(), geometryFactory.getScaleZ());
      } else if (featureClass.isHasZ()) {
        geometryFactory = GeometryFactory.getFactory(geometryFactory.getSRID(), 3,
          geometryFactory.getScaleXY(), geometryFactory.getScaleZ());
      }
      final FieldDefinition geometryAttribute = metaData.getGeometryField();
      geometryAttribute.setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
    }

    return metaData;
  }

  public static List<Record> getValues(final RecordDefinition metaData,
    final CodedValueDomain domain) {
    final List<Record> values = new ArrayList<Record>();
    for (final CodedValue codedValue : domain.getCodedValues()) {
      final Record value = new ArrayRecord(metaData);
      value.setIdValue(codedValue.getCode());
      value.setValue("DESCRIPTION", codedValue.getName());
      values.add(value);
    }
    return values;
  }
}
