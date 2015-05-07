package com.revolsys.gis.data.io;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.revolsys.data.io.RecordStoreFactoryRegistry;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.util.Property;

public class DataObjectStoreFactoryBean extends
  AbstractFactoryBean<RecordStore> {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  @Override
  protected RecordStore createInstance() throws Exception {
    final RecordStore dataObjectStore = RecordStoreFactoryRegistry.createDataObjectStore(config);
    Property.set(dataObjectStore, properties);
    dataObjectStore.initialize();
    return dataObjectStore;
  }

  @Override
  protected void destroyInstance(final RecordStore dataObjectStore)
    throws Exception {
    dataObjectStore.close();
    properties = null;
    config = null;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  @Override
  public Class<?> getObjectType() {
    return RecordStore.class;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setConfig(final Map<String, Object> config) {
    this.config = config;
  }

  public void setProperties(final Map<String, Object> properties) {
    this.properties = properties;
  }

  public void setUrl(final String url) {
    config.put("url", url);
  }
}
