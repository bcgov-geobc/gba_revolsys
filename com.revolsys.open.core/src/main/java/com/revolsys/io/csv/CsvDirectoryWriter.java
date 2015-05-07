package com.revolsys.io.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;

public class CsvDirectoryWriter extends AbstractWriter<Record> {
  private File directory;

  private final Map<RecordDefinition, CsvDataObjectWriter> writers = new HashMap<RecordDefinition, CsvDataObjectWriter>();

  public CsvDirectoryWriter() {
  }

  public CsvDirectoryWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  public void close() {
    for (final CsvDataObjectWriter writer : writers.values()) {
      try {
        writer.close();
      } catch (final RuntimeException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void flush() {
    for (final CsvDataObjectWriter writer : writers.values()) {
      writer.flush();
    }
  }

  public File getDirectory() {
    return directory;
  }

  private CsvDataObjectWriter getWriter(final Record object) {
    final RecordDefinition metaData = object.getRecordDefinition();
    CsvDataObjectWriter writer = writers.get(metaData);
    if (writer == null) {
      try {

        final File file = new File(directory, metaData.getPath().toString()
          + ".csv");
        writer = new CsvDataObjectWriter(metaData, new FileWriter(file));
        final Geometry geometry = object.getGeometryValue();
        if (geometry != null) {
          writer.setProperty(IoConstants.GEOMETRY_FACTORY,
            GeometryFactory.getFactory(geometry));
        }
        writers.put(metaData, writer);
      } catch (final IOException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
    return writer;
  }

  public void setDirectory(final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }

  @Override
  public String toString() {
    return directory.getAbsolutePath();
  }

  @Override
  public void write(final Record object) {

    final CsvDataObjectWriter writer = getWriter(object);
    writer.write(object);
  }

}
