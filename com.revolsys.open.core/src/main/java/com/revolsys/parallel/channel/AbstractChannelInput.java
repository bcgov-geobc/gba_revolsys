package com.revolsys.parallel.channel;

import java.util.Iterator;

public abstract class AbstractChannelInput<T> implements ChannelInput<T> {
  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** The monitor reads must synchronize on */
  private final Object monitor = new Object();

  /** The name of the channel. */
  private String name;

  /** Number of readers connected to the channel. */
  private int numReaders = 0;

  /** The monitor reads must synchronize on */
  private final Object readMonitor = new Object();

  public AbstractChannelInput() {

  }

  public AbstractChannelInput(final String name) {
    this.name = name;
  }

  public void close() {
    closed = true;
  }

  protected abstract T doRead();

  protected abstract T doRead(long timeout);

  public String getName() {
    return name;
  }

  public boolean isClosed() {
    return closed;
  }

  @Override
  public Iterator<T> iterator() {
    return new ChannelInputIterator<T>(this);
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read.
   * 
   * @return The object returned from the Channel.
   */
  @Override
  public T read() {
    synchronized (readMonitor) {
      synchronized (monitor) {
        if (isClosed()) {
          throw new ClosedException();
        }
        return doRead();
      }
    }
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read. If no data is available to be read after the
   * timeout the method will return null.
   * 
   * @param timeout The maximum time to wait in milliseconds.
   * @return The object returned from the Channel.
   */
  @Override
  public T read(final long timeout) {
    synchronized (readMonitor) {
      synchronized (monitor) {
        if (isClosed()) {
          throw new ClosedException();
        }
        return doRead(timeout);
      }
    }
  }

  @Override
  public void readConnect() {
    synchronized (monitor) {
      if (isClosed()) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        numReaders++;
      }

    }
  }

  @Override
  public void readDisconnect() {
    synchronized (monitor) {
      if (!closed) {
        numReaders--;
        if (numReaders <= 0) {
          close();
          monitor.notifyAll();
        }
      }

    }
  }

  @Override
  public String toString() {
    if (name == null) {
      return super.toString();
    } else {
      return name;
    }
  }
}
