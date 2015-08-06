package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.Equals;

public class RightUnaryCondition extends Condition {

  private final QueryValue value;

  private final String operator;

  public RightUnaryCondition(final QueryValue value, final String operator) {
    this.operator = operator;
    this.value = value;
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return this.value.appendParameters(index, statement);
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    this.value.appendSql(buffer);
    buffer.append(" ");
    buffer.append(this.operator);
  }

  @Override
  public RightUnaryCondition clone() {
    return new RightUnaryCondition(this.value.clone(), this.operator);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof RightUnaryCondition) {
      final RightUnaryCondition condition = (RightUnaryCondition)obj;
      if (Equals.equal(condition.getValue(), this.getValue())) {
        if (Equals.equal(condition.getOperator(), this.getOperator())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getOperator() {
    return this.operator;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.singletonList(this.value);
  }

  public QueryValue getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return getValue() + " " + getOperator();
  }
}
