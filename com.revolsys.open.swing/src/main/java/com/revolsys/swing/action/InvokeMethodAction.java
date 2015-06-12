package com.revolsys.swing.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import com.revolsys.i18n.I18nCharSequence;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.AbstractActionMainMenuItemFactory;
import com.revolsys.swing.parallel.Invoke;

public class InvokeMethodAction extends AbstractActionMainMenuItemFactory {

  private static final long serialVersionUID = -5339626097125548212L;

  public static JButton createButton(final CharSequence name, final String toolTip,
    final Icon icon, final Object object, final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, toolTip, icon, true, object,
      methodName, parameters);
    final JButton button = new JButton(action);
    return button;
  }

  public static JButton createButton(final String name, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, object, methodName, parameters);
    final JButton button = new JButton(action);
    return button;
  }

  public static JCheckBoxMenuItem createCheckBoxMenuItem(final String name, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, object, methodName, parameters);
    final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
    return menuItem;
  }

  public static JMenuItem createMenuItem(final String name, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, icon, object, methodName,
      parameters);
    final JMenuItem menuItem = new JMenuItem(action);
    return menuItem;
  }

  public static JMenuItem createMenuItem(final String name, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, object, methodName, parameters);
    final JMenuItem menuItem = new JMenuItem(action);
    return menuItem;
  }

  public static JMenuItem createMenuItem(final String name, final String iconName,
    final Object object, final String methodName, final Object... parameters) {
    final Icon icon = Icons.getIcon(iconName);
    return createMenuItem(name, icon, object, methodName, parameters);
  }

  private boolean invokeLater;

  private Runnable runnable;

  private String iconName;

  public InvokeMethodAction() {
  }

  public InvokeMethodAction(final CharSequence name, final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    this(name, null, invokeLater, object, methodName, parameters);
  }

  public InvokeMethodAction(final CharSequence name, final Icon icon, final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    this.runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
    if (name != null) {
      putValue(NAME, name.toString());
      putValue(SHORT_DESCRIPTION, name.toString());
    }
    if (icon != null) {
      putValue(SMALL_ICON, icon);
    }
    if (name instanceof I18nCharSequence) {
      final I18nCharSequence i18nName = (I18nCharSequence)name;
      i18nName.getI18n().addListener("locale", new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
          putValue(NAME, name.toString());
        }
      });

    }
  }

  public InvokeMethodAction(final CharSequence name, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    this(name, icon, false, object, methodName, parameters);
  }

  public InvokeMethodAction(final CharSequence name, final Object object, final String methodName,
    final Object... parameters) {
    this(name, null, false, object, methodName, parameters);
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final boolean invokeLater, final Object object, final String methodName,
    final Object... parameters) {
    this(name, toolTip, icon, invokeLater, new InvokeMethodRunnable(object, methodName, parameters));
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final boolean invokeLater, final Runnable runnable) {
    this.runnable = runnable;
    this.invokeLater = invokeLater;
    if (name != null) {
      putValue(NAME, name.toString());
    }
    if (toolTip != null) {
      putValue(SHORT_DESCRIPTION, toolTip.toString());
    }
    if (icon != null) {
      putValue(SMALL_ICON, icon);
    }
    if (name instanceof I18nCharSequence) {
      final I18nCharSequence i18nName = (I18nCharSequence)name;
      i18nName.getI18n().addListener("locale", new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
          putValue(NAME, name.toString());
        }
      });

    }
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final Object object, final String methodName,
    final Object... parameters) {
    this(name, toolTip, icon, true, object, methodName, parameters);
    setEnableCheck(enableCheck);
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final Object object, final String methodName, final Object... parameters) {
    this(name, toolTip, icon, true, object, methodName, parameters);
  }

  public InvokeMethodAction(final Icon icon, final boolean invokeLater, final Object object,
    final String methodName, final Object... parameters) {
    this(null, icon, invokeLater, object, methodName, parameters);
  }

  public InvokeMethodAction(final Icon icon, final Object object, final String methodName,
    final Object... parameters) {
    this(null, icon, false, object, methodName, parameters);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (this.invokeLater) {
      Invoke.later(this.runnable);
    } else {
      this.runnable.run();
    }
  }

  @Override
  public String getIconName() {
    return this.iconName;
  }

  public void setIconName(final String iconName) {
    this.iconName = iconName;
  }

  @Override
  public String toString() {
    return this.runnable.toString();
  }

}
