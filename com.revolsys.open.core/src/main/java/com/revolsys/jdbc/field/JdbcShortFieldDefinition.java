package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;

public class JdbcShortFieldDefinition extends JdbcFieldDefinition {
  public JdbcShortFieldDefinition(final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(name, DataTypes.SHORT, sqlType, length, 0, required, description,
      properties);
  }

  @Override
  public JdbcShortFieldDefinition clone() {
    return new JdbcShortFieldDefinition(getName(), getSqlType(), getLength(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final short value = resultSet.getShort(columnIndex);
    if (!resultSet.wasNull()) {
      object.setValue(getIndex(), Short.valueOf(value));
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      short numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.shortValue();
      } else {
        numberValue = Short.parseShort(value.toString());
      }
      statement.setShort(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
