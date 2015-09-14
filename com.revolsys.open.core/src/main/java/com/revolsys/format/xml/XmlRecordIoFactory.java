package com.revolsys.format.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.RecordDefinition;

public class XmlRecordIoFactory extends AbstractRecordAndGeometryWriterFactory {
  public XmlRecordIoFactory() {
    super("XML", true, true);
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  @Override
  public RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
    return new XmlRecordWriter(recordDefinition, writer);
  }

}
