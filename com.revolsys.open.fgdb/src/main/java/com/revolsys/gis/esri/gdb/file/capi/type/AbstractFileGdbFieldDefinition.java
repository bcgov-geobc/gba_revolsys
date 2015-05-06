package com.revolsys.gis.esri.gdb.file.capi.type;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public abstract class AbstractFileGdbFieldDefinition extends FieldDefinition {

  private Reference<CapiFileGdbRecordStore> dataStore;

  public AbstractFileGdbFieldDefinition(final String name,
    final DataType dataType, final boolean required) {
    super(name, dataType, required);
  }

  public AbstractFileGdbFieldDefinition(final String name,
    final DataType dataType, final int length, final boolean required) {
    super(name, dataType, length, required);
  }

  public CapiFileGdbRecordStore getDataStore() {
    if (this.dataStore == null) {
      return null;
    } else {
      return this.dataStore.get();
    }
  }

  public abstract Object getValue(Row row);

  public void setDataStore(final CapiFileGdbRecordStore dataStore) {
    this.dataStore = new WeakReference<CapiFileGdbRecordStore>(dataStore);
  }

  public Object setInsertValue(final Record object, final Row row,
    final Object value) {
    return setValue(object, row, value);
  }

  public void setPostInsertValue(final Record object, final Row row) {
  }

  public Object setUpdateValue(final Record object, final Row row,
    final Object value) {
    return setValue(object, row, value);
  }

  public abstract Object setValue(Record object, Row row, Object value);
}
