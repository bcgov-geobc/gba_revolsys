package com.revolsys.data.record.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.data.io.IteratorReader;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.SqlCondition;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.util.Property;

public class RecordStoreQueryReader extends IteratorReader<Record>implements RecordReader {

  private BoundingBox boundingBox;

  private List<Query> queries = new ArrayList<Query>();

  private AbstractRecordStore recordStore;

  private List<String> typePaths;

  private String whereClause;

  public RecordStoreQueryReader() {
    setIterator(new RecordStoreMultipleQueryIterator(this));
  }

  public RecordStoreQueryReader(final AbstractRecordStore recordStore) {
    this();
    setRecordStore(recordStore);
  }

  public void addQuery(final Query query) {
    this.queries.add(query);
  }

  @Override
  @PreDestroy
  public void close() {
    super.close();
    this.boundingBox = null;
    this.recordStore = null;
    this.queries = null;
    this.typePaths = null;
    this.whereClause = null;
  }

  protected AbstractIterator<Record> createQueryIterator(final int i) {
    if (i < this.queries.size()) {
      final Query query = this.queries.get(i);
      if (Property.hasValue(this.whereClause)) {
        query.and(new SqlCondition(this.whereClause));
      }
      if (this.boundingBox != null) {
        query.setBoundingBox(this.boundingBox);
      }

      final AbstractIterator<Record> iterator = this.recordStore.createIterator(query,
        getProperties());
      return iterator;
    }
    throw new NoSuchElementException();
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public List<Query> getQueries() {
    return this.queries;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return ((RecordIterator)iterator()).getRecordDefinition();
  }

  public AbstractRecordStore getRecordStore() {
    return this.recordStore;
  }

  public String getWhereClause() {
    return this.whereClause;
  }

  @Override
  @PostConstruct
  public void open() {
    if (this.typePaths != null) {
      for (final String tableName : this.typePaths) {
        final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(tableName);
        if (recordDefinition != null) {
          Query query;
          if (this.boundingBox == null) {
            query = new Query(recordDefinition);
            query.setWhereCondition(new SqlCondition(this.whereClause));
          } else {
            query = new Query(recordDefinition);
            query.setBoundingBox(this.boundingBox);
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

  public void setRecordStore(final AbstractRecordStore recordStore) {
    this.recordStore = recordStore;
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
