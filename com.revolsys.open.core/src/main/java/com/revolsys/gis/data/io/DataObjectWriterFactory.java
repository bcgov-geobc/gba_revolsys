package com.revolsys.gis.data.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;

public interface DataObjectWriterFactory extends IoFactory {

  Writer<Record> createDataObjectWriter(RecordDefinition metaData,
    Resource resource);

  Writer<Record> createDataObjectWriter(String baseName,
    RecordDefinition metaData, OutputStream outputStream);

  Writer<Record> createDataObjectWriter(String baseName,
    RecordDefinition metaData, OutputStream outputStream, Charset charset);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);

  boolean isCustomAttributionSupported();

  boolean isGeometrySupported();

  boolean isSingleFile();
}
