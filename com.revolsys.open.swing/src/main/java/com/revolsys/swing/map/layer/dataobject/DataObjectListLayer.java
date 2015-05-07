package com.revolsys.swing.map.layer.dataobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTable;
import com.revolsys.swing.map.layer.dataobject.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectListLayerTableModel;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListLayer extends AbstractRecordLayer {

  public static RecordDefinitionImpl createMetaData(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    final RecordDefinitionImpl metaData = new RecordDefinitionImpl(name);
    metaData.addField("GEOMETRY", geometryType, true);
    metaData.setGeometryFactory(geometryFactory);
    return metaData;
  }

  private final List<LayerDataObject> records = new ArrayList<LayerDataObject>();

  public DataObjectListLayer() {
  }

  public DataObjectListLayer(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public DataObjectListLayer(final RecordDefinition metaData) {
    super(metaData);
    setEditable(true);
  }

  public DataObjectListLayer(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    super(name);
    final RecordDefinitionImpl metaData = createMetaData(name, geometryFactory,
      geometryType);
    setRecordDefinition(metaData);
  }

  @Override
  public LayerDataObject createRecord(final Map<String, Object> values) {
    final LayerDataObject record = super.createRecord(values);
    addToIndex(record);
    fireEmpty();
    return record;
  }

  protected void createRecordInternal(final Map<String, Object> values) {
    final LayerDataObject record = createRecord(getMetaData());
    record.setState(RecordState.Initalizing);
    try {
      record.setValues(values);
    } finally {
      record.setState(RecordState.Persisted);
    }
    synchronized (this.records) {
      this.records.add(record);
    }
    addToIndex(record);
  }

  @Override
  public RecordLayerTablePanel createTablePanel() {
    final DataObjectLayerTable table = DataObjectListLayerTableModel.createTable(this);
    return new RecordLayerTablePanel(this, table);
  }

  @Override
  public void deleteRecord(final LayerDataObject record) {
    this.records.remove(record);
    super.deleteRecord(record);
    saveChanges(record);
    fireEmpty();
  }

  @Override
  public void deleteRecords(final Collection<? extends LayerDataObject> records) {
    if (isCanDeleteRecords()) {
      super.deleteRecords(records);
      synchronized (this.records) {
        this.records.removeAll(records);
      }
      removeFromIndex(records);
      fireRecordsChanged();
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public List<LayerDataObject> doQuery(final BoundingBox boundingBox) {
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();
    if (boundingBox.isEmpty() || width == 0 || height == 0) {
      return Collections.emptyList();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
      final List<LayerDataObject> records = (List)getIndex().queryIntersects(
        convertedBoundingBox);
      return records;
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public List<LayerDataObject> doQuery(Geometry geometry, final double distance) {
    geometry = getGeometryFactory().createGeometry(geometry);
    return (List)getIndex().queryDistance(geometry, distance);
  }

  @Override
  protected List<LayerDataObject> doQuery(final Query query) {
    final Condition whereCondition = query.getWhereCondition();
    if (whereCondition == null) {
      return new ArrayList<LayerDataObject>(this.records);
    } else {
      final List<LayerDataObject> records = new ArrayList<LayerDataObject>();
      for (final LayerDataObject record : new ArrayList<LayerDataObject>(
        this.records)) {
        if (whereCondition.accept(record)) {
          records.add(record);
        }
      }
      return records;
    }
  }

  @Override
  protected boolean doSaveChanges(final LayerDataObject record) {
    if (record.isDeleted()) {
      return true;
    } else {
      return super.doSaveChanges(record);
    }
  }

  public void fireEmpty() {
    final boolean empty = isEmpty();
    firePropertyChange("empty", !empty, empty);
  }

  @Override
  protected void fireRecordsChanged() {
    super.fireRecordsChanged();
    fireEmpty();
  }

  @Override
  public BoundingBox getBoundingBox() {
    BoundingBox boundingBox = new BoundingBox(getGeometryFactory());
    for (final LayerDataObject record : getRecords()) {
      boundingBox = boundingBox.expandToInclude(record);
    }
    return boundingBox;
  }

  @Override
  public int getNewRecordCount() {
    return 0;
  }

  @Override
  public LayerDataObject getRecord(final int index) {
    if (index < 0) {
      return null;
    } else {
      synchronized (this.records) {
        return this.records.get(index);
      }
    }
  }

  @Override
  public List<LayerDataObject> getRecords() {
    synchronized (this.records) {
      final ArrayList<LayerDataObject> records = new ArrayList<LayerDataObject>(
        this.records);
      records.addAll(getNewRecords());
      return records;
    }
  }

  @Override
  public int getRowCount() {
    synchronized (this.records) {
      return this.records.size();
    }
  }

  @Override
  public int getRowCount(final Query query) {
    final List<LayerDataObject> results = query(query);
    return results.size();
  }

  @Override
  public boolean isEmpty() {
    return getRowCount() + super.getNewRecordCount() <= 0;
  }

}
