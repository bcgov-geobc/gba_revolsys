package com.revolsys.io.map;

import java.util.Map;

import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;

public interface MapReader extends Reader<Map<String, Object>> {
  static MapReader create(final Object source) {
    final MapReaderFactory factory = IoFactory.factory(MapReaderFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.createMapReader(source);
    }
  }

  static boolean isReadable(final Object source) {
    return IoFactoryRegistry.isAvailable(MapReaderFactory.class, source);
  }
}