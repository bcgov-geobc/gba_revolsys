package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreImpl;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class OidFieldDefinition extends AbstractFileGdbFieldDefinition {
  public OidFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.INT,
      BooleanStringConverter.getBoolean(field.getRequired())
        || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStoreImpl recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (getSync()) {
        return row.getOid();
      }
    }
  }

  @Override
  public void setPostInsertValue(final Record record, final Row row) {
    synchronized (getSync()) {
      final int oid = row.getOid();
      final String name = getName();
      record.setValue(name, oid);
    }
  }

  @Override
  public Object setUpdateValue(final Record record, final Row row,
    final Object value) {
    return value;
  }

  @Override
  public Object setValue(final Record record, final Row row, final Object value) {
    return null;
  }

}
