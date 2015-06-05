package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.util.StringUtils;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.data.io.IteratorReader;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.SqlCondition;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordIterator;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.BoundingBox;

public class DataObjectStoreQueryReader extends IteratorReader<Record>
  implements RecordReader {

  private AbstractRecordStore dataStore;

  private List<Query> queries = new ArrayList<Query>();

  private BoundingBox boundingBox;

  private List<String> typePaths;

  private String whereClause;

  public DataObjectStoreQueryReader() {
    setIterator(new DataStoreMultipleQueryIterator(this));
  }

  public DataObjectStoreQueryReader(final AbstractRecordStore dataStore) {
    this();
    setDataStore(dataStore);
  }

  public void addQuery(final Query query) {
    queries.add(query);
  }

  @Override
  @PreDestroy
  public void close() {
    super.close();
    boundingBox = null;
    dataStore = null;
    queries = null;
    typePaths = null;
    whereClause = null;
  }

  protected AbstractIterator<Record> createQueryIterator(final int i) {
    if (i < queries.size()) {
      final Query query = queries.get(i);
      if (StringUtils.hasText(whereClause)) {
        query.and(new SqlCondition(whereClause));
      }
      if (boundingBox != null) {
        query.setBoundingBox(boundingBox);
      }

      final AbstractIterator<Record> iterator = dataStore.createIterator(
        query, getProperties());
      return iterator;
    }
    throw new NoSuchElementException();
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public AbstractRecordStore getDataStore() {
    return dataStore;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return ((RecordIterator)iterator()).getRecordDefinition();
  }

  public List<Query> getQueries() {
    return queries;
  }

  public String getWhereClause() {
    return whereClause;
  }

  @Override
  @PostConstruct
  public void open() {
    if (typePaths != null) {
      for (final String tableName : typePaths) {
        final RecordDefinition metaData = dataStore.getRecordDefinition(tableName);
        if (metaData != null) {
          Query query;
          if (boundingBox == null) {
            query = new Query(metaData);
            query.setWhereCondition(new SqlCondition(whereClause));
          } else {
            query = new Query(metaData);
            query.setBoundingBox(boundingBox);
          }
          addQuery(query);
        }
      }
    }
    super.open();
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setDataStore(final AbstractRecordStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * @param queries the queries to set
   */
  public void setQueries(final Collection<Query> queries) {
    this.queries.clear();
    for (final Query query : queries) {
      addQuery(query);
    }
  }

  public void setQueries(final List<Query> queries) {
    this.queries.clear();
    for (final Query query : queries) {
      addQuery(query);
    }
  }

  /**
   * @param typePaths the typePaths to set
   */
  public void setTypeNames(final List<String> typePaths) {
    this.typePaths = typePaths;

  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }
}
