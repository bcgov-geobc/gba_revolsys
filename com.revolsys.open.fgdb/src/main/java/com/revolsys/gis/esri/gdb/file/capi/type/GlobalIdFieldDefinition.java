package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.datatype.DataTypes;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.file.capi.swig.Guid;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;

public class GlobalIdFieldDefinition extends AbstractFileGdbFieldDefinition {
  public GlobalIdFieldDefinition(final Field field) {
    this(field.getName(), field.getLength(),
      BooleanStringConverter.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  public GlobalIdFieldDefinition(final String name, final int length, final boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (getSync()) {
      final Guid guid = row.getGlobalId();
      return guid.toString();
    }
  }

  @Override
  public boolean isAutoCalculated() {
    return true;
  }

  @Override
  public void setPostInsertValue(final Record record, final Row row) {
    synchronized (getSync()) {
      final Guid guid = row.getGlobalId();
      final String name = getName();
      final String string = guid.toString();
      record.setValue(name, string);
    }
  }

  @Override
  public Object setValue(final Record record, final Row row, final Object value) {
    return null;
  }

}
