package com.revolsys.io.map;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.spring.SpringUtil;

public interface MapWriterFactory extends FileIoFactory {
  default MapWriter createMapWriter(final Object source) {
    final Resource resource = IoFactoryRegistry.getResource(source);
    final Writer writer = SpringUtil.getWriter(resource);
    return createMapWriter(writer);
  }

  default MapWriter createMapWriter(final OutputStream out) {
    final Writer writer = FileUtil.createUtf8Writer(out);
    return createMapWriter(writer);
  }

  default MapWriter createMapWriter(final OutputStream out, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(out, charset);
    return createMapWriter(writer);
  }

  MapWriter createMapWriter(final Writer out);
}
