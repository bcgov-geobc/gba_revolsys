package com.revolsys.record.schema;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.PathName;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.Transactionable;
import com.vividsolutions.jts.geom.Geometry;

public interface RecordStore extends RecordDefinitionFactory, Transactionable, Closeable {
  void addCodeTable(CodeTable codeTable);

  void addCodeTables(Collection<CodeTable> codeTables);

  void addStatistic(String name, Record object);

  void addStatistic(String name, String typePath, int count);

  @Override
  void close();

  Record copy(Record record);

  default Identifier createPrimaryIdValue(final PathName typePath) {
    final Object id = createPrimaryIdValue(typePath.toString());
    return Identifier.create(id);
  }

  <T> T createPrimaryIdValue(String typePath);

  Query createQuery(final String typePath, String whereClause, final BoundingBox boundingBox);

  @Deprecated
  default Transaction createTransaction(final Propagation propagation) {
    return newTransaction(propagation);
  }

  Record createWithId(RecordDefinition recordDefinition);

  Writer<Record> createWriter();

  int delete(Query query);

  void delete(Record object);

  default int delete(final String typePath, final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition != null) {
      final String idFieldName = recordDefinition.getIdFieldName();
      if (idFieldName != null) {
        final Query query = Query.equal(recordDefinition, idFieldName, identifier);
        return delete(query);
      }
    }
    return 0;
  }

  void deleteAll(Collection<Record> objects);

  <V extends CodeTable> V getCodeTable(String typePath);

  CodeTable getCodeTableByFieldName(String fieldName);

  Map<String, CodeTable> getCodeTableByFieldNameMap();

  RecordStoreConnected getConnected();

  GeometryFactory getGeometryFactory();

  String getLabel();

  default RecordDefinition getRecordDefinition(final PathName typePath) {
    return getRecordDefinition(typePath.toString());
  }

  RecordDefinition getRecordDefinition(RecordDefinition recordDefinition);

  /**
   * Get the meta data for the specified type.
   *
   * @param typePath The type name.
   * @return The meta data.
   */
  @Override
  RecordDefinition getRecordDefinition(String typePath);

  RecordFactory getRecordFactory();

  RecordStoreSchema getRootSchema();

  int getRowCount(Query query);

  RecordStoreSchema getSchema(final String schemaName);

  StatisticsMap getStatistics();

  Statistics getStatistics(String string);

  @Override
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

  default Record load(final PathName typePath, final Identifier id) {
    return load(typePath.toString(), id.getValue(0));
  }

  default Record load(final PathName typePath, final Object id) {
    return load(typePath.toString(), id);
  }

  Record load(String typePath, Object... id);

  Record lock(String typePath, Object id);

  default Identifier newPrimaryIdentifier(final String typePath) {
    final Object id = createPrimaryIdValue(typePath);
    return Identifier.create(id);
  }

  default Record newRecord(final PathName typePath) {
    return newRecord(typePath.toString());
  }

  Record newRecord(RecordDefinition recordDefinition);

  Record newRecord(String typePath);

  Record newRecord(String typePath, Map<String, ? extends Object> values);

  ResultPager<Record> page(Query query);

  RecordReader query(List<?> queries);

  RecordReader query(Query... queries);

  RecordReader query(RecordFactory recordFactory, String typePath, Geometry geometry);

  RecordReader query(RecordFactory recordFactory, String typePath, Geometry geometry,
    double distance);

  RecordReader query(String typePath);

  RecordReader query(String typePath, Geometry geometry);

  RecordReader query(String typePath, Geometry geometry, double distance);

  Record queryFirst(Query query);

  void setLabel(String label);

  void setLogCounts(boolean logCounts);

  void setRecordFactory(RecordFactory recordFactory);

  void update(Record object);

  void updateAll(Collection<Record> objects);
}
