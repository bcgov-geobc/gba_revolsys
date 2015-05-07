package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jdbc.io.JdbcQueryResultPager;

public class OracleJdbcQueryResultPager extends JdbcQueryResultPager {

  private Integer numResults;

  private List<Record> results = null;

  public OracleJdbcQueryResultPager(final JdbcDataObjectStore dataStore,
    final Map<String, Object> properties, final Query query) {
    super(dataStore, properties, query);
  }

  @Override
  public List<Record> getList() {
    synchronized (this) {
      if (this.results == null) {
        final ArrayList<Record> results = new ArrayList<Record>();
        final int pageSize = getPageSize();
        final int pageNumber = getPageNumber();
        if (pageNumber != -1) {
          String sql = getSql();

          final int startRowNum = (pageNumber - 1) * pageSize + 1;
          final int endRowNum = startRowNum + pageSize - 1;
          sql = "SELECT * FROM ( SELECT  T2.*, ROWNUM TROWNUM FROM ( " + sql
            + ") T2 ) WHERE TROWNUM BETWEEN " + startRowNum + " AND "
            + endRowNum;

          final DataSource dataSource = getDataSource();
          Connection connection = getConnection();
          if (dataSource != null) {
            connection = JdbcUtils.getConnection(dataSource);
          }
          try {
            final JdbcDataObjectStore dataStore = getDataStore();
            final RecordFactory dataObjectFactory = getDataObjectFactory();
            final RecordDefinition metaData = getMetaData();
            final List<FieldDefinition> attributes = metaData.getFields();

            final PreparedStatement statement = connection.prepareStatement(sql);
            try {
              final ResultSet resultSet = JdbcQueryIterator.getResultSet(
                metaData, statement, getQuery());
              try {
                if (resultSet.next()) {
                  int i = 0;
                  do {
                    final Record object = JdbcQueryIterator.getNextObject(
                      dataStore, metaData, attributes, dataObjectFactory,
                      resultSet);
                    results.add(object);
                    i++;
                  } while (resultSet.next() && i < pageSize);
                }
              } finally {
                JdbcUtils.close(resultSet);
              }
            } finally {
              JdbcUtils.close(statement);
            }
          } catch (final SQLException e) {
            JdbcUtils.getException(dataSource, connection, "updateResults",
              sql, e);
          } finally {
            if (dataSource != null) {
              JdbcUtils.release(connection, dataSource);
            }
          }
        }
        this.results = results;
      }
      return this.results;
    }
  }

  @Override
  public int getNumResults() {
    if (this.numResults == null) {
      final JdbcDataObjectStore dataStore = getDataStore();
      final Query query = getQuery();
      this.numResults = dataStore.getRowCount(query);
      updateNumPages();
    }
    return this.numResults;
  }

  /**
   * Update the cached results for the current page.
   */
  @Override
  protected void updateResults() {
    this.results = null;
  }
}