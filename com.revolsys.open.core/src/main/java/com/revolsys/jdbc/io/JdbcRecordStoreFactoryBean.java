package com.revolsys.jdbc.io;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.revolsys.util.Property;

public class JdbcRecordStoreFactoryBean extends AbstractFactoryBean<JdbcRecordStore>
implements ApplicationContextAware {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  private DataSource dataSource;

  private ApplicationContext applicationContext;

  @Override
  protected JdbcRecordStore createInstance() throws Exception {
    JdbcRecordStore dataObjectStore;
    final JdbcFactoryRegistry jdbcFactoryRegistry = JdbcFactoryRegistry.getFactory(this.applicationContext);
    if (this.dataSource == null) {
      final JdbcDatabaseFactory databaseFactory = jdbcFactoryRegistry.getDatabaseFactory(this.config);
      dataObjectStore = databaseFactory.createRecordStore(this.config);
    } else {
      final JdbcDatabaseFactory databaseFactory = jdbcFactoryRegistry.getDatabaseFactory(this.dataSource);
      dataObjectStore = databaseFactory.createDataObjectStore(this.dataSource);
    }
    Property.set(dataObjectStore, this.properties);
    dataObjectStore.initialize();
    return dataObjectStore;
  }

  @Override
  protected void destroyInstance(final JdbcRecordStore dataObjectStore) throws Exception {
    dataObjectStore.close();
    this.config = null;
    this.dataSource = null;
    this.properties = null;
    this.applicationContext = null;
  }

  public Map<String, Object> getConfig() {
    return this.config;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public Class<?> getObjectType() {
    return JdbcRecordStore.class;
  }

  public Map<String, Object> getProperties() {
    return this.properties;
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setConfig(final Map<String, Object> config) {
    this.config = config;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setProperties(final Map<String, Object> properties) {
    this.properties = properties;
  }
}
