package com.revolsys.data.query;

import java.util.Map;

import com.revolsys.data.equals.Equals;
import com.revolsys.util.Property;

public class ILike extends BinaryCondition {

  public ILike(final QueryValue left, final QueryValue right) {
    super(left, "LIKE", right);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    String value1 = left.getStringValue(record);

    final QueryValue right = getRight();
    String value2 = right.getStringValue(record);

    if (Property.hasValue(value1)) {
      if (Property.hasValue(value2)) {
        value1 = value1.toUpperCase();
        value2 = value2.toUpperCase();
        if (value2.contains("%")) {
          value2 = Like.toPattern(value2);
          if (value1.matches(value2)) {
            return true;
          } else {
            return false;
          }
        } else {
          return Equals.equal(value1, value2);
        }
      } else {
        return false;
      }
    } else {
      return !Property.hasValue(value2);
    }
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    final QueryValue left = getLeft();
    final QueryValue right = getRight();

    buffer.append("UPPER(CAST(");
    if (left == null) {
      buffer.append("NULL");
    } else {
      left.appendSql(buffer);
    }
    buffer.append(" AS VARCHAR(4000))) LIKE UPPER(");
    if (right == null) {
      buffer.append("NULL");
    } else {
      right.appendSql(buffer);
    }
    buffer.append(")");
  }

  @Override
  public ILike clone() {
    return (ILike)super.clone();
  }

}
