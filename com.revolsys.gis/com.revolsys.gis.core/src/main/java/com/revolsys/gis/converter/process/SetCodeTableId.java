package com.revolsys.gis.converter.process;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.codes.CodeTable;

public class SetCodeTableId implements
  SourceToTargetProcess<DataObject, DataObject> {
  private final CodeTable codeTable;

  private final Map<String, Converter<DataObject, Object>> codeTableValueConverters = new HashMap<String, Converter<DataObject, Object>>();

  private final String targetAttributeName;

  public SetCodeTableId(final CodeTable codeTable,
    final String targetAttributeName) {
    this.codeTable = codeTable;
    this.targetAttributeName = targetAttributeName;
  }

  public void process(final DataObject source, final DataObject target) {
    final Map<String, Object> codeTableValues = new HashMap<String, Object>();

    for (final Entry<String, Converter<DataObject, Object>> entry : codeTableValueConverters.entrySet()) {
      String codeTableAttributeName = entry.getKey();
      final Converter<DataObject, Object> sourceAttributeConverter = entry.getValue();
      Object sourceValue = sourceAttributeConverter.convert(source);
      if (sourceValue != null) {
        DataObjectStore dataObjectStore = target.getMetaData()
          .getDataObjectStore();
        if (dataObjectStore != null) {
          String codeTableValueName = null;
          final int dotIndex = codeTableAttributeName.indexOf(".");
          if (dotIndex != -1) {
            codeTableValueName = codeTableAttributeName.substring(dotIndex + 1);
            codeTableAttributeName = codeTableAttributeName.substring(0,
              dotIndex);
          }
          CodeTable targetCodeTable = dataObjectStore.getCodeTableByColumn(codeTableAttributeName);
          if (targetCodeTable != null) {
            if (codeTableValueName == null) {
              sourceValue = targetCodeTable.getId(sourceValue);
            } else {
              sourceValue = targetCodeTable.getId(Collections.singletonMap(codeTableValueName, sourceValue));
            }
          }
        }
      }
      codeTableValues.put(codeTableAttributeName, sourceValue);
    }
    final Object codeId = codeTable.getId(codeTableValues);
    target.setValue(targetAttributeName, codeId);
  }

  public void setValueMapping(final String codeTableAttribute,
    final Converter<DataObject, Object> valueConverter) {
    codeTableValueConverters.put(codeTableAttribute, valueConverter);

  }

  @Override
  public String toString() {
    return "setCodeTableId" + codeTableValueConverters;
  }
}
