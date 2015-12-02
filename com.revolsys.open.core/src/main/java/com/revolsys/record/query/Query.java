package com.revolsys.record.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.io.PathName;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.vividsolutions.jts.geom.Geometry;

public class Query extends BaseObjectWithProperties implements Cloneable {
  private static void addFilter(final Query query, final RecordDefinition recordDefinition,
    final Map<String, ?> filter, final AbstractMultiCondition multipleCondition) {
    if (filter != null && !filter.isEmpty()) {
      for (final Entry<String, ?> entry : filter.entrySet()) {
        final String name = entry.getKey();
        final FieldDefinition attribute = recordDefinition.getField(name);
        if (attribute == null) {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.add(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.add(new In(name, values));
          } else {
            multipleCondition.add(Q.equal(name, value));
          }
        } else {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.add(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.add(new In(attribute, values));
          } else {
            multipleCondition.add(Q.equal(attribute, value));
          }
        }
      }
      query.setWhereCondition(multipleCondition);
    }
  }

  public static Query and(final RecordDefinition recordDefinition, final Map<String, ?> filter) {
    final Query query = new Query(recordDefinition);
    final Condition[] conditions = {};
    final And and = new And(conditions);
    addFilter(query, recordDefinition, filter, and);
    return query;
  }

  public static Query equal(final RecordDefinition recordDefinition, final String name,
    final Object value) {
    final FieldDefinition attribute = recordDefinition.getField(name);
    if (attribute == null) {
      return null;
    } else {
      final Query query = new Query(recordDefinition);
      final Value valueCondition = new Value(attribute, value);
      final BinaryCondition equal = Q.equal(name, valueCondition);
      query.setWhereCondition(equal);
      return query;
    }
  }

  public static Query or(final RecordDefinition recordDefinition, final Map<String, ?> filter) {
    final Query query = new Query(recordDefinition);
    final Condition[] conditions = {};
    final Or or = new Or(conditions);
    addFilter(query, recordDefinition, filter, or);
    return query;
  }

  private BoundingBox boundingBox;

  private List<String> fieldNames = Collections.emptyList();

  private String fromClause;

  private Geometry geometry;

  private int limit = -1;

  private boolean lockResults = false;

  private int offset = 0;

  private Map<String, Boolean> orderBy = new HashMap<String, Boolean>();

  private List<Object> parameters = new ArrayList<Object>();

  private RecordDefinition recordDefinition;

  private String sql;

  private String typeName;

  private String typePathAlias;

  private Condition whereCondition;

  public Query() {
  }

  public Query(final PathName typePath) {
    this(typePath.toString());
  }

  public Query(final RecordDefinition recordDefinition) {
    this(recordDefinition.getPath());
    this.recordDefinition = recordDefinition;
  }

  public Query(final RecordDefinition recordDefinition, final Condition whereCondition) {
    this(recordDefinition);
    this.whereCondition = whereCondition;
  }

  public Query(final String typePath) {
    this.typeName = typePath;
  }

  public Query(final String typeName, final Condition whereCondition) {
    this(typeName);
    this.whereCondition = whereCondition;
  }

  public void addOrderBy(final String column, final boolean ascending) {
    this.orderBy.put(column, ascending);
  }

  @Deprecated
  public void addParameter(final Object value) {
    this.parameters.add(value);
  }

  public void and(final Condition condition) {
    final Condition whereCondition = getWhereCondition();
    if (whereCondition == null) {
      setWhereCondition(condition);
    } else if (whereCondition instanceof And) {
      final And and = (And)whereCondition;
      and.add(condition);
    } else {
      setWhereCondition(new And(whereCondition, condition));
    }
  }

  @Override
  public Query clone() {
    try {
      final Query clone = (Query)super.clone();
      clone.fieldNames = new ArrayList<>(clone.fieldNames);
      clone.parameters = new ArrayList<>(this.parameters);
      clone.orderBy = new HashMap<>(this.orderBy);
      if (this.whereCondition != null) {
        clone.whereCondition = this.whereCondition.clone();
      }
      if (!clone.getFieldNames().isEmpty() || clone.whereCondition != null) {
        clone.sql = null;
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public String getFromClause() {
    return this.fromClause;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public int getLimit() {
    return this.limit;
  }

  public int getOffset() {
    return this.offset;
  }

  public Map<String, Boolean> getOrderBy() {
    return this.orderBy;
  }

  public List<Object> getParameters() {
    return this.parameters;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public String getSql() {
    return this.sql;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public String getTypeNameAlias() {
    return this.typePathAlias;
  }

  public String getWhere() {
    if (this.whereCondition == null) {
      return null;
    } else {
      return this.whereCondition.toFormattedString();
    }
  }

  public Condition getWhereCondition() {
    return this.whereCondition;
  }

  public boolean isLockResults() {
    return this.lockResults;
  }

  public void or(final Condition condition) {
    final Condition whereCondition = getWhereCondition();
    if (whereCondition == null) {
      setWhereCondition(condition);
    } else if (whereCondition instanceof Or) {
      final Or or = (Or)whereCondition;
      or.add(condition);
    } else {
      setWhereCondition(new Or(whereCondition, condition));
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setFieldNames(final List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public void setFieldNames(final String... fieldNames) {
    setFieldNames(Arrays.asList(fieldNames));
  }

  public void setFromClause(final String fromClause) {
    this.fromClause = fromClause;
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
  }

  public void setLimit(final int limit) {
    this.limit = limit;
  }

  public void setLockResults(final boolean lockResults) {
    this.lockResults = lockResults;
  }

  public void setOffset(final int offset) {
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be >= 0");
    }
    this.offset = offset;
  }

  public void setOrderBy(final Map<String, Boolean> orderBy) {
    if (orderBy != this.orderBy) {
      this.orderBy.clear();
      if (orderBy != null) {
        this.orderBy.putAll(orderBy);
      }
    }
  }

  public void setOrderByColumns(final List<String> orderBy) {
    this.orderBy.clear();
    for (final String column : orderBy) {
      this.orderBy.put(column, Boolean.TRUE);
    }
  }

  public void setOrderByColumns(final String... orderBy) {
    setOrderByColumns(Arrays.asList(orderBy));
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  public void setSql(final String sql) {
    this.sql = sql;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  public void setTypeNameAlias(final String typePathAlias) {
    this.typePathAlias = typePathAlias;
  }

  public void setWhere(final String whereCondition) {
    setWhereCondition(Q.sql(whereCondition));
  }

  public void setWhereCondition(final Condition whereCondition) {
    this.whereCondition = whereCondition;
  }

  @Override
  public String toString() {
    try {
      final StringBuffer string = new StringBuffer();
      if (this.sql == null) {
        string.append(JdbcUtils.getSelectSql(this));
      } else {
        string.append(this.sql);
      }
      if (!this.parameters.isEmpty()) {
        string.append(" ");
        string.append(this.parameters);
      }
      return string.toString();
    } catch (final Throwable t) {
      t.printStackTrace();
      return "";
    }
  }
}
