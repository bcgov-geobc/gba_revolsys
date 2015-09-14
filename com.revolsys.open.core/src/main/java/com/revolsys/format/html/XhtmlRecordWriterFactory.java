package com.revolsys.format.html;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.RecordDefinition;

public class XhtmlRecordWriterFactory extends AbstractRecordAndGeometryWriterFactory {
  public XhtmlRecordWriterFactory() {
    super("XHMTL", true, true);
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  @Override
  public RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new XhtmlRecordWriter(recordDefinition, writer);
  }
}
