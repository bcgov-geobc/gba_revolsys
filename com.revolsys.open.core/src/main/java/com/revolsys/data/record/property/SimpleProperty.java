package com.revolsys.data.record.property;

public class SimpleProperty extends AbstractRecordDefinitionProperty {
  private String propertyName;

  private Object value;

  public SimpleProperty() {
  }

  public SimpleProperty(final String propertyName, final Object value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  @Override
  public SimpleProperty clone() {
    return new SimpleProperty(propertyName, value);
  }

  @Override
  public String getPropertyName() {
    return propertyName;
  }

  public <T> T getValue() {
    return (T)value;
  }

  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(final Object value) {
    this.value = value;
  }
}
