package com.revolsys.gis.oracle.esri;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class ArcSdeObjectIdJdbcAttribute extends JdbcAttribute {
  public static void replaceAttribute(final String schemaName,
    final RecordDefinition metaData, final Integer registrationId,
    final String rowIdColumn) {
    final JdbcAttribute objectIdAttribute = (JdbcAttribute)metaData.getAttribute(rowIdColumn);
    if (objectIdAttribute != null
        && !(objectIdAttribute instanceof ArcSdeObjectIdJdbcAttribute)) {
      final String name = objectIdAttribute.getName();
      final String description = objectIdAttribute.getDescription();
      final Map<String, Object> properties = objectIdAttribute.getProperties();

      final ArcSdeObjectIdJdbcAttribute newObjectIdAttribute = new ArcSdeObjectIdJdbcAttribute(
        name, description, properties, schemaName, registrationId);
      newObjectIdAttribute.setMetaData(metaData);
      final RecordDefinitionImpl metaDataImpl = (RecordDefinitionImpl)metaData;
      metaDataImpl.replaceAttribute(objectIdAttribute, newObjectIdAttribute);
      if (metaData.getIdFieldName() == null
          && metaData.getIdAttributeNames().isEmpty()) {
        metaDataImpl.setIdFieldName(name);
      }
    }
  }

  /** The SDE.TABLE_REGISTRY REGISTRATION_ID for the table. */
  private final long registrationId;

  /** The name of the database schema the table owned by. */
  private final String schemaName;

  public ArcSdeObjectIdJdbcAttribute(final String name,
    final String description, final Map<String, Object> properties,
    final String schemaName, final long registrationId) {
    super(name, DataTypes.INT, -1, 19, 0, true, description, properties);
    this.schemaName = schemaName;
    this.registrationId = registrationId;
  }

  /**
   * Generate an OBJECT ID using ESRI's sde.version_user_ddl.next_row_id
   * function.
   */
  @Override
  public void addInsertStatementPlaceHolder(final StringBuilder sql,
    final boolean generateKeys) {
    sql.append(" sde.version_user_ddl.next_row_id('");
    sql.append(this.schemaName);
    sql.append("', ");
    sql.append(this.registrationId);
    sql.append(")");
  }

  @Override
  public ArcSdeObjectIdJdbcAttribute clone() {
    return new ArcSdeObjectIdJdbcAttribute(getName(), getDescription(),
      getProperties(), this.schemaName, this.registrationId);
  }

  /**
   * Ignore any inserted value.
   */
  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record object) throws SQLException {
    return parameterIndex;
  }
}
