package com.revolsys.io.map;

import java.util.Map;

import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;

public interface MapWriter extends Writer<Map<String, ? extends Object>> {
  static MapWriter create(final Object source) {
    final MapWriterFactory factory = IoFactory.factory(MapWriterFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.createMapWriter(source);
    }
  }

  static boolean isWritable(final Object source) {
    return IoFactoryRegistry.isAvailable(MapWriterFactory.class, source);
  }
}
