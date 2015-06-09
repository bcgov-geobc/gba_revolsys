package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class BinaryCondition extends Condition {

  private QueryValue left;

  private final String operator;

  private QueryValue right;

  public BinaryCondition(final QueryValue left, final String operator, final QueryValue right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public BinaryCondition(final String name, final String operator, final Object value) {
    this(new Column(name), operator, new Value(value));
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.left != null) {
      index = this.left.appendParameters(index, statement);
    }
    if (this.right != null) {
      index = this.right.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    if (this.left == null) {
      buffer.append("NULL");
    } else {
      this.left.appendSql(buffer);
    }
    buffer.append(" ");
    buffer.append(this.operator);
    buffer.append(" ");
    if (this.right == null) {
      buffer.append("NULL");
    } else {
      this.right.appendSql(buffer);
    }
  }

  @Override
  public BinaryCondition clone() {
    final BinaryCondition clone = (BinaryCondition)super.clone();
    clone.left = this.left.clone();
    clone.right = this.right.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BinaryCondition) {
      final BinaryCondition condition = (BinaryCondition)obj;
      if (EqualsRegistry.equal(condition.getLeft(), this.getLeft())) {
        if (EqualsRegistry.equal(condition.getRight(), this.getRight())) {
          if (EqualsRegistry.equal(condition.getOperator(), this.getOperator())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getLeft() {
    return (V)this.left;
  }

  public String getOperator() {
    return this.operator;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.left, this.right);
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getRight() {
    return (V)this.right;
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(this.left) + " " + this.operator + " "
        + StringConverterRegistry.toString(this.right);
  }
}
