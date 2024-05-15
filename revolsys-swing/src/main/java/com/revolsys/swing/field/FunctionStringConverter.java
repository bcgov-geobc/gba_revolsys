package com.revolsys.swing.field;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

public class FunctionStringConverter<T> extends ObjectToStringConverter
  implements ListCellRenderer<T> {

  private int horizontalAlignment = JLabel.LEFT;

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  private final Function<Object, String> function;

  private Object prototypeValue;

  public FunctionStringConverter(final Function<Object, String> function) {
    this.function = function;
  }

  public int getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public Component getListCellRendererComponent(final JList<? extends T> list, final T value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    final String text;
    if (this.prototypeValue == null) {
      text = "";
    } else if (value == this.prototypeValue) {
      text = this.prototypeValue.toString();
    } else {
      text = getPreferredStringForItem(value);
    }
    this.renderer.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
    this.renderer.setText(text);
    this.renderer.setHorizontalAlignment(this.horizontalAlignment);
    return this.renderer;
  }

  @Override
  public String getPreferredStringForItem(final Object item) {
    if (item == null) {
      return "";
    } else {
      return this.function.apply(item);
    }
  }

  public void setHorizontalAlignment(final int horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
  }

  public void setPrototypeValue(final Object prototypeValue) {
    this.prototypeValue = prototypeValue;
  }
}
