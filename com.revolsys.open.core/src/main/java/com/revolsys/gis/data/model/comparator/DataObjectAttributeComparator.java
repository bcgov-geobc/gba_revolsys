package com.revolsys.gis.data.model.comparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.revolsys.data.record.Record;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.util.CompareUtil;

public class DataObjectAttributeComparator implements Comparator<Record> {
  private List<String> attributeNames;

  private boolean invert;

  private boolean nullFirst;

  public DataObjectAttributeComparator() {
  }

  public DataObjectAttributeComparator(final boolean sortAsceding,
    final String... attributeNames) {
    this(Arrays.asList(attributeNames));
    this.invert = !sortAsceding;
  }

  public DataObjectAttributeComparator(final List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public DataObjectAttributeComparator(final String... attributeNames) {
    this(Arrays.asList(attributeNames));
  }

  @Override
  public int compare(final Record object1, final Record object2) {
    for (final String attributeName : attributeNames) {
      final int compare = compare(object1, object2, attributeName);
      if (compare != 0) {
        return compare;
      }
    }

    return 0;
  }

  public int compare(final Record object1, final Record object2,
    final String attributeName) {
    final Comparable<Object> value1 = DataObjectUtil.getAttributeByPath(
      object1, attributeName);
    final Comparable<Object> value2 = DataObjectUtil.getAttributeByPath(
      object2, attributeName);
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        if (nullFirst) {
          return -1;
        } else {
          return 1;
        }
      }
    } else if (value2 == null) {
      if (nullFirst) {
        return 1;
      } else {
        return -1;
      }
    } else {
      final int compare = CompareUtil.compare(value1, value2);
      if (invert) {
        return -compare;
      } else {
        return compare;
      }
    }
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public boolean isInvert() {
    return invert;
  }

  public boolean isNullFirst() {
    return nullFirst;
  }

  public void setAttributeNames(final List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public void setInvert(final boolean invert) {
    this.invert = invert;
  }

  public void setNullFirst(final boolean nullFirst) {
    this.nullFirst = nullFirst;
  }

}
