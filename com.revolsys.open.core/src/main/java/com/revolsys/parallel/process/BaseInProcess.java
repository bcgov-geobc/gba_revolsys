package com.revolsys.parallel.process;

import com.revolsys.parallel.ThreadUtil;
import com.revolsys.parallel.channel.Channel;

public class BaseInProcess<T> extends AbstractInProcess<T> {
  private boolean running = false;

  public BaseInProcess() {
  }

  public BaseInProcess(final Channel<T> in) {
    super(in);
  }

  public BaseInProcess(final int bufferSize) {
    super(bufferSize);
  }

  public BaseInProcess(final String processName) {
    super(processName);
  }

  protected void postRun(final Channel<T> in) {
  }

  protected void preRun(final Channel<T> in) {
  }

  protected void process(final Channel<T> in, final T object) {
  }

  @Override
  protected final void run(final Channel<T> in) {
    this.running = true;
    try {
      preRun(in);
      while (this.running) {
        if (ThreadUtil.isInterrupted()) {
          return;
        } else {
          final T object = in.read(5000);
          if (ThreadUtil.isInterrupted()) {
            return;
          } else if (object != null) {
            process(in, object);
          }
        }
      }
    } finally {
      try {
        postRun(in);
      } finally {
        this.running = false;
      }
    }
  }

}
