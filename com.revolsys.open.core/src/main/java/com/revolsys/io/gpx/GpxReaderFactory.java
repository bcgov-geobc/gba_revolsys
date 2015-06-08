package com.revolsys.io.gpx;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.AbstractRecordAndGeometryReaderFactory;
import com.revolsys.data.record.io.RecordIterator;
import com.revolsys.data.record.io.RecordIteratorReader;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.schema.RecordDefinition;

public class GpxReaderFactory extends AbstractRecordAndGeometryReaderFactory {
  public GpxReaderFactory() {
    super("GPS Exchange Format", false);
    addMediaTypeAndFileExtension(GpxConstants.MEDIA_TYPE, GpxConstants.FILE_EXTENSION);
    setCustomAttributionSupported(false);
  }

  public RecordReader createDataObjectReader(final RecordDefinition metaData,
    final Resource resource, final RecordFactory factory) {
    throw new UnsupportedOperationException();
  }

  /**
   * Create a reader for the file using the specified data object factory.
   *
   * @param inputStream The file to read.
   * @param factory The factory used to create data objects.
   * @return The reader for the file.
   */
  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    try {
      final RecordIterator iterator = new GpxIterator(resource, dataObjectFactory, null);
      return new RecordIteratorReader(iterator);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource, e);
    }
  }
}
