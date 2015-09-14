package com.revolsys.jdbc.field;

import java.io.File;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.datatype.DataTypes;
import com.revolsys.jdbc.LocalBlob;
import com.revolsys.record.Record;
import com.revolsys.spring.resource.FileSystemResource;

public class JdbcBlobFieldDefinition extends JdbcFieldDefinition {
  public JdbcBlobFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.BLOB, sqlType, length, 0, required, description, properties);
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record record) throws SQLException {
    final Blob value = resultSet.getBlob(columnIndex);
    setValue(record, value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      Blob blob;
      if (value instanceof Resource) {
        final Resource resource = (Resource)value;
        blob = new LocalBlob(resource);
      } else if (value instanceof Blob) {
        blob = (Blob)value;
      } else if (value instanceof byte[]) {
        final byte[] bytes = (byte[])value;
        blob = new LocalBlob(bytes);
      } else if (value instanceof File) {
        final File file = (File)value;
        final FileSystemResource resource = new FileSystemResource(file);
        blob = new LocalBlob(resource);
      } else {
        throw new IllegalArgumentException("Not valid for a blob column");
      }
      statement.setBlob(parameterIndex, blob);

    }
    return parameterIndex + 1;
  }
}
