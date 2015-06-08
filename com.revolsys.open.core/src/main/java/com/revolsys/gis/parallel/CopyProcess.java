package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class CopyProcess extends BaseInOutProcess<Record, Record> {

  private String typeName;

  private RecordDefinitionFactory metaDataFactory;

  private RecordDefinition metaData;

  private Map<String, Map<Object, Object>> valueMaps = new HashMap<String, Map<Object, Object>>();

  private Map<String, String> attributeMap = new HashMap<String, String>();

  public CopyProcess() {
  }

  protected Record copy(final Record object) {
    Record targetObject;
    if (this.metaData == null) {
      targetObject = object;
    } else {
      targetObject = new ArrayRecord(this.metaData);
      for (final String attributeName : this.metaData.getFieldNames()) {
        copyAttribute(object, attributeName, targetObject, attributeName);
      }
      if (this.attributeMap != null) {
        for (final Entry<String, String> mapping : this.attributeMap.entrySet()) {
          final String sourceAttributeName = mapping.getKey();
          final String targetAttributeName = mapping.getValue();
          copyAttribute(object, sourceAttributeName, targetObject, targetAttributeName);
        }
      }
    }
    return targetObject;
  }

  private void copyAttribute(final Record sourceObject, final String sourceAttributeName,
    final Record targetObject, final String targetAttributeName) {
    Object value = sourceObject.getValueByPath(sourceAttributeName);
    final Map<Object, Object> valueMap = this.valueMaps.get(targetAttributeName);
    if (valueMap != null) {
      final Object mappedValue = valueMap.get(value);
      if (mappedValue != null) {
        value = mappedValue;
      }
    }
    targetObject.setValue(targetAttributeName, value);
  }

  public Map<String, String> getAttributeMap() {
    return this.attributeMap;
  }

  public RecordDefinition getMetaData() {
    return this.metaData;
  }

  public RecordDefinitionFactory getMetaDataFactory() {
    return this.metaDataFactory;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public Map<String, Map<Object, Object>> getValueMaps() {
    return this.valueMaps;
  }

  @Override
  @PostConstruct
  protected void init() {
    super.init();
    if (this.metaData == null) {
      this.metaData = this.metaDataFactory.getRecordDefinition(this.typeName);
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Record targetObject = copy(object);
    out.write(targetObject);
  }

  public void setAttributeMap(final Map<String, String> attributeMap) {
    this.attributeMap = attributeMap;
  }

  public void setMetaData(final RecordDefinition metaData) {
    this.metaData = metaData;
  }

  public void setMetaDataFactory(final RecordDefinitionFactory metaDataFactory) {
    this.metaDataFactory = metaDataFactory;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  public void setValueMaps(final Map<String, Map<Object, Object>> valueMaps) {
    this.valueMaps = valueMaps;
  }

}
