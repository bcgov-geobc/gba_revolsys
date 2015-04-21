package com.revolsys.jdbc.attribute;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;

public class JdbcBigDecimalAttribute extends JdbcDecimalAttribute {
  public JdbcBigDecimalAttribute(final String name, final int sqlType,
    final int length, final int scale, final boolean required,
    final String description, final Map<String, Object> properties) {
    super(name, sqlType, length, scale, required, description, properties);
  }

  @Override
  public JdbcBigDecimalAttribute clone() {
    return new JdbcBigDecimalAttribute(getName(), getSqlType(), getLength(),
      getScale(), isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final BigDecimal value = resultSet.getBigDecimal(columnIndex);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }
}
