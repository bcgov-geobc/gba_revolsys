package com.revolsys.format.esri.gdb.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.io.Writer;

public class EsriGeodatabaseXmlDataObjectWriterFactory extends
  AbstractRecordAndGeometryWriterFactory {
  public EsriGeodatabaseXmlDataObjectWriterFactory() {
    super(EsriGeodatabaseXmlConstants.FORMAT_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(EsriGeodatabaseXmlConstants.MEDIA_TYPE,
      EsriGeodatabaseXmlConstants.FILE_EXTENSION);
    setCoordinateSystems(EpsgCoordinateSystems.getCoordinateSystem(4326));
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName, final RecordDefinition metaData,
    final OutputStream outputStream, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
    return new EsriGeodatabaseXmlDataObjectWriter(metaData, writer);
  }
}
