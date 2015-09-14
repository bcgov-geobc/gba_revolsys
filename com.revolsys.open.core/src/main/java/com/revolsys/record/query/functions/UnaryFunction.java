package com.revolsys.record.query.functions;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import com.revolsys.equals.Equals;
import com.revolsys.record.query.QueryValue;

public abstract class UnaryFunction extends QueryValue {

  private final String name;

  private final QueryValue parameter;

  public UnaryFunction(final String name, final QueryValue parameter) {
    this.name = name;
    this.parameter = parameter;
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    final QueryValue parameter = getParameter();
    return parameter.appendParameters(index, statement);
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append(getName());
    buffer.append("(");
    final QueryValue parameter = getParameter();
    parameter.appendSql(buffer);
    buffer.append(")");
  }

  @Override
  public UnaryFunction clone() {

    return (UnaryFunction)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof UnaryFunction) {
      final UnaryFunction function = (UnaryFunction)other;
      if (Equals.equal(function.getName(), getName())) {
        if (Equals.equal(function.getParameter(), getParameter())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getName() {
    return this.name;
  }

  public QueryValue getParameter() {
    return this.parameter;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.singletonList(this.parameter);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.name == null ? 0 : this.name.hashCode());
    result = prime * result + (this.parameter == null ? 0 : this.parameter.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return getName() + "(" + getParameter() + ")";
  }
}
