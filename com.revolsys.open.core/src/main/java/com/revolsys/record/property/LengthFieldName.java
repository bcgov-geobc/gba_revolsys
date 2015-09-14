package com.revolsys.record.property;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.LineString;

public class LengthFieldName extends AbstractRecordDefinitionProperty {
  public static final String PROPERTY_NAME = LengthFieldName.class.getName() + ".propertyName";

  public static LengthFieldName getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static LengthFieldName getProperty(final RecordDefinition recordDefinition) {
    LengthFieldName property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new LengthFieldName();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static void setRecordLength(final Record object) {
    final LengthFieldName property = getProperty(object);
    property.setLength(object);
  }

  private String fieldName;

  public LengthFieldName() {
  }

  public LengthFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  public void setLength(final Record object) {
    if (Property.hasValue(this.fieldName)) {
      final LineString line = object.getGeometry();
      final double length = line.getLength();
      object.setValue(this.fieldName, length);
    }
  }

  @Override
  public String toString() {
    return "LengthAttribute " + this.fieldName;
  }
}
