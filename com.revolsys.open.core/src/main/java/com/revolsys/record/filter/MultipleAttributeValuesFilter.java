package com.revolsys.record.filter;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.revolsys.equals.EqualsInstance;
import com.revolsys.record.Record;
import com.revolsys.record.Records;

/**
 * Filter records by the value of the property.
 *
 * @author Paul Austin
 */
public class MultipleAttributeValuesFilter implements Predicate<Record> {
  /** The values to match. */
  private Map<String, ? extends Object> values = Collections.emptyMap();

  public MultipleAttributeValuesFilter(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  public Map<String, ? extends Object> getValues() {
    return this.values;
  }

  public void setValues(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  /**
   * Match the property on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean test(final Record object) {
    for (final Entry<String, ? extends Object> entry : this.values.entrySet()) {
      final String fieldName = entry.getKey();
      final Object value = entry.getValue();
      final Object objectValue = Records.getFieldByPath(object, fieldName);
      if (objectValue == null) {
        if (value != null) {
          if (!EqualsInstance.INSTANCE.equals(value, objectValue)) {
            return false;
          }
        }
      } else {
        if (!EqualsInstance.INSTANCE.equals(objectValue, value)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return this.values.toString();
  }

}
