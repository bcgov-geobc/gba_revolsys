package com.revolsys.parallel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.Window;

import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import com.revolsys.util.ExceptionUtil;

public abstract class AbstractRunnable implements Runnable {
  private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

  public static Window getActiveWindow() {
    final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager
      .getCurrentKeyboardFocusManager();
    final Window activeWindow = keyboardFocusManager.getActiveWindow();
    if (activeWindow == null) {
      final Window[] windows = Window.getOwnerlessWindows();
      for (final Window window : windows) {
        if (window.isVisible()) {
          return window;
        }
      }
    }
    return activeWindow;
  }

  private static boolean isEventDispatchThread() {
    try {
      return SwingUtilities.isEventDispatchThread();
    } catch (final NullPointerException e) {
      return false;
    }
  }

  private boolean showWaitCursor = false;

  protected void doRun() {

  }

  public boolean isShowWaitCursor() {
    return this.showWaitCursor;
  }

  @Override
  public final void run() {
    try {
      if (isShowWaitCursor() && isEventDispatchThread()) {
        final Window activeWindow = getActiveWindow();
        if (activeWindow == null) {
          doRun();
        } else {
          Component component;
          Component glassPane = null;
          if (activeWindow instanceof RootPaneContainer) {
            final RootPaneContainer container = (RootPaneContainer)activeWindow;
            glassPane = container.getGlassPane();
            glassPane.setVisible(true);
            component = glassPane;
          } else {
            component = activeWindow;
          }

          final Cursor cursor = activeWindow.getCursor();
          try {
            component.setCursor(WAIT_CURSOR);
            doRun();
          } finally {
            if (glassPane != null) {
              glassPane.setVisible(false);
            }
            component.setCursor(cursor);
          }
        }
      } else {
        doRun();
      }
    } catch (final Throwable t) {
      ExceptionUtil.log(getClass(), t);
    }
  }

  public void setShowWaitCursor(final boolean showWaitCursor) {
    this.showWaitCursor = showWaitCursor;
  }
}
