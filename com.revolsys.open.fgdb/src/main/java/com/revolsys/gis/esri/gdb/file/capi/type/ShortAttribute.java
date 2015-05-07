package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreImpl;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class ShortAttribute extends AbstractFileGdbFieldDefinition {
  public ShortAttribute(final Field field) {
    super(field.getName(), DataTypes.SHORT,
      BooleanStringConverter.getBoolean(field.getRequired())
        || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 6;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStoreImpl dataStore = getDataStore();
    if (dataStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (dataStore) {
        return row.getShort(name);
      }
    }
  }

  @Override
  public Object setValue(final Record object, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getDataStore().setNull(row, name);
      }
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final short shortValue = number.shortValue();
      synchronized (getDataStore()) {
        row.setShort(name, shortValue);
      }
      return shortValue;
    } else {
      final String string = value.toString();
      final short shortValue = Short.parseShort(string);
      synchronized (getDataStore()) {
        row.setShort(name, shortValue);
      }
      return shortValue;
    }
  }
}
