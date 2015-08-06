package com.revolsys.data.query;

import java.util.Map;

import com.revolsys.data.equals.Equals;

public class Equal extends BinaryCondition {

  public Equal(final QueryValue left, final QueryValue right) {
    super(left, "=", right);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return Equals.equal(value1, value2);
  }

  @Override
  public Equal clone() {
    return (Equal)super.clone();
  }

}
