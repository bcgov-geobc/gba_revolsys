package com.revolsys.data.record.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;

public interface RecordWriterFactory extends IoFactory {

  Writer<Record> createRecordWriter(RecordDefinition recordDefinition, Resource resource);

  Writer<Record> createRecordWriter(String baseName, RecordDefinition recordDefinition,
    OutputStream outputStream);

  Writer<Record> createRecordWriter(String baseName, RecordDefinition recordDefinition,
    OutputStream outputStream, Charset charset);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);

  boolean isCustomAttributionSupported();

  boolean isGeometrySupported();

  boolean isSingleFile();
}
