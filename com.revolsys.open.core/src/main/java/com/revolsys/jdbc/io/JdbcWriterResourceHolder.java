package com.revolsys.jdbc.io;

import org.springframework.transaction.support.ResourceHolderSupport;

public class JdbcWriterResourceHolder extends ResourceHolderSupport {
  private JdbcWriterImpl writer;

  public JdbcWriterResourceHolder(final JdbcWriterImpl writer) {
    this.writer = writer;
  }

  protected void close() {
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
  }

  public JdbcWriterImpl getWriter() {
    return this.writer;
  }

  public boolean hasWriter() {
    return this.writer != null;
  }

  @Override
  public void released() {
    super.released();
    if (!isOpen()) {
      close();
    }
  }

  public void setWriter(final JdbcWriterImpl writer) {
    this.writer = writer;
  }

  public boolean writerEquals(final JdbcWriterImpl writer) {
    return this.writer == writer;
  }
}
