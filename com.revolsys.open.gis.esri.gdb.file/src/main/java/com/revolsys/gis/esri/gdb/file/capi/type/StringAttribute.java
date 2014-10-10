package com.revolsys.gis.esri.gdb.file.capi.type;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class StringAttribute extends AbstractFileGdbAttribute {
  public StringAttribute(final Field field) {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      BooleanStringConverter.getBoolean(field.getRequired())
      || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final CapiFileGdbDataObjectStore dataStore = getDataStore();
    if (dataStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (dataStore) {
        return row.getString(name);
      }
    }
  }

  @Override
  public Object setValue(final DataObject object, final Row row,
    final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getDataStore().setNull(row, name);
      }
      return null;
    } else {
      String string = value.toString();
      if (string.length() > getLength()) {
        LoggerFactory.getLogger(getClass()).warn(
          "Value is to long for: " + this + ":" + string);
        string = string.substring(0, getLength());
      }
      synchronized (getDataStore()) {
        row.setString(name, string);
      }
      return string;
    }
  }
}
