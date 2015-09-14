package com.revolsys.record.query;

import java.util.Map;

import com.revolsys.equals.Equals;

public class NotEqual extends BinaryCondition {

  public NotEqual(final QueryValue left, final QueryValue right) {
    super(left, "<>", right);
  }

  @Override
  public NotEqual clone() {
    return (NotEqual)super.clone();
  }

  @Override
  public boolean test(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return !Equals.equal(value1, value2);
  }

}
