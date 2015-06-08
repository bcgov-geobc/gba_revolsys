package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jdbc.field.JdbcFieldDefinition;

// TODO accept (how?)
public class SqlCondition extends Condition {
  private List<FieldDefinition> parameterAttributes = new ArrayList<FieldDefinition>();

  private List<Object> parameterValues = new ArrayList<Object>();

  private final String sql;

  public SqlCondition(final String sql) {
    this.sql = sql;
  }

  public SqlCondition(final String sql,
    final FieldDefinition parameterAttribute, final Object parameterValue) {
    this(sql, Arrays.asList(parameterAttribute), Arrays.asList(parameterValue));
  }

  public SqlCondition(final String sql,
    final List<FieldDefinition> parameterAttributes,
    final List<Object> parameterValues) {
    this.sql = sql;
    this.parameterValues = new ArrayList<Object>(parameterValues);
    this.parameterAttributes = new ArrayList<FieldDefinition>(
      parameterAttributes);
  }

  public SqlCondition(final String sql, final Object... parameters) {
    this.sql = sql;
    addParameters(parameters);
  }

  public void addParameter(final Object value) {
    this.parameterValues.add(value);
    this.parameterAttributes.add(null);
  }

  public void addParameter(final Object value, final FieldDefinition attribute) {
    addParameter(value);
    this.parameterAttributes.set(this.parameterAttributes.size() - 1, attribute);
  }

  public void addParameters(final List<Object> parameters) {
    for (final Object parameter : parameters) {
      addParameter(parameter);
    }
  }

  public void addParameters(final Object... parameters) {
    addParameters(Arrays.asList(parameters));
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (int i = 0; i < this.parameterValues.size(); i++) {
      final Object value = this.parameterValues.get(i);
      JdbcFieldDefinition jdbcAttribute = null;
      if (i < this.parameterAttributes.size()) {
        final FieldDefinition attribute = this.parameterAttributes.get(i);
        if (attribute instanceof JdbcFieldDefinition) {
          jdbcAttribute = (JdbcFieldDefinition)attribute;

        }
      }

      if (jdbcAttribute == null) {
        jdbcAttribute = JdbcFieldDefinition.createField(value);
      }
      try {
        index = jdbcAttribute.setPreparedStatementValue(statement, index, value);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to set value: " + value, e);
      }
    }
    return index;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append(this.sql);
  }

  @Override
  public SqlCondition clone() {
    return new SqlCondition(this.sql, this.parameterAttributes,
      this.parameterValues);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SqlCondition) {
      final SqlCondition sqlCondition = (SqlCondition)obj;
      if (EqualsRegistry.equal(sqlCondition.getSql(), this.getSql())) {
        if (EqualsRegistry.equal(sqlCondition.getParameterValues(),
          this.getParameterValues())) {
          return true;
        }
      }
    }
    return false;
  }

  public List<Object> getParameterValues() {
    return this.parameterValues;
  }

  public String getSql() {
    return this.sql;
  }

  @Override
  public String toString() {
    return getSql() + ": " + getParameterValues();
  }
}
