package com.revolsys.data.record.filter;

import java.util.Arrays;
import java.util.Collection;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.filter.Filter;

public class AttributesEqualOrNullFilter implements Filter<Record> {
  public static boolean accept(final Record object1, final Record object2,
    final Collection<String> attributeNames) {
    for (final String attributeName : attributeNames) {
      final Object value1 = Records.getAttributeByPath(object1, attributeName);
      final Object value2 = Records.getAttributeByPath(object2, attributeName);

      if (value1 != null && value2 != null && !EqualsInstance.INSTANCE.equals(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  public static boolean accept(final Record object1, final Record object2,
    final String... attributeNames) {
    return accept(object1, object2, Arrays.asList(attributeNames));
  }

  private final Collection<String> attributeNames;

  private final Record object;

  public AttributesEqualOrNullFilter(final Record object, final Collection<String> attributeNames) {
    this.attributeNames = attributeNames;
    this.object = object;
  }

  public AttributesEqualOrNullFilter(final Record object, final String... attributeNames) {
    this(object, Arrays.asList(attributeNames));
  }

  @Override
  public boolean accept(final Record object) {
    return accept(this.object, object, this.attributeNames);
  }

  @Override
  public String toString() {
    return "AttributeEquals" + this.attributeNames;
  }

}
