package com.revolsys.data.codes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.revolsys.data.query.And;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.RecordDefinitionProperty;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.revolsys.io.Path;
import com.revolsys.io.Reader;
import com.revolsys.util.Property;

public class CodeTableProperty extends AbstractCodeTable implements
  RecordDefinitionProperty {

  private static final ArrayList<String> DEFAULT_ATTRIBUTE_NAMES = new ArrayList<String>(
    Arrays.asList("VALUE"));

  public static final String PROPERTY_NAME = CodeTableProperty.class.getName();

  public static final CodeTableProperty getProperty(
    final RecordDefinition metaData) {
    final CodeTableProperty property = metaData.getProperty(PROPERTY_NAME);
    return property;
  }

  private String creationTimestampAttributeName;

  private String modificationTimestampAttributeName;

  private List<String> attributeAliases = new ArrayList<String>();

  private RecordStore dataStore;

  private boolean loadAll = true;

  private RecordDefinition metaData;

  private List<String> valueAttributeNames = DEFAULT_ATTRIBUTE_NAMES;

  private List<String> orderBy = DEFAULT_ATTRIBUTE_NAMES;

  private String typePath;

  private String idAttributeName;

  private boolean createMissingCodes = true;

  private boolean loading = false;

  private boolean loadMissingCodes = true;

  private final ThreadLocal<Boolean> threadLoading = new ThreadLocal<Boolean>();

  public CodeTableProperty() {
  }

  public void addAttributeAlias(final String columnName) {
    attributeAliases.add(columnName);
  }

  public void addValue(final Record code) {
    final Object id = code.getValue(getIdAttributeName());
    final List<Object> values = new ArrayList<Object>();
    for (final String attributeName : this.valueAttributeNames) {
      final Object value = code.getValue(attributeName);
      values.add(value);
    }
    addValue(id, values);
  }

  protected void addValues(final Iterable<Record> allCodes) {
    for (final Record code : allCodes) {
      addValue(code);
    }
  }

  @Override
  public CodeTableProperty clone() {
    final CodeTableProperty clone = (CodeTableProperty)super.clone();
    clone.metaData = null;
    clone.attributeAliases = new ArrayList<String>(attributeAliases);
    clone.valueAttributeNames = new ArrayList<String>(valueAttributeNames);
    return clone;
  }

  protected synchronized Object createId(final List<Object> values) {
    if (createMissingCodes) {
      // TODO prevent duplicates from other threads/processes
      final Record code = dataStore.create(typePath);
      final RecordDefinition metaData = code.getRecordDefinition();
      Object id = dataStore.createPrimaryIdValue(typePath);
      if (id == null) {
        final FieldDefinition idAttribute = metaData.getIdField();
        if (idAttribute != null) {
          if (Number.class.isAssignableFrom(idAttribute.getType()
            .getJavaClass())) {
            id = getNextId();
          } else {
            id = UUID.randomUUID().toString();
          }
        }
      }
      code.setIdValue(id);
      for (int i = 0; i < valueAttributeNames.size(); i++) {
        final String name = valueAttributeNames.get(i);
        final Object value = values.get(i);
        code.setValue(name, value);
      }

      final Timestamp now = new Timestamp(System.currentTimeMillis());
      if (creationTimestampAttributeName != null) {
        code.setValue(creationTimestampAttributeName, now);
      }
      if (modificationTimestampAttributeName != null) {
        code.setValue(modificationTimestampAttributeName, now);
      }

      dataStore.insert(code);
      id = code.getIdValue();
      return id;
    } else {
      return null;
    }
  }

  @Override
  public List<String> getAttributeAliases() {
    return attributeAliases;
  }

  @Override
  public Map<Object, List<Object>> getCodes() {
    final Map<Object, List<Object>> codes = super.getCodes();
    if (codes.isEmpty() && isLoadAll()) {
      loadAll();
      return super.getCodes();
    } else {
      return codes;
    }
  }

  public String getCreationTimestampAttributeName() {
    return creationTimestampAttributeName;
  }

  public RecordStore getDataStore() {
    return dataStore;
  }

  @Override
  public String getIdAttributeName() {
    if (StringUtils.hasText(idAttributeName)) {
      return idAttributeName;
    } else if (metaData == null) {
      return "";
    } else {
      final String idAttributeName = metaData.getIdFieldName();
      if (StringUtils.hasText(idAttributeName)) {
        return idAttributeName;
      } else {
        return metaData.getFieldName(0);
      }
    }
  }

  @Override
  public Map<String, ? extends Object> getMap(final Object id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<String, Object>();
      for (int i = 0; i < values.size(); i++) {
        final String name = valueAttributeNames.get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  @Override
  public RecordDefinition getMetaData() {
    return metaData;
  }

  public String getModificationTimestampAttributeName() {
    return modificationTimestampAttributeName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public String getTypeName() {
    return typePath;
  }

  @Override
  public List<String> getValueAttributeNames() {
    return valueAttributeNames;
  }

  public boolean isCreateMissingCodes() {
    return createMissingCodes;
  }

  public boolean isLoadAll() {
    return loadAll;
  }

  protected synchronized void loadAll() {
    if (threadLoading.get() != Boolean.TRUE) {
      if (loading) {
        while (loading) {
          try {
            wait(1000);
          } catch (final InterruptedException e) {
          }
        }
        return;
      } else {
        threadLoading.set(Boolean.TRUE);
        loading = true;
        try {
          final RecordDefinition metaData = dataStore.getRecordDefinition(typePath);
          final Query query = new Query(typePath);
          query.setAttributeNames(metaData.getFieldNames());
          for (final String order : orderBy) {
            query.addOrderBy(order, true);
          }
          try (
            Reader<Record> reader = dataStore.query(query)) {
            final List<Record> codes = reader.read();
            dataStore.getStatistics()
              .getStatistics("query")
              .add(typePath, -codes.size());
            Collections.sort(codes, new DataObjectAttributeComparator(orderBy));
            addValues(codes);
          }
          Property.firePropertyChange(this, "valuesChanged", false, true);
        } finally {
          loading = false;
          threadLoading.set(null);
        }
      }
    }
  }

  @Override
  protected synchronized Object loadId(final List<Object> values,
    final boolean createId) {
    if (loadAll && !loadMissingCodes && !isEmpty()) {
      return null;
    }
    Object id = null;
    if (createId && loadAll) {
      loadAll();
      id = getId(values, false);
    } else {
      final Query query = new Query(typePath);
      final And and = new And();
      if (!values.isEmpty()) {
        int i = 0;
        for (final String attributeName : valueAttributeNames) {
          final Object value = values.get(i);
          if (value == null) {
            and.add(Q.isNull(attributeName));
          } else {
            final FieldDefinition attribute = metaData.getField(attributeName);
            and.add(Q.equal(attribute, value));
          }
          i++;
        }
      }
      query.setWhereCondition(and);
      final Reader<Record> reader = dataStore.query(query);
      try {
        final List<Record> codes = reader.read();
        dataStore.getStatistics()
          .getStatistics("query")
          .add(typePath, -codes.size());
        addValues(codes);
        id = getIdByValue(values);
        Property.firePropertyChange(this, "valuesChanged", false, true);
      } finally {
        reader.close();
      }
    }
    if (createId && id == null) {
      return createId(values);
    } else {
      return id;
    }
  }

  @Override
  protected List<Object> loadValues(final Object id) {
    List<Object> values = null;
    if (loadAll) {
      loadAll();
      values = getValueById(id);
    } else {
      final Record code = dataStore.load(typePath, id);
      if (code != null) {
        addValue(code);
        values = getValueById(id);
      }
    }
    return values;
  }

  @Override
  public synchronized void refresh() {
    super.refresh();
    if (isLoadAll()) {
      loadAll();
    }
  }

  public void setAttributeAliases(final List<String> columnAliases) {
    this.attributeAliases = columnAliases;
  }

  public void setCreateMissingCodes(final boolean createMissingCodes) {
    this.createMissingCodes = createMissingCodes;
  }

  public void setCreationTimestampAttributeName(
    final String creationTimestampAttributeName) {
    this.creationTimestampAttributeName = creationTimestampAttributeName;
  }

  public void setIdAttributeName(final String idAttributeName) {
    this.idAttributeName = idAttributeName;
  }

  public void setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
  }

  public void setLoadMissingCodes(final boolean loadMissingCodes) {
    this.loadMissingCodes = loadMissingCodes;
  }

  @Override
  public void setRecordDefinition(final RecordDefinition metaData) {
    if (this.metaData != metaData) {
      if (this.metaData != null) {
        this.metaData.setProperty(getPropertyName(), null);
      }
      this.metaData = metaData;
      if (metaData == null) {
        this.dataStore = null;
        this.typePath = null;
      } else {
        this.typePath = metaData.getPath();
        setName(Path.getName(typePath));
        this.dataStore = this.metaData.getRecordStore();
        metaData.setProperty(getPropertyName(), this);
        dataStore.addCodeTable(this);
      }
    }
  }

  public void setModificationTimestampAttributeName(
    final String modificationTimestampAttributeName) {
    this.modificationTimestampAttributeName = modificationTimestampAttributeName;
  }

  public void setOrderBy(final List<String> orderBy) {
    this.orderBy = new ArrayList<String>(orderBy);
  }

  public void setValueAttributeName(final String valueColumns) {
    setValueAttributeNames(valueColumns);
  }

  public void setValueAttributeNames(final List<String> valueColumns) {
    this.valueAttributeNames = new ArrayList<String>(valueColumns);
    if (this.orderBy == DEFAULT_ATTRIBUTE_NAMES) {
      setOrderBy(valueColumns);
    }
  }

  public void setValueAttributeNames(final String... valueColumns) {
    setValueAttributeNames(Arrays.asList(valueColumns));
  }

  @Override
  public String toString() {
    return typePath + " " + getIdAttributeName() + " " + valueAttributeNames;

  }

  public String toString(final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
