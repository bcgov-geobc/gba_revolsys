package com.revolsys.data.io;

import java.util.Map;

public interface RecordStoreExtension {

  public abstract void initialize(RecordStore recordStore,
    Map<String, Object> connectionProperties);

  boolean isEnabled(RecordStore recordStore);

  public abstract void postProcess(RecordStoreSchema schema);

  public abstract void preProcess(RecordStoreSchema schema);
}
