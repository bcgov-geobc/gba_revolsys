package com.revolsys.format.geojson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class GeoJsonIoFactory extends AbstractRecordAndGeometryWriterFactory implements
  GeometryReaderFactory {

  public GeoJsonIoFactory() {
    super(GeoJsonConstants.DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(GeoJsonConstants.MEDIA_TYPE, GeoJsonConstants.FILE_EXTENSION);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    try {
      final GeoJsonGeometryIterator iterator = new GeoJsonGeometryIterator(resource);
      return new GeometryReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName, final RecordDefinition metaData,
    final OutputStream outputStream, final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new GeoJsonRecordWriter(writer);
  }

  @Override
  public boolean isBinary() {
    return false;
  }
}
