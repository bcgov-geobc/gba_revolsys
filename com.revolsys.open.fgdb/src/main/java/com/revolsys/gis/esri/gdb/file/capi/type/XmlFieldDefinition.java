package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreImpl;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class XmlFieldDefinition extends AbstractFileGdbFieldDefinition {

  public XmlFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      BooleanStringConverter.getBoolean(field.getRequired())
        || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStoreImpl recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (getSync()) {
        return row.getXML(name);
      }
    }
  }

  @Override
  public Object setValue(final Record record, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getRecordStore().setNull(row, name);
      }
      return null;
    } else {
      final String string = value.toString();
      synchronized (getSync()) {
        row.setXML(name, string);
      }
      return string;
    }
  }

}
