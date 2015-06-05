package com.revolsys.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.collection.iterator.AbstractIterator;

public abstract class AbstractMultipleIteratorReader<T> extends
  AbstractReader<T> implements Iterator<T> {

  private AbstractIterator<T> iterator;

  private boolean open;

  private boolean loadNext = true;

  @Override
  @PreDestroy
  public void close() {
    if (iterator != null) {
      iterator.close();
      iterator = null;
    }
  }

  protected abstract AbstractIterator<T> getNextIterator();

  @Override
  public boolean hasNext() {
    if (loadNext) {
      if (iterator == null) {
        iterator = getNextIterator();
        if (iterator == null) {
          close();
          return false;
        }
      }
      while (!iterator.hasNext()) {
        iterator.close();
        iterator = getNextIterator();
        if (iterator == null) {
          return false;
        }
      }
      loadNext = false;
    }
    return true;
  }

  @Override
  public Iterator<T> iterator() {
    open();
    return this;
  }

  @Override
  public T next() {
    if (hasNext()) {
      final T object = iterator.next();
      process(object);
      loadNext = true;
      return object;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void open() {
    if (!open) {
      open = true;
    }
  }

  protected void process(final T object) {
  }

  @Override
  public void remove() {
    iterator.remove();
  }
}
