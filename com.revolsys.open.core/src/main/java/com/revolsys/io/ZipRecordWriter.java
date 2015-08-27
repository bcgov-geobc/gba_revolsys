package com.revolsys.io;

import java.io.File;
import java.io.OutputStream;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordWriter;
import com.revolsys.data.record.schema.RecordDefinition;

public class ZipRecordWriter extends ZipWriter<Record>implements RecordWriter {

  public ZipRecordWriter(final File tempDirectory, final RecordWriter writer,
    final OutputStream out) {
    super(tempDirectory, writer, out);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    final RecordWriter writer = getWriter();
    return writer.getRecordDefinition();
  }

  @Override
  public RecordWriter getWriter() {
    return (RecordWriter)super.getWriter();
  }

}