package com.revolsys.swing.field;

import java.awt.Color;

import com.revolsys.swing.undo.UndoManager;

public interface Field {

  void firePropertyChange(String propertyName, Object oldValue, Object newValue);

  String getFieldName();

  String getFieldValidationMessage();

  <T> T getFieldValue();

  boolean isFieldValid();

  void setEnabled(boolean enabled);

  void setFieldBackgroundColor(Color color);

  void setFieldForegroundColor(Color color);

  void setFieldInvalid(String message, Color foregroundColor, Color backgroundColor);

  void setFieldToolTip(String toolTip);

  void setFieldValid();

  void setFieldValue(Object value);

  void setUndoManager(UndoManager undoManager);

  void updateFieldValue();
}
