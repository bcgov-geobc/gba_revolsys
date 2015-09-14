package com.revolsys.record.schema;

import com.revolsys.properties.ObjectWithProperties;

public interface RecordDefinitionFactory extends ObjectWithProperties {
  RecordDefinition getRecordDefinition(String path);

}
