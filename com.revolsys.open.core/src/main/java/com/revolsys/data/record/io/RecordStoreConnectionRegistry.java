package com.revolsys.data.record.io;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.revolsys.collection.map.Maps;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.format.json.JsonMapIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnectionRegistry;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class RecordStoreConnectionRegistry extends
AbstractConnectionRegistry<RecordStoreConnection> {

  private static final ThreadLocal<RecordStoreConnectionRegistry> threadRegistry = new ThreadLocal<RecordStoreConnectionRegistry>();

  public static RecordStoreConnectionRegistry getForThread() {
    return RecordStoreConnectionRegistry.threadRegistry.get();
  }

  public static RecordStoreConnectionRegistry setForThread(
    final RecordStoreConnectionRegistry registry) {
    final RecordStoreConnectionRegistry oldValue = getForThread();
    RecordStoreConnectionRegistry.threadRegistry.set(registry);
    return oldValue;
  }

  protected RecordStoreConnectionRegistry(
    final RecordStoreConnectionManager connectionManager, final String name,
    final boolean visible) {
    super(connectionManager, name);
    setVisible(visible);
    init();
  }

  protected RecordStoreConnectionRegistry(
    final RecordStoreConnectionManager connectionManager, final String name,
    final Resource resource) {
    super(connectionManager, name);
    setDirectory(resource);
    init();
  }

  public RecordStoreConnectionRegistry(final String name) {
    this(null, name, true);
  }

  public RecordStoreConnectionRegistry(final String name, final Resource resource,
    final boolean readOnly) {
    super(null, name);
    setReadOnly(readOnly);
    setDirectory(resource);
    init();
  }

  public void addConnection(final RecordStoreConnection connection) {
    addConnection(connection.getName(), connection);
  }

  public void addConnection(final Map<String, Object> config) {
    final RecordStoreConnection connection = new RecordStoreConnection(this, null, config);
    addConnection(connection);
  }

  public void addConnection(final String name, final RecordStore dataStore) {
    final RecordStoreConnection connection = new RecordStoreConnection(this, name,
      dataStore);
    addConnection(connection);
  }

  @Override
  protected RecordStoreConnection loadConnection(final File dataStoreFile) {
    final Map<String, ? extends Object> config = JsonMapIoFactory.toMap(dataStoreFile);
    String name = Maps.getString(config, "name");
    if (!Property.hasValue(name)) {
      name = FileUtil.getBaseName(dataStoreFile);
    }
    try {
      final Map<String, Object> connectionProperties = CollectionUtil.get(config, "connection",
        Collections.<String, Object> emptyMap());
      if (connectionProperties.isEmpty()) {
        LoggerFactory.getLogger(getClass()).error(
          "Data store must include a 'connection' map property: " + dataStoreFile);
        return null;
      } else {
        final RecordStoreConnection dataStoreConnection = new RecordStoreConnection(this,
          dataStoreFile.toString(), config);
        addConnection(name, dataStoreConnection);
        return dataStoreConnection;
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Error creating data store from: " + dataStoreFile,
        e);
      return null;
    }
  }

  @Override
  public boolean removeConnection(final RecordStoreConnection connection) {
    if (connection == null) {
      return false;
    } else {
      return removeConnection(connection.getName(), connection);
    }
  }

}
