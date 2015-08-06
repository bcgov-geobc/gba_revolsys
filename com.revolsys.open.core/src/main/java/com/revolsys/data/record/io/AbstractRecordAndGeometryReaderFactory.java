package com.revolsys.data.record.io;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.gis.data.io.RecordDirectoryReader;
import com.revolsys.gis.data.io.RecordGeometryIterator;
import com.revolsys.gis.geometry.io.AbstractGeometryReaderFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.Reader;

public abstract class AbstractRecordAndGeometryReaderFactory extends AbstractGeometryReaderFactory
  implements RecordReaderFactory, MapReaderFactory {

  public static RecordReaderFactory getRecordReaderFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final RecordReaderFactory readerFactory = ioFactoryRegistry
      .getFactory(RecordReaderFactory.class, resource);
    return readerFactory;
  }

  private final ArrayRecordFactory recordFactory = new ArrayRecordFactory();

  private boolean singleFile = true;

  private boolean customAttributionSupported = true;

  public AbstractRecordAndGeometryReaderFactory(final String name, final boolean binary) {
    super(name, binary);
  }

  /**
   * Create a directory reader using the ({@link ArrayRecordFactory}).
   *
   * @return The reader.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader() {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    return directoryReader;
  }

  /**
   * Create a reader for the directory using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param directory The directory to read.
   * @return The reader for the file.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader(final File directory) {
    return createDirectoryRecordReader(directory, this.recordFactory);
  }

  /**
   * Create a reader for the directory using the specified data object
   * recordFactory.
   *
   * @param directory directory file to read.
   * @param recordFactory The recordFactory used to create data objects.
   * @return The reader for the file.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader(final File directory,
    final RecordFactory recordFactory) {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final Reader<Record> recordReader = createRecordReader(resource);
    final Iterator<Record> recordIterator = recordReader.iterator();
    final RecordGeometryIterator iterator = new RecordGeometryIterator(recordIterator);
    final GeometryReader geometryReader = new GeometryReader(iterator);
    return geometryReader;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    final Reader reader = createRecordReader(resource);
    return reader;
  }

  /**
   * Create a reader for the resource using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  @Override
  public RecordReader createRecordReader(final Resource resource) {
    return createRecordReader(resource, this.recordFactory);

  }

  @Override
  public boolean isCustomAttributionSupported() {
    return this.customAttributionSupported;
  }

  @Override
  public boolean isSingleFile() {
    return this.singleFile;
  }

  protected void setCustomAttributionSupported(final boolean customAttributionSupported) {
    this.customAttributionSupported = customAttributionSupported;
  }

  protected void setSingleFile(final boolean singleFile) {
    this.singleFile = singleFile;
  }
}
