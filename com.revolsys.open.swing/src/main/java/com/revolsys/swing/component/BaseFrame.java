package com.revolsys.swing.component;

import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import com.revolsys.swing.WindowManager;
import com.revolsys.swing.parallel.Invoke;

public class BaseFrame extends JFrame implements WindowListener {
  private static final long serialVersionUID = 1L;

  public BaseFrame(final String title) throws HeadlessException {
    this(title, true);
  }

  public BaseFrame(final String title, final boolean initialize) throws HeadlessException {
    super(title);
    if (initialize) {
      init();
    }
  }

  @Override
  public void dispose() {
    removeWindowListener(this);
    WindowManager.removeWindow(this);
    super.dispose();
  }

  protected void init() {
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(this);
  }

  @Override
  public void setVisible(final boolean visible) {
    Invoke.later(() -> {
      if (visible) {
        WindowManager.addWindow(this);
      } else {
        WindowManager.removeWindow(this);
      }
      super.setVisible(visible);
    });
  }

  @Override
  public void windowActivated(final WindowEvent e) {
  }

  @Override
  public void windowClosed(final WindowEvent e) {
  }

  @Override
  public void windowClosing(final WindowEvent e) {
    setVisible(false);
  }

  @Override
  public void windowDeactivated(final WindowEvent e) {
  }

  @Override
  public void windowDeiconified(final WindowEvent e) {
  }

  @Override
  public void windowIconified(final WindowEvent e) {
  }

  @Override
  public void windowOpened(final WindowEvent e) {
  }

}
