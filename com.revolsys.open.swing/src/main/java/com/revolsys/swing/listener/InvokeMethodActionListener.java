package com.revolsys.swing.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.parallel.Invoke;

/**
 * An ActionListener that invokes the method on the object when the action is
 * performed.
 *
 * @author Paul Austin
 */
public class InvokeMethodActionListener implements ActionListener {
  private final boolean invokeLater;

  private final Runnable runnable;

  public InvokeMethodActionListener(final boolean invokeLater, final Class<?> clazz,
    final String methodName, final Object... parameters) {
    this.runnable = new InvokeMethodRunnable(clazz, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodActionListener(final boolean invokeLater, final Object object,
    final String methodName, final Object... parameters) {
    this.runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
  }

  public InvokeMethodActionListener(final Class<?> clazz, final String methodName,
    final Object... parameters) {
    this(false, clazz, methodName, parameters);
  }

  public InvokeMethodActionListener(final Object object, final String methodName,
    final Object... parameters) {
    this(false, object, methodName, parameters);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (this.invokeLater) {
      Invoke.later(this.runnable);
    } else {
      this.runnable.run();
    }
  }

}
