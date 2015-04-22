package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Map;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class Cast extends QueryValue {
  private final QueryValue value;

  private final String dataType;

  public Cast(final QueryValue queryValue, final String dataType) {
    this.value = queryValue;
    this.dataType = dataType;
  }

  public Cast(final String name, final String dataType) {
    this(new Column(name), dataType);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return this.value.appendParameters(index, statement);
  }

  @Override
  public void appendSql(final StringBuilder buffer) {
    buffer.append("CAST(");
    this.value.appendSql(buffer);
    buffer.append(" AS ");
    buffer.append(this.dataType);
    buffer.append(")");
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Cast) {
      final Cast condition = (Cast)obj;
      if (EqualsRegistry.equal(condition.getValue(), this.getValue())) {
        if (EqualsRegistry.equal(condition.getDataType(), this.getDataType())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getDataType() {
    return this.dataType;
  }

  @Override
  public String getStringValue(final Map<String, Object> record) {
    return this.value.getStringValue(record);
  }

  public QueryValue getValue() {
    return this.value;
  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    return this.value.getValue(record);
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("CAST(");
    buffer.append(this.value);
    buffer.append(" AS ");
    buffer.append(this.dataType);
    buffer.append(")");
    return buffer.toString();
  }
}
