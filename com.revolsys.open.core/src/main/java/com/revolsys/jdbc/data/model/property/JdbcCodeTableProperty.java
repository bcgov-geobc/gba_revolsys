package com.revolsys.jdbc.data.model.property;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.revolsys.data.codes.CodeTableProperty;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.util.Property;

public class JdbcCodeTableProperty extends CodeTableProperty {

  private String insertSql;

  private JdbcRecordStore recordStore;

  private String tableName;

  private boolean useAuditColumns;

  @Override
  public JdbcCodeTableProperty clone() {
    return this;
  }

  @Override
  protected synchronized Object createId(final List<Object> values) {
    try (
      final Connection connection = this.recordStore.getJdbcConnection()) {
      Object id = loadId(values, false);
      boolean retry = true;
      while (id == null) {
        final PreparedStatement statement = connection.prepareStatement(this.insertSql);
        try {
          id = this.recordStore.getNextPrimaryKey(getRecordDefinition());
          int index = 1;
          index = JdbcUtils.setValue(statement, index, id);
          for (int i = 0; i < getValueFieldNames().size(); i++) {
            final Object value = values.get(i);
            index = JdbcUtils.setValue(statement, index, value);
          }
          if (statement.executeUpdate() > 0) {
            return id;
          }
        } catch (final SQLException e) {
          if (retry) {
            retry = false;
            id = loadId(values, false);
          } else {
            throw new RuntimeException(this.tableName + ": Unable to create ID for  " + values, e);
          }
        } finally {
          JdbcUtils.close(statement);
        }
      }
      return id;

    } catch (final SQLException e) {
      throw new RuntimeException(this.tableName + ": Unable to create ID for  " + values, e);
    }

  }

  @Override
  public JdbcRecordStore getRecordStore() {
    return this.recordStore;
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    super.setRecordDefinition(recordDefinition);
    this.recordStore = (JdbcRecordStore)recordDefinition.getRecordStore();
    if (recordDefinition != null) {
      this.tableName = JdbcUtils.getQualifiedTableName(recordDefinition.getPath());

      final List<String> valueAttributeNames = getValueFieldNames();
      String idColumn = recordDefinition.getIdFieldName();
      if (!Property.hasValue(idColumn)) {
        idColumn = recordDefinition.getFieldName(0);
      }
      this.insertSql = "INSERT INTO " + this.tableName + " (" + idColumn;
      for (int i = 0; i < valueAttributeNames.size(); i++) {
        final String columnName = valueAttributeNames.get(i);
        this.insertSql += ", " + columnName;
      }
      if (this.useAuditColumns) {
        this.insertSql += ", WHO_CREATED, WHEN_CREATED, WHO_UPDATED, WHEN_UPDATED";
      }
      this.insertSql += ") VALUES (?";
      for (int i = 0; i < valueAttributeNames.size(); i++) {
        this.insertSql += ", ?";
      }
      if (this.useAuditColumns) {
        if (this.recordStore.getClass()
          .getName()
          .equals("com.revolsys.gis.oracle.io.OracleRecordStore")) {
          this.insertSql += ", USER, SYSDATE, USER, SYSDATE";
        } else {
          this.insertSql += ", current_user, current_timestamp, current_user, current_timestamp";
        }
      }
      this.insertSql += ")";
    }
  }

  public void setUseAuditColumns(final boolean useAuditColumns) {
    this.useAuditColumns = useAuditColumns;
  }

  @Override
  public String toString(final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
