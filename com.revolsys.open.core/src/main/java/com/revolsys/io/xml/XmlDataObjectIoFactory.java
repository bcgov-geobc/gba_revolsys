package com.revolsys.io.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;

public class XmlDataObjectIoFactory extends
  AbstractRecordAndGeometryWriterFactory {
  public XmlDataObjectIoFactory() {
    super("XML", true, true);
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);
    return new XmlDataObjectWriter(metaData, writer);
  }

}
