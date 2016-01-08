package com.revolsys.record.schema;

import java.util.List;

import com.revolsys.io.PathName;
import com.revolsys.record.RecordFactory;

public interface RecordDefinitionProxy {
  default int getFieldCount() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldCount();
  }

  default FieldDefinition getFieldDefinition(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getField(fieldName);
  }

  default FieldDefinition getFieldDefinition(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getField(fieldIndex);
  }

  default List<FieldDefinition> getFieldDefinitions() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFields();
  }

  default int getFieldIndex(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldIndex(fieldName);
  }

  default String getFieldName(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldName(fieldIndex);
  }

  default List<String> getFieldNames() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldNames();
  }

  default String getFieldTitle(final String name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldTitle(name);
  }

  default String getGeometryFieldName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getGeometryFieldName();
  }

  RecordDefinition getRecordDefinition();

  default RecordFactory getRecordFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordFactory();
    }
  }

  default RecordStore getRecordStore() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordStore();
    }
  }

  default String getTypePath() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getPath();
  }

  default PathName getTypePathName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getPathName();
  }

  /**
   * Checks to see if the definition for this record has a field with the
   * specified name.
   *
   * @param name The name of the field.
   * @return True if the record has a field with the specified name.
   */
  default boolean hasField(final CharSequence name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.hasField(name);
  }
}
