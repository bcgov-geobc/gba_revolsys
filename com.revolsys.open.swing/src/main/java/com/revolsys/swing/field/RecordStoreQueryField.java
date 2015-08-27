package com.revolsys.swing.field;

import java.util.List;
import java.util.function.Supplier;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.Reader;

public class RecordStoreQueryField extends AbstractRecordQueryField {
  private static final long serialVersionUID = 1L;

  public static Supplier<Field> factory(final String fieldName, final RecordStore recordStore,
    final String typePath, final String displayFieldName) {
    return () -> {
      return new RecordStoreQueryField(fieldName, recordStore, typePath, displayFieldName);
    };
  }

  private final RecordDefinition recordDefinition;

  private final RecordStore recordStore;

  public RecordStoreQueryField(final String fieldName, final RecordStore recordStore,
    final String typePath, final String displayFieldName) {
    super(fieldName, typePath, displayFieldName);
    this.recordStore = recordStore;
    this.recordDefinition = recordStore.getRecordDefinition(typePath);
  }

  @Override
  public RecordStoreQueryField clone() {
    final String fieldName = getFieldName();
    final String typePath = getTypePath();
    final String displayFieldName = getDisplayFieldName();
    return new RecordStoreQueryField(fieldName, this.recordStore, typePath, displayFieldName);
  }

  @Override
  protected Record getRecord(final Object identifier) {
    final String typePath = getTypePath();
    return this.recordStore.load(typePath, identifier);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  protected List<Record> getRecords(final Query query) {
    try (
      Reader<Record> reader = this.recordStore.query(query)) {
      return reader.read();
    }
  }
}
