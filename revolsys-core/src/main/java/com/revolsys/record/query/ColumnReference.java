package com.revolsys.record.query;

import java.sql.PreparedStatement;

import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;

public interface ColumnReference extends QueryValue {

  @Override
  default int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  ColumnReference clone();

  default String getAliasName() {
    return getName();
  }

  FieldDefinition getFieldDefinition();

  String getName();

  @Override
  String getStringValue(final Record record);

  TableReference getTable();

  @Override
  @SuppressWarnings("unchecked")
  default <V> V getValue(final Record record) {
    if (record == null) {
      return null;
    } else {
      final String name = getName();
      return (V)record.getValue(name);
    }
  }

  @SuppressWarnings("unchecked")
  default <V> V toColumnType(final Object value) {
    try {
      return toColumnTypeException(value);
    } catch (final Throwable e) {
      return (V)value;
    }
  }

  <V> V toColumnTypeException(final Object value);

  @SuppressWarnings("unchecked")
  default <V> V toFieldValue(final Object value) {
    try {
      return toColumnTypeException(value);
    } catch (final Throwable e) {
      return (V)value;
    }
  }

  <V> V toFieldValueException(final Object value);

  String toString(Object value);
}
