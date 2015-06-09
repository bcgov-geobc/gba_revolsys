package com.revolsys.swing.field;

import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.util.Property;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CollectionUtil;

public class ObjectLabelField extends JLabel implements Field {
  private static final long serialVersionUID = 1L;

  private Object fieldValue;

  private final CodeTable codeTable;

  private final String fieldName;

  private String errorMessage;

  private final Color defaultBackground = getBackground();

  private final Color defaultForeground = getForeground();

  private String originalToolTip;

  public ObjectLabelField() {
    this("fieldValue", null);
  }

  public ObjectLabelField(final String fieldName) {
    this(fieldName, null);
  }

  public ObjectLabelField(final String fieldName, final CodeTable codeTable) {
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),
      BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    this.fieldName = fieldName;
    this.codeTable = codeTable;
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.errorMessage;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getFieldValue() {
    final Object value = this.fieldValue;
    return (T)value;
  }

  @Override
  public boolean isFieldValid() {
    return !Property.hasValue(this.errorMessage);
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = this.defaultBackground;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = this.defaultForeground;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    setForeground(foregroundColor);
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
    setForeground(this.defaultForeground);
    setBackground(this.defaultBackground);
    this.errorMessage = null;
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object object) {
    final Object oldValue = this.fieldValue;
    this.fieldValue = object;
    String text;
    if (object == null) {
      text = "-";
    } else if (this.codeTable == null) {
      text = StringConverterRegistry.toString(object);
    } else {
      final List<Object> values = this.codeTable.getValues(object);
      if (values == null || values.isEmpty()) {
        text = "-";
      } else {
        text = CollectionUtil.toString(values);
      }
    }
    setText(text);
    firePropertyChange(this.fieldName, oldValue, object);
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
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
  }

}
