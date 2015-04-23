package com.revolsys.gis.esri.gdb.file.capi.type;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class DoubleAttribute extends AbstractFileGdbFieldDefinition {
  public DoubleAttribute(final Field field) {
    super(field.getName(), DataTypes.DOUBLE,
      BooleanStringConverter.getBoolean(field.getRequired())
      || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 19;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final CapiFileGdbRecordStore dataStore = getDataStore();
    if (dataStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (dataStore) {
        return row.getDouble(name);
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
      final double doubleValue = number.doubleValue();
      synchronized (getDataStore()) {
        row.setDouble(name, doubleValue);
      }
      return doubleValue;
    } else {
      final String string = value.toString();
      if (StringUtils.hasText(string)) {
        final double doubleValue = Double.parseDouble(string);
        synchronized (getDataStore()) {
          row.setDouble(name, doubleValue);
        }
        return doubleValue;
      } else if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getDataStore().setNull(row, name);
        return null;
      }
    }
  }

}
