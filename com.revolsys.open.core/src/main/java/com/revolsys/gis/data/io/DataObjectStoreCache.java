package com.revolsys.gis.data.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.gis.cs.BoundingBox;

public class DataObjectStoreCache {
  public static DataObjectStoreCache getCache(final RecordStore dataStore) {
    return new DataObjectStoreCache(dataStore);
  }

  private final Map<BoundingBox, List> cachedObejcts = Collections.synchronizedMap(new HashMap<BoundingBox, List>());

  private final RecordStore dataStore;

  private final Map<BoundingBox, DataStoreQueryTask> loadTasks = new LinkedHashMap<BoundingBox, DataStoreQueryTask>();

  private String typePath;

  public DataObjectStoreCache(final RecordStore dataStore) {
    this.dataStore = dataStore;
  }

  private void addBoundingBox(final BoundingBox boundingBox) {
    synchronized (loadTasks) {
      if (!loadTasks.containsKey(boundingBox)) {
        loadTasks.put(boundingBox, new DataStoreQueryTask(dataStore, typePath,
          boundingBox));
      }
    }
  }

  public List getObjects(final BoundingBox boundingBox) {
    final List objects = cachedObejcts.get(boundingBox);
    if (objects == null) {
      addBoundingBox(boundingBox);
    }
    return objects;
  }

  public void removeObjects(final BoundingBox boundingBox) {
    synchronized (loadTasks) {
      final DataStoreQueryTask task = loadTasks.get(boundingBox);
      if (task != null) {
        task.cancel();
        loadTasks.remove(task);
      }
    }
    cachedObejcts.remove(boundingBox);
  }

}
