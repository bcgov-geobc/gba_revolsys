package com.revolsys.gis.data.io;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.RecordDefinition;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.util.ExceptionUtil;

public class DataObjectStoreSchema extends AbstractObjectWithProperties {

  private Reference<AbstractDataObjectStore> dataStore;

  private Map<String, RecordDefinition> metaDataCache = new TreeMap<String, RecordDefinition>();

  private String path;

  public DataObjectStoreSchema() {
  }

  public DataObjectStoreSchema(final AbstractDataObjectStore dataStore,
    final String path) {
    this.dataStore = new WeakReference<AbstractDataObjectStore>(dataStore);
    this.path = path;
  }

  public void addMetaData(final RecordDefinition metaData) {
    addMetaData(metaData.getPath(), metaData);
  }

  protected void addMetaData(final String typePath,
    final RecordDefinition metaData) {
    metaDataCache.put(typePath.toUpperCase(), metaData);
  }

  @Override
  @PreDestroy
  public void close() {
    if (metaDataCache != null) {
      for (final RecordDefinition metaData : metaDataCache.values()) {
        metaData.destroy();
      }
      metaDataCache.clear();
    }
    dataStore = null;
    metaDataCache = null;
    path = null;
    super.close();
  }

  public synchronized RecordDefinition findMetaData(final String typePath) {
    final RecordDefinition metaData = metaDataCache.get(typePath);
    return metaData;
  }

  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getDataStore() {
    if (dataStore == null) {
      return null;
    } else {
      return (V)dataStore.get();
    }
  }

  public GeometryFactory getGeometryFactory() {
    final GeometryFactory geometryFactory = getProperty("geometryFactory");
    if (geometryFactory == null) {
      final AbstractDataObjectStore dataStore = getDataStore();
      if (dataStore == null) {
        return GeometryFactory.getFactory();
      } else {
        return dataStore.getGeometryFactory();
      }
    } else {
      return geometryFactory;
    }
  }

  public synchronized RecordDefinition getMetaData(String typePath) {
    typePath = typePath.toUpperCase();
    if (typePath.startsWith(path + "/") || path.equals("/")) {
      if (metaDataCache.isEmpty()) {
        refreshMetaData();
      }
      final RecordDefinition metaData = metaDataCache.get(typePath.toUpperCase());
      return metaData;
    } else {
      return null;
    }
  }

  protected Map<String, RecordDefinition> getMetaDataCache() {
    return metaDataCache;
  }

  public String getName() {
    final String path = getPath();
    return PathUtil.getName(path);
  }

  public String getPath() {
    return path;
  }

  public List<String> getTypeNames() {
    if (metaDataCache.isEmpty()) {
      refreshMetaData();
    }
    return new ArrayList<String>(metaDataCache.keySet());
  }

  public List<RecordDefinition> getTypes() {
    if (metaDataCache.isEmpty()) {
      refreshMetaData();
    }
    return new ArrayList<RecordDefinition>(metaDataCache.values());
  }

  public void refreshMetaData() {
    final AbstractDataObjectStore dataStore = getDataStore();
    if (dataStore != null) {
      final Collection<DataObjectStoreExtension> extensions = dataStore.getDataStoreExtensions();
      for (final DataObjectStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(dataStore)) {
            extension.preProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(),
            "Unable to pre-process schema " + this, e);
        }
      }
      dataStore.loadSchemaDataObjectMetaData(this, metaDataCache);
      for (final DataObjectStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(dataStore)) {
            extension.postProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(),
            "Unable to post-process schema " + this, e);
        }
      }
    }
  }

  @Override
  public String toString() {
    return path;
  }
}
