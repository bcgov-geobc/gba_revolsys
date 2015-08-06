package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.data.record.Record;
import com.revolsys.io.DelegatingObjectWithProperties;
import com.vividsolutions.jts.geom.Geometry;

public class RecordGeometryIterator extends DelegatingObjectWithProperties
  implements Iterator<Geometry> {
  private Iterator<Record> iterator;

  public RecordGeometryIterator(final Iterator<Record> iterator) {
    super(iterator);
    this.iterator = iterator;
  }

  @Override
  public void close() {
    super.close();
    this.iterator = null;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public Geometry next() {
    if (this.iterator.hasNext()) {
      final Record record = this.iterator.next();
      return record.getGeometry();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }
}
