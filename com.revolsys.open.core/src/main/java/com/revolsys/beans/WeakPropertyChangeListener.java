package com.revolsys.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class WeakPropertyChangeListener implements PropertyChangeListener {

  private final Reference<PropertyChangeListener> listenerReference;

  public WeakPropertyChangeListener(final PropertyChangeListener listener) {
    this.listenerReference = new WeakReference<PropertyChangeListener>(listener);
  }

  public PropertyChangeListener getListener() {
    return this.listenerReference.get();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final PropertyChangeListener listener = getListener();
    if (listener != null) {
      listener.propertyChange(event);
    }
  }
}
