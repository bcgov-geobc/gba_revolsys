package com.revolsys.io.kml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.spring.SpringUtil;

public class KmlMapIoFactory extends AbstractIoFactory implements
  MapWriterFactory {
  public KmlMapIoFactory() {
    super(Kml22Constants.FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.MEDIA_TYPE,
      Kml22Constants.FILE_EXTENSION);
  }

  @Override
  public MapWriter getWriter(final OutputStream out) {
    final Writer writer = new OutputStreamWriter(out);
    return getWriter(writer);
  }

  @Override
  public MapWriter getWriter(final Resource resource) {
    final Writer writer = SpringUtil.getWriter(resource);
    return getWriter(writer);
  }

  @Override
  public MapWriter getWriter(final Writer out) {
    return new KmlMapWriter(out);
  }

  @Override
  public boolean isCustomAttributionSupported() {
    return true;
  }

  @Override
  public boolean isGeometrySupported() {
    return true;
  }

}
