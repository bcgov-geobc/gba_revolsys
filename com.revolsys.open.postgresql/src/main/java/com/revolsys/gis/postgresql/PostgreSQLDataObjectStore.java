package com.revolsys.gis.postgresql;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.postgis.PGbox2d;
import org.postgis.Point;
import org.springframework.util.StringUtils;

import com.revolsys.collection.ResultPager;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.data.query.BinaryCondition;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.property.ShortNameProperty;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.data.model.ArrayRecordFactory;
import com.revolsys.io.Path;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.DataStoreIteratorFactory;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class PostgreSQLDataObjectStore extends AbstractJdbcDataObjectStore {

  public static final List<String> POSTGRESQL_INTERNAL_SCHEMAS = Arrays.asList(
    "information_schema", "pg_catalog", "pg_toast_temp_1");

  public static final AbstractIterator<Record> createPostgreSQLIterator(
    final PostgreSQLDataObjectStore dataStore, final Query query,
    final Map<String, Object> properties) {
    return new PostgreSQLJdbcQueryIterator(dataStore, query, properties);
  }

  private boolean useSchemaSequencePrefix = true;

  public PostgreSQLDataObjectStore() {
    this(new ArrayRecordFactory());
  }

  public PostgreSQLDataObjectStore(final RecordFactory dataObjectFactory) {
    super(dataObjectFactory);
    initSettings();
  }

  public PostgreSQLDataObjectStore(final RecordFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  public PostgreSQLDataObjectStore(final DataSource dataSource) {
    super(dataSource);
    initSettings();
  }

  public PostgreSQLDataObjectStore(
    final PostgreSQLDatabaseFactory databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory);
    final DataSource dataSource = databaseFactory.createDataSource(connectionProperties);
    setDataSource(dataSource);
    initSettings();
    setConnectionProperties(connectionProperties);
  }

  @Override
  public String getGeneratePrimaryKeySql(final RecordDefinition metaData) {
    final String sequenceName = getSequenceName(metaData);
    return "nextval('" + sequenceName + "')";
  }

  @Override
  public Object getNextPrimaryKey(final RecordDefinition metaData) {
    final String sequenceName = getSequenceName(metaData);
    return getNextPrimaryKey(sequenceName);
  }

  @Override
  public Object getNextPrimaryKey(final String sequenceName) {
    final String sql = "SELECT nextval(?)";
    try {
      return JdbcUtils.selectLong(getDataSource(), getConnection(), sql,
        sequenceName);
    } catch (final SQLException e) {
      throw new IllegalArgumentException(
        "Cannot create ID for " + sequenceName, e);
    }
  }

  @Override
  public int getRowCount(Query query) {
    BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      final String typePath = query.getTypeName();
      final RecordDefinition metaData = getRecordDefinition(typePath);
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        query = query.clone();
        query.setAttributeNames("count(*))");
        final String geometryAttributeName = metaData.getGeometryFieldName();
        final GeometryFactory geometryFactory = metaData.getGeometryFactory();
        boundingBox = boundingBox.convert(geometryFactory);
        final double x1 = boundingBox.getMinX();
        final double y1 = boundingBox.getMinY();
        final double x2 = boundingBox.getMaxX();
        final double y2 = boundingBox.getMaxY();

        final PGbox2d box = new PGbox2d(new Point(x1, y1), new Point(x2, y2));
        query.and(new BinaryCondition(geometryAttributeName, "&&", box));
      }
    }

    return super.getRowCount(query);
  }

  public String getSequenceName(final RecordDefinition metaData) {
    final String typePath = metaData.getPath();
    final String schema = getDatabaseSchemaName(Path.getPath(typePath));
    final String shortName = ShortNameProperty.getShortName(metaData);
    final String sequenceName;
    if (StringUtils.hasText(shortName)) {
      if (this.useSchemaSequencePrefix) {
        sequenceName = schema + "." + shortName.toLowerCase() + "_seq";
      } else {
        sequenceName = shortName.toLowerCase() + "_seq";
      }
    } else {
      final String tableName = getDatabaseTableName(typePath);
      final String idAttributeName = metaData.getIdFieldName()
          .toLowerCase();
      if (this.useSchemaSequencePrefix) {
        sequenceName = schema + "." + tableName + "_" + idAttributeName
            + "_seq";
      } else {
        sequenceName = tableName + "_" + idAttributeName + "_seq";
      }
    }
    return sequenceName;

  }

  @Override
  @PostConstruct
  public void initialize() {
    super.initialize();
    final JdbcAttributeAdder numberAttributeAdder = new JdbcAttributeAdder(
      DataTypes.DECIMAL);
    addAttributeAdder("numeric", numberAttributeAdder);

    final JdbcAttributeAdder stringAttributeAdder = new JdbcAttributeAdder(
      DataTypes.STRING);
    addAttributeAdder("varchar", stringAttributeAdder);
    addAttributeAdder("text", stringAttributeAdder);
    addAttributeAdder("name", stringAttributeAdder);
    addAttributeAdder("bpchar", stringAttributeAdder);

    final JdbcAttributeAdder longAttributeAdder = new JdbcAttributeAdder(
      DataTypes.LONG);
    addAttributeAdder("int8", longAttributeAdder);
    addAttributeAdder("bigint", longAttributeAdder);
    addAttributeAdder("bigserial", longAttributeAdder);
    addAttributeAdder("serial8", longAttributeAdder);

    final JdbcAttributeAdder intAttributeAdder = new JdbcAttributeAdder(
      DataTypes.INT);
    addAttributeAdder("int4", intAttributeAdder);
    addAttributeAdder("integer", intAttributeAdder);
    addAttributeAdder("serial", intAttributeAdder);
    addAttributeAdder("serial4", intAttributeAdder);

    final JdbcAttributeAdder shortAttributeAdder = new JdbcAttributeAdder(
      DataTypes.SHORT);
    addAttributeAdder("int2", shortAttributeAdder);
    addAttributeAdder("smallint", shortAttributeAdder);

    final JdbcAttributeAdder floatAttributeAdder = new JdbcAttributeAdder(
      DataTypes.FLOAT);
    addAttributeAdder("float4", floatAttributeAdder);

    final JdbcAttributeAdder doubleAttributeAdder = new JdbcAttributeAdder(
      DataTypes.DOUBLE);
    addAttributeAdder("float8", doubleAttributeAdder);
    addAttributeAdder("double precision", doubleAttributeAdder);

    addAttributeAdder("date", new JdbcAttributeAdder(DataTypes.DATE_TIME));

    addAttributeAdder("bool", new JdbcAttributeAdder(DataTypes.BOOLEAN));

    final JdbcAttributeAdder geometryAttributeAdder = new PostgreSQLGeometryAttributeAdder(
      this, getDataSource());
    addAttributeAdder("geometry", geometryAttributeAdder);
    setPrimaryKeySql("select p.table_name,c.column_name from information_schema.table_constraints p join information_schema.key_column_usage c using (constraint_catalog, constraint_schema, constraint_name) where p.constraint_type = 'PRIMARY KEY' and p.table_schema = ?");
    setSchemaPermissionsSql("select distinct t.table_schema as \"SCHEMA_NAME\" "
        + "from information_schema.role_table_grants t  "
        + "where (t.grantee  in (current_user, 'PUBLIC') or "
        + "t.grantee in (select role_name from information_schema.applicable_roles r where r.grantee = current_user)) and "
        + "privilege_type IN ('SELECT', 'INSERT','UPDATE','DELETE') ");
    setTablePermissionsSql("select distinct t.table_schema as \"SCHEMA_NAME\", t.table_name, t.privilege_type as \"PRIVILEGE\", d.description as \"REMARKS\" from information_schema.role_table_grants t join pg_namespace n on t.table_schema = n.nspname join pg_class c on (n.oid = c.relnamespace AND t.table_name = c.relname) left join pg_description d on d.objoid = c.oid "
        + "where t.table_schema = ? and "
        + "(t.grantee  in (current_user, 'PUBLIC') or t.grantee in (select role_name from information_schema.applicable_roles r where r.grantee = current_user)) AND "
        + "privilege_type IN ('SELECT', 'INSERT','UPDATE','DELETE') "
        + "order by t.table_schema, t.table_name, t.privilege_type");
  }

  protected void initSettings() {
    setIteratorFactory(new DataStoreIteratorFactory(
      PostgreSQLDataObjectStore.class, "createPostgreSQLIterator"));
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return POSTGRESQL_INTERNAL_SCHEMAS.contains(schemaName);
  }

  public boolean isUseSchemaSequencePrefix() {
    return this.useSchemaSequencePrefix;
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return new PostgreSQLJdbcQueryResultPager(this, getProperties(), query);
  }

  public void setUseSchemaSequencePrefix(final boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }
}
