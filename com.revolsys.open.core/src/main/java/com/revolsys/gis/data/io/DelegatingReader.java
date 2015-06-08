package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class DelegatingReader<T> extends AbstractReader<T> {
  private Reader<T> reader;

  private Iterator<T> iterator;

  public DelegatingReader() {
  }

  public DelegatingReader(final Reader<T> reader) {
    this.reader = reader;
  }

  @Override
  public final void close() {
    try {
      if (this.reader != null) {
        this.reader.close();
      }
    } finally {
      doClose();
    }
  }

  protected void doClose() {
    if (this.iterator instanceof AbstractIterator) {
      final AbstractIterator<T> iter = (AbstractIterator<T>)this.iterator;
      iter.close();
    }
  }

  @Override
  public Map<String, Object> getProperties() {
    return this.reader.getProperties();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> C getProperty(final String name) {
    return (C)this.reader.getProperty(name);
  }

  public Reader<T> getReader() {
    return this.reader;
  }

  @Override
  public Iterator<T> iterator() {
    if (this.iterator == null) {
      this.iterator = this.reader.iterator();
    }
    return this.iterator;
  }

  @Override
  public void open() {
    this.reader.open();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    this.reader.setProperty(name, value);
  }

  public void setReader(final Reader<T> reader) {
    this.reader = reader;
  }
}
