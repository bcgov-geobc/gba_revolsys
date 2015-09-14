package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import com.revolsys.datatype.DataType;
import com.revolsys.record.Record;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.ListRecordLayer;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;

public class RecordListLayerTableModel extends RecordLayerTableModel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static RecordLayerTable createTable(final ListRecordLayer layer) {
    final RecordLayerTableModel model = new RecordListLayerTableModel(layer);
    final RecordLayerTable table = new RecordLayerTable(model);

    Property.addListener(layer, "hasSelectedRecords",
      new InvokeMethodListener(RecordLayerTableModel.class, "selectionChanged", table, model));

    return table;
  }

  private ListRecordLayer layer;

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  private List<LayerRecord> records = Collections.emptyList();

  public RecordListLayerTableModel(final ListRecordLayer layer) {
    this(layer, layer.getRecordDefinition().getFieldNames());
  }

  public RecordListLayerTableModel(final ListRecordLayer layer, final List<String> columnNames) {
    super(layer, columnNames);
    this.layer = layer;
    setEditable(false);
    setSortableModes(MODE_SELECTED, MODE_ALL);
  }

  @Override
  public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.layer = null;
  }

  private void firePropertyChange(final Record object, final String name, final Object oldValue,
    final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name, oldValue, newValue);
    for (final PropertyChangeListener listener : this.propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(this.propertyChangeListeners);
  }

  @Override
  public int getRowCountInternal() {
    if (getFieldFilterMode().equals(MODE_ALL)) {
      final Query query = getFilterQuery();
      query.setOrderBy(getOrderBy());
      this.records = this.layer.query(query);
      return this.records.size();
    } else {
      return super.getRowCountInternal();
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final String columnName = getColumnName(columnIndex);
      final RecordDefinition recordDefinition = getRecordDefinition();
      final DataType dataType = recordDefinition.getFieldType(columnName);
      if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  protected LayerRecord loadLayerRecord(final int row) {
    if (row >= 0 && row < this.records.size()) {
      return this.records.get(row);
    } else {
      return null;
    }
  }

  @Override
  public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Record record = getRecord(rowIndex);
    if (record != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = record.getValueByPath(name);
      record.setValue(name, value);
      firePropertyChange(record, name, oldValue, value);
    }
  }
}
