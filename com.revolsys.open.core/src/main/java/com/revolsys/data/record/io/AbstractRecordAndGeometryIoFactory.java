package com.revolsys.data.record.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.io.DataObjectWriterGeometryWriter;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractRecordAndGeometryIoFactory extends
AbstractRecordAndGeometryReaderFactory implements RecordWriterFactory, GeometryWriterFactory {

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractRecordAndGeometryIoFactory(final String name, final boolean binary,
    final boolean customAttributionSupported) {
    super(name, binary);
    setCustomAttributionSupported(customAttributionSupported);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final Resource resource) {
    final RecordDefinition metaData = DataObjectUtil.createGeometryMetaData();
    final Writer<Record> dataObjectWriter = createRecordWriter(metaData, resource);
    return createGeometryWriter(dataObjectWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName, final OutputStream out) {
    final RecordDefinition metaData = DataObjectUtil.createGeometryMetaData();
    final Writer<Record> dataObjectWriter = createRecordWriter(baseName, metaData, out);
    return createGeometryWriter(dataObjectWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName, final OutputStream out,
    final Charset charset) {
    final RecordDefinition metaData = DataObjectUtil.createGeometryMetaData();
    final Writer<Record> dataObjectWriter = createRecordWriter(baseName, metaData, out, charset);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(final Writer<Record> dataObjectWriter) {
    final Writer<Geometry> geometryWriter = new DataObjectWriterGeometryWriter(dataObjectWriter);
    return geometryWriter;
  }

  /**
   * Create a writer to write to the specified resource.
   *
   * @param metaData The metaData for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  @Override
  public Writer<Record> createRecordWriter(final RecordDefinition metaData, final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createRecordWriter(baseName, metaData, out);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName, final RecordDefinition metaData,
    final OutputStream outputStream) {
    return createRecordWriter(baseName, metaData, outputStream, StandardCharsets.UTF_8);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return this.coordinateSystems;
  }

  @Override
  public boolean isCoordinateSystemSupported(final CoordinateSystem coordinateSystem) {
    return this.coordinateSystems.contains(coordinateSystem);
  }

  @Override
  public boolean isGeometrySupported() {
    return true;
  }

  @Override
  protected void setCoordinateSystems(final CoordinateSystem... coordinateSystems) {
    setCoordinateSystems(new LinkedHashSet<CoordinateSystem>(Arrays.asList(coordinateSystems)));
  }

  @Override
  protected void setCoordinateSystems(final Set<CoordinateSystem> coordinateSystems) {
    this.coordinateSystems = coordinateSystems;
  }
}
