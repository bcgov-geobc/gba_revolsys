package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.io.AbstractObjectWithProperties;

public abstract class AbstractIterator<T> extends AbstractObjectWithProperties
  implements Iterator<T>, Iterable<T>, AutoCloseable {

  private boolean hasNext = true;

  private boolean initialized;

  private boolean loadNext = true;

  private T object;

  @Override
  @PreDestroy
  public final void close() {
    hasNext = false;
    object = null;
    doClose();
  }

  protected void doClose() {
  }

  protected void doInit() {
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  protected abstract T getNext() throws NoSuchElementException;

  @Override
  public final boolean hasNext() {
    if (hasNext) {
      if (!initialized) {
        init();
      }
      if (loadNext) {
        try {
          object = getNext();
          loadNext = false;
        } catch (final NoSuchElementException e) {
          close();
          hasNext = false;
        }
      }
    }
    return hasNext;
  }

  public synchronized void init() {
    if (!initialized) {
      initialized = true;
      doInit();
    }
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public final T next() {
    if (hasNext()) {
      final T currentObject = object;
      loadNext = true;
      return currentObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  protected void setLoadNext(final boolean loadNext) {
    this.loadNext = loadNext;
    hasNext = true;
  }
}
