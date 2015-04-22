package com.revolsys.data.record.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.vividsolutions.jts.geom.Geometry;

public interface RecordStore extends DataObjectMetaDataFactory,
  AutoCloseable {
  void addCodeTable(CodeTable codeTable);

  void addCodeTables(Collection<CodeTable> codeTables);

  void addStatistic(String name, Record object);

  void addStatistic(String name, String typePath, int count);

  @Override
  void close();

  Record copy(Record record);

  Record create(RecordDefinition metaData);

  Record create(String typePath);

  <T> T createPrimaryIdValue(String typePath);

  Query createQuery(final String typePath, String whereClause,
    final BoundingBox boundingBox);

  Transaction createTransaction(Propagation propagation);

  Record createWithId(RecordDefinition objectMetaData);

  Record create(String typePath, Map<String, ? extends Object> values);

  Writer<Record> createWriter();

  void delete(Record object);

  int delete(Query query);

  void deleteAll(Collection<Record> objects);

  CodeTable getCodeTable(String typePath);

  CodeTable getCodeTableByFieldName(String columnName);

  Map<String, CodeTable> getCodeTableByColumnMap();

  RecordFactory getRecordFactory();

  String getLabel();

  RecordDefinition getRecordDefinition(RecordDefinition metaData);

  /**
   * Get the meta data for the specified type.
   * 
   * @param typePath The type name.
   * @return The meta data.
   */
  @Override
  RecordDefinition getRecordDefinition(String typePath);

  int getRowCount(Query query);

  RecordStoreSchema getSchema(final String schemaName);

  /**
   * Get the list of name space names provided by the data store.
   * 
   * @return The name space names.
   */
  List<RecordStoreSchema> getSchemas();

  StatisticsMap getStatistics();

  Statistics getStatistics(String string);

  PlatformTransactionManager getTransactionManager();

  /**
   * Get the list of type names (including name space) in the name space.
   * 
   * @param namespace The name space.
   * @return The type names.
   */
  List<String> getTypeNames(String namespace);

  List<RecordDefinition> getTypes(String namespace);

  String getUrl();

  String getUsername();

  Writer<Record> getWriter();

  Writer<Record> getWriter(boolean throwExceptions);

  boolean hasSchema(String name);

  void initialize();

  void insert(Record object);

  void insertAll(Collection<Record> objects);

  boolean isEditable(String typePath);

  Record load(String typePath, Object... id);

  Record lock(String typePath, Object id);

  ResultPager<Record> page(Query query);

  Reader<Record> query(RecordFactory dataObjectFactory,
    String typePath, Geometry geometry);

  Reader<Record> query(RecordFactory dataObjectFactory,
    String typePath, Geometry geometry, double distance);

  Reader<Record> query(List<?> queries);

  Reader<Record> query(Query... queries);

  Reader<Record> query(String typePath);

  Reader<Record> query(String typePath, Geometry geometry);

  Reader<Record> query(String typePath, Geometry geometry, double distance);

  Record queryFirst(Query query);

  void setDataObjectFactory(RecordFactory dataObjectFactory);

  void setLabel(String label);

  void setLogCounts(boolean logCounts);

  void update(Record object);

  void updateAll(Collection<Record> objects);
}
