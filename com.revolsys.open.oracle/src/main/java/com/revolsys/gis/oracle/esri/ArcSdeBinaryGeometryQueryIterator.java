package com.revolsys.gis.oracle.esri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeEnvelope;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.gis.io.Statistics;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class ArcSdeBinaryGeometryQueryIterator extends AbstractIterator<Record> {

  private List<FieldDefinition> attributes = new ArrayList<FieldDefinition>();

  private SeConnection connection;

  private RecordDefinition recordDefinition;

  private Query query;

  private RecordFactory recordFactory;

  private JdbcRecordStore recordStore;

  private ArcSdeBinaryGeometryRecordUtil sdeUtil;

  private SeQuery seQuery;

  private Statistics statistics;

  public ArcSdeBinaryGeometryQueryIterator(final ArcSdeBinaryGeometryRecordUtil sdeUtil,
    final JdbcRecordStore recordStore, final Query query, final Map<String, Object> properties) {
    this.sdeUtil = sdeUtil;
    this.recordFactory = query.getProperty("recordFactory");
    if (this.recordFactory == null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
    this.recordStore = recordStore;
    this.query = query;
    this.statistics = (Statistics)properties.get(Statistics.class.getName());
  }

  @Override
  @PreDestroy
  public void doClose() {
    if (this.sdeUtil != null) {
      try {
        this.seQuery = this.sdeUtil.close(this.seQuery);
      } finally {
        this.connection = this.sdeUtil.close(this.connection);
      }
    }
    this.sdeUtil = null;
    this.attributes = null;
    this.recordFactory = null;
    this.recordStore = null;
    this.recordDefinition = null;
    this.query = null;
    this.seQuery = null;
    this.statistics = null;
  }

  @Override
  protected void doInit() {
    String tableName = this.recordStore.getDatabaseQualifiedTableName(this.query.getTypeName());
    this.recordDefinition = this.query.getRecordDefinition();
    if (this.recordDefinition == null) {
      if (tableName != null) {
        this.recordDefinition = this.recordStore.getRecordDefinition(tableName);
        this.query.setRecordDefinition(this.recordDefinition);

      }
    }
    if (this.recordDefinition != null) {
      tableName = this.sdeUtil.getTableName(this.recordDefinition);
    }
    try {

      final List<String> fieldNames = new ArrayList<String>(this.query.getFieldNames());
      if (fieldNames.isEmpty()) {
        this.attributes.addAll(this.recordDefinition.getFields());
        fieldNames.addAll(this.recordDefinition.getFieldNames());
      } else {
        for (final String fieldName : fieldNames) {
          if (fieldName.equals("*")) {
            this.attributes.addAll(this.recordDefinition.getFields());
            fieldNames.addAll(this.recordDefinition.getFieldNames());
          } else {
            final FieldDefinition attribute = this.recordDefinition.getField(fieldName);
            if (attribute != null) {
              this.attributes.add(attribute);
            }
            fieldNames.add(fieldName);
          }
        }
      }

      this.connection = this.sdeUtil.createSeConnection();
      final SeSqlConstruct sqlConstruct = new SeSqlConstruct(tableName);
      final String[] columnNames = fieldNames.toArray(new String[0]);
      this.seQuery = new SeQuery(this.connection, columnNames, sqlConstruct);
      BoundingBox boundingBox = this.query.getBoundingBox();
      if (boundingBox != null) {
        final SeLayer layer = new SeLayer(this.connection, tableName,
          this.recordDefinition.getGeometryFieldName());

        final GeometryFactory geometryFactory = this.recordDefinition.getGeometryFactory();
        boundingBox = boundingBox.convert(geometryFactory);
        final SeEnvelope envelope = new SeEnvelope(boundingBox.getMinX(), boundingBox.getMinY(),
          boundingBox.getMaxX(), boundingBox.getMaxY());
        final SeShape shape = new SeShape(layer.getCoordRef());
        shape.generateRectangle(envelope);
        final SeShapeFilter filter = new SeShapeFilter(tableName,
          this.recordDefinition.getGeometryFieldName(), shape, SeFilter.METHOD_ENVP);
        this.seQuery.setSpatialConstraints(SeQuery.SE_SPATIAL_FIRST, false, new SeFilter[] {
          filter
        });
      }
      // TODO where clause
      // TODO how to load geometry for non-spatial queries
      this.seQuery.prepareQuery();
      this.seQuery.execute();

      final String typePath = this.query.getTypeNameAlias();
      if (typePath != null) {
        final RecordDefinitionImpl newRecordDefinition = ((RecordDefinitionImpl)this.recordDefinition)
          .rename(typePath);
        this.recordDefinition = newRecordDefinition;
      }
    } catch (final SeException e) {
      this.seQuery = this.sdeUtil.close(this.seQuery);
      throw new RuntimeException("Error performing query", e);
    }
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    try {
      if (this.seQuery != null) {
        final SeRow row = this.seQuery.fetch();
        if (row != null) {
          final Record object = getNextRecord(this.recordDefinition, row);
          if (this.statistics != null) {
            this.statistics.add(object);
          }
          return object;
        }
      }
      close();
      throw new NoSuchElementException();
    } catch (final SeException e) {
      close();
      throw new RuntimeException(this.query.getSql(), e);
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Error e) {
      close();
      throw e;
    }
  }

  private Record getNextRecord(final RecordDefinition recordDefinition, final SeRow row) {
    final Record object = this.recordFactory.createRecord(recordDefinition);
    if (object != null) {
      object.setState(RecordState.Initalizing);
      for (int columnIndex = 0; columnIndex < this.attributes.size(); columnIndex++) {
        this.sdeUtil.setValueFromRow(object, row, columnIndex);
      }
      object.setState(RecordState.Persisted);
      this.recordStore.addStatistic("query", object);
    }
    return object;
  }

  public RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      hasNext();
    }
    return this.recordDefinition;
  }

}
