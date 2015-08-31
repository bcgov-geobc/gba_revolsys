package com.revolsys.geometry.io;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;

public interface GeometryWriter extends Writer<Geometry> {
  public static GeometryWriter create(final Object source) {
    final GeometryWriterFactory factory = IoFactory.factory(GeometryWriterFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.createGeometryWriter(source);
    }
  }

  public static boolean isWritable(final Object source) {
    return IoFactoryRegistry.isAvailable(GeometryWriterFactory.class, source);
  }
}
