package com.revolsys.swing.parallel;

import org.slf4j.LoggerFactory;

public class RunnableSwingWorker extends AbstractSwingWorker<Void, Void> {
  private final Runnable backgroundTask;

  private final String description;

  public RunnableSwingWorker(final Runnable backgroundTask) {
    this(backgroundTask.toString(), backgroundTask);
  }

  public RunnableSwingWorker(final String description, final Runnable backgroundTask) {
    this.description = description;
    this.backgroundTask = backgroundTask;
  }

  @Override
  protected Void doInBackground() throws Exception {
    if (this.backgroundTask != null) {
      try {
        this.backgroundTask.run();
      } catch (final Throwable e) {
        LoggerFactory.getLogger(this.backgroundTask.getClass())
          .error("Error running :" + this.description, e);
        throw e;
      }
    }
    return null;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.description;
  }

  @Override
  protected void uiTask() {
  }
}
