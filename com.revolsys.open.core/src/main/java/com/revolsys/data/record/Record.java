package com.revolsys.data.record;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.data.record.schema.RecordDefinition;
import com.vividsolutions.jts.geom.Geometry;

public interface Record extends Map<String, Object>, Comparable<Record> {
  /**
   * Create a clone of the data object.
   *
   * @return The data object.
   */
  Record clone();

  void delete();

  Byte getByte(final CharSequence name);

  Double getDouble(final CharSequence name);

  /**
   * Get the factory which created the instance.
   *
   * @return The factory.
   */
  RecordFactory getFactory();

  Float getFloat(final CharSequence name);

  /**
   * Get the value of the primary geometry attribute.
   *
   * @return The primary geometry.
   */
  <T extends Geometry> T getGeometryValue();

  Integer getIdInteger();

  String getIdString();

  /**
   * Get the value of the unique identifier attribute.
   *
   * @return The unique identifier.
   */
  <T extends Object> T getIdValue();

  Integer getInteger(CharSequence name);

  Long getLong(final CharSequence name);

  /**
   * Get the meta data describing the DataObject and it's attributes.
   *
   * @return The meta data.
   */
  RecordDefinition getRecordDefinition();

  Short getShort(final CharSequence name);

  RecordState getState();

  String getString(final CharSequence name);

  String getTypeName();

  /**
   * Get the value of the attribute with the specified name.
   *
   * @param name The name of the attribute.
   * @return The attribute value.
   */
  <T extends Object> T getValue(CharSequence name);

  /**
   * Get the value of the attribute with the specified index.
   *
   * @param index The index of the attribute.
   * @return The attribute value.
   */
  <T extends Object> T getValue(int index);

  <T> T getValueByPath(CharSequence attributePath);

  Map<String, Object> getValueMap(final Collection<? extends CharSequence> attributeNames);

  /**
   * Get the values of all attributes.
   *
   * @return The attribute value.
   */
  List<Object> getValues();

  /**
   * Checks to see if the metadata for this DataObject has an attribute with the
   * specified name.
   *
   * @param name The name of the attribute.
   * @return True if the DataObject has an attribute with the specified name.
   */
  boolean hasAttribute(CharSequence name);

  boolean isModified();

  boolean isValid(int index);

  boolean isValid(String attributeName);

  /**
   * Set the value of the primary geometry attribute.
   *
   * @param geometry The primary geometry.
   */
  void setGeometryValue(Geometry geometry);

  /**
   * Set the value of the unique identifier attribute.
   *
   * @param id The unique identifier.
   */
  void setIdValue(Object id);

  void setState(final RecordState state);

  /**
   * Set the value of the attribute with the specified name.
   *
   * @param name The name of the attribute. param value The attribute value.
   * @param value The new value;
   */
  void setValue(CharSequence name, Object value);

  /**
   * Set the value of the attribute with the specified name.
   *
   * @param index The index of the attribute. param value The attribute value.
   * @param value The new value;
   */
  void setValue(int index, Object value);

  void setValueByPath(CharSequence attributePath, Object value);

  <T> T setValueByPath(CharSequence attributePath, Record source, String sourceAttributePath);

  void setValues(Map<String, ? extends Object> values);

  void setValues(final Record object);

  void setValues(Record object, Collection<String> attributeNames);

  /**
   * Set the values on the object based on the values in the map.
   *
   * @param values The values to set.
   */
  void setValuesByPath(Map<String, ? extends Object> values);

}
