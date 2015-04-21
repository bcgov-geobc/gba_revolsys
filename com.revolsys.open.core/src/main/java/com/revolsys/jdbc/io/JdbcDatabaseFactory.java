package com.revolsys.jdbc.io;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.gis.data.io.RecordStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;

public interface JdbcDatabaseFactory extends DataObjectStoreFactory {

  boolean canHandleUrl(String url);

  void closeDataSource(DataSource dataSource);

  JdbcDataObjectStore createDataObjectStore(DataSource dataSource);

  @Override
  JdbcDataObjectStore createDataObjectStore(
    Map<String, ? extends Object> connectionProperties);

  DataSource createDataSource(Map<String, ? extends Object> connectionProperties);

  @Override
  Class<? extends RecordStore> getDataObjectStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<String> getProductNames();

  @Override
  List<String> getUrlPatterns();
}
