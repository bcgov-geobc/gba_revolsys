package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextArea;

import com.revolsys.util.Property;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;

public class TextArea extends JTextArea implements Field, FocusListener {
  private static final long serialVersionUID = 1L;

  private final String fieldName;

  private String fieldValue;

  private String errorMessage;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public TextArea() {
    this("fieldValue");
  }

  public TextArea(final int rows, final int columns) {
    this("text", rows, columns);
  }

  public TextArea(final String fieldName) {
    this(fieldName, "");
  }

  public TextArea(final String fieldName, final int rows, final int columns) {
    this(fieldName, "", rows, columns);
  }

  public TextArea(final String fieldName, final Object fieldValue) {
    this(fieldName, fieldValue, 0, 0);
  }

  public TextArea(final String fieldName, final Object fieldValue, final int rows, final int columns) {
    setRows(rows);
    setColumns(columns);
    this.fieldName = fieldName;
    this.fieldValue = StringConverterRegistry.toString(fieldValue);
    setDocument(new PropertyChangeDocument(this));
    setText(this.fieldValue);
    addFocusListener(new WeakFocusListener(this));
    PopupMenu.getPopupMenuFactory(this);
    this.undoManager.addKeyMap(this);
    setRows(rows);
    setColumns(columns);
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final String text = getText();
    setFieldValue(text);
  }

  @Override
  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.errorMessage;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)getText();
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    setForeground(foregroundColor);
    setSelectedTextColor(foregroundColor);
    setBackground(backgroundColor);
    this.errorMessage = message;
    super.setToolTipText(this.errorMessage);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    final String newValue = StringConverterRegistry.toString(value);
    final String oldValue = this.fieldValue;
    if (!EqualsRegistry.equal(getText(), newValue)) {
      setText(newValue);
    }
    if (!EqualsRegistry.equal(oldValue, value)) {
      this.fieldValue = (String)value;
      firePropertyChange(this.fieldName, oldValue, value);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this, oldValue, value);
    }
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!Property.hasValue(this.errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.undoManager.setParent(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(getText());
  }
}
