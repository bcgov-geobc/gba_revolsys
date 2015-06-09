package com.revolsys.io.shp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.core.io.FileSystemResource;
import com.revolsys.util.Property;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordIo;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Path;
import com.revolsys.io.Writer;
import com.revolsys.io.xbase.XbaseDataObjectWriter;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;

public class ShapeDirectoryWriter extends AbstractWriter<Record> {
  private File directory;

  private boolean useZeroForNull = true;

  private Map<String, Writer<Record>> writers = new HashMap<String, Writer<Record>>();

  private boolean useNamespaceAsSubDirectory;

  private Statistics statistics;

  private String nameSuffix = "";

  public ShapeDirectoryWriter() {
  }

  public ShapeDirectoryWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  @PreDestroy
  public void close() {
    if (this.writers != null) {
      for (final Writer<Record> writer : this.writers.values()) {
        try {
          writer.close();
        } catch (final RuntimeException e) {
          e.printStackTrace();
        }
      }
      this.writers = null;
    }
    if (this.statistics != null) {
      this.statistics.disconnect();
      this.statistics = null;
    }
  }

  @Override
  public void flush() {
    for (final Writer<Record> writer : this.writers.values()) {
      try {
        writer.flush();
      } catch (final RuntimeException e) {
        e.printStackTrace();
      }
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  private File getDirectory(final RecordDefinition metaData) {
    if (this.useNamespaceAsSubDirectory) {
      final String typePath = metaData.getPath();
      final String schemaName = Path.getPath(typePath);
      if (Property.hasValue(schemaName)) {
        final File childDirectory = new File(this.directory, schemaName);
        if (!childDirectory.mkdirs()) {
          if (!childDirectory.isDirectory()) {
            throw new IllegalArgumentException("Unable to create directory " + childDirectory);
          }
        }
        return childDirectory;
      }
    }
    return this.directory;
  }

  private String getFileName(final RecordDefinition metaData) {
    return metaData.getTypeName();
  }

  public String getNameSuffix() {
    return this.nameSuffix;
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  private Writer<Record> getWriter(final Record object) {
    final RecordDefinition metaData = object.getRecordDefinition();
    final String path = metaData.getPath();
    Writer<Record> writer = this.writers.get(path);
    if (writer == null) {
      final File directory = getDirectory(metaData);
      directory.mkdirs();
      final File file = new File(directory, getFileName(metaData) + this.nameSuffix + ".shp");
      writer = RecordIo.recordWriter(metaData, new FileSystemResource(file));
      ((XbaseDataObjectWriter)writer).setUseZeroForNull(this.useZeroForNull);
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null) {
        setProperty(IoConstants.GEOMETRY_FACTORY, GeometryFactory.getFactory(geometry));
      }
      this.writers.put(path, writer);
    }
    return writer;
  }

  public boolean isUseNamespaceAsSubDirectory() {
    return this.useNamespaceAsSubDirectory;
  }

  public boolean isUseZeroForNull() {
    return this.useZeroForNull;
  }

  public void setDirectory(final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
    this.statistics = new Statistics("Write Shape " + baseDirectory.getAbsolutePath());
    this.statistics.connect();
  }

  public void setNameSuffix(final String nameSuffix) {
    this.nameSuffix = nameSuffix;
  }

  public void setStatistics(final Statistics statistics) {
    if (this.statistics != statistics) {
      this.statistics = statistics;
      statistics.connect();
    }
  }

  public void setUseNamespaceAsSubDirectory(final boolean useNamespaceAsSubDirectory) {
    this.useNamespaceAsSubDirectory = useNamespaceAsSubDirectory;
  }

  public void setUseZeroForNull(final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  @Override
  public String toString() {
    return this.directory.getAbsolutePath();
  }

  @Override
  public void write(final Record object) {
    final Writer<Record> writer = getWriter(object);
    writer.write(object);
    this.statistics.add(object);
  }

}
