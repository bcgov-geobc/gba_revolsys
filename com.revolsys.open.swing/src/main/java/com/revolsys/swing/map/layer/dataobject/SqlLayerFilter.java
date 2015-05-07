package com.revolsys.swing.map.layer.dataobject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.revolsys.data.query.Condition;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.Filter;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.UriTemplate;

public class SqlLayerFilter implements Filter<Record>, MapSerializer {
  private final String query;

  private Condition condition;

  private final AbstractRecordLayer layer;

  private boolean initialized;

  public SqlLayerFilter(final AbstractRecordLayer layer, final String query) {
    this.layer = layer;
    this.query = query;
  }

  @Override
  public boolean accept(final Record record) {
    final Condition condition = getCondition();
    if (condition == null) {
      return false;
    } else {
      if (condition.accept(record)) {
        return true;
      } else {
        return false;
      }
    }
  }

  private synchronized Condition getCondition() {
    if (condition == null) {
      if (!initialized) {
        final RecordDefinition metaData = layer.getMetaData();
        if (metaData != null) {
          initialized = true;
          try {
            final Properties properties = System.getProperties();
            final HashMap<String, Object> uriVariables = new HashMap<String, Object>();
            for (final Entry<Object, Object> entry : properties.entrySet()) {
              final String key = (String)entry.getKey();
              final Object value = entry.getValue();
              if (value != null) {
                uriVariables.put(key, value);
              }
            }

            final String query = new UriTemplate(this.query).expandString(uriVariables);
            condition = QueryValue.parseWhere(metaData, query);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(getClass()).error(
              "Invalid query: " + query, e);
          }
        }
      }
    }
    return condition;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "sqlFilter");
    map.put("query", query);
    return map;
  }

  @Override
  public String toString() {
    return query;
  }
}
