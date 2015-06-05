package com.revolsys.swing.table.dataobject.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.swing.JTable;
import javax.swing.SortOrder;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.revolsys.swing.map.layer.dataobject.AbstractRecordLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.util.Reorderable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListTableModel extends DataObjectRowTableModel implements
  Reorderable {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final AbstractRecordLayer layer) {
    return createPanel(layer.getRecordDefinition(), new ArrayList<LayerDataObject>(),
      layer.getColumnNames());
  }

  public static TablePanel createPanel(final AbstractRecordLayer layer,
    final Collection<LayerDataObject> objects) {
    return createPanel(layer.getRecordDefinition(), objects, layer.getColumnNames());
  }

  public static TablePanel createPanel(final RecordDefinition metaData,
    final Collection<LayerDataObject> objects,
    final Collection<String> attributeNames) {
    final DataObjectListTableModel model = new DataObjectListTableModel(
      metaData, objects, attributeNames);
    final JTable table = new DataObjectRowTable(model);
    return new TablePanel(table);
  }

  public static TablePanel createPanel(final RecordDefinition metaData,
    final List<LayerDataObject> objects, final String... attributeNames) {
    return createPanel(metaData, objects, Arrays.asList(attributeNames));
  }

  private final List<LayerDataObject> records = new ArrayList<LayerDataObject>();

  public DataObjectListTableModel(final RecordDefinition metaData,
    final Collection<LayerDataObject> objects,
    final Collection<String> columnNames) {
    super(metaData, columnNames);
    if (objects != null) {
      this.records.addAll(objects);
    }
    setEditable(true);
  }

  public void add(final int index, final LayerDataObject object) {
    this.records.add(index, object);
    fireTableRowsInserted(index, index + 1);
  }

  public void add(final LayerDataObject... objects) {
    for (final LayerDataObject object : objects) {
      this.records.add(object);
      fireTableRowsInserted(this.records.size() - 1, this.records.size());
    }
  }

  public void addAll(final Collection<LayerDataObject> objects) {
    this.records.clear();
    this.records.addAll(objects);
  }

  public void clear() {
    this.records.clear();
    fireTableDataChanged();
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.records.clear();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Record> V getRecord(final int index) {
    if (index >= 0 && index < this.records.size()) {
      return (V)this.records.get(index);
    } else {
      return null;
    }
  }

  /**
   * @return the records
   */
  public List<LayerDataObject> getRecords() {
    return this.records;
  }

  @Override
  public int getRowCount() {
    return this.records.size();
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final String attributeName = getFieldName(rowIndex, columnIndex);
      if (isReadOnly(attributeName)) {
        return false;
      } else {
        final RecordDefinition metaData = getMetaData();
        final DataType dataType = metaData.getFieldType(attributeName);
        if (dataType == null) {
          return false;
        } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
          return false;
        } else {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  public void remove(final int... rows) {
    final List<LayerDataObject> rowsToRemove = getRecords(rows);
    removeAll(rowsToRemove);
  }

  public void removeAll(final Collection<LayerDataObject> objects) {
    for (final LayerDataObject object : objects) {
      final int row = this.records.indexOf(object);
      if (row != -1) {
        this.records.remove(row);
        fireTableRowsDeleted(row, row + 1);
      }
    }
  }

  public void removeAll(final LayerDataObject... removedFeatures) {
    removeAll(Arrays.asList(removedFeatures));
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final Record object = getRecord(fromIndex);
    if (object instanceof LayerDataObject) {
      final LayerDataObject layerDataObject = (LayerDataObject)object;
      removeAll(layerDataObject);
      add(toIndex, layerDataObject);
      clearSortedColumns();
    }
    firePropertyChange("reorder", false, true);
  }

  /**
   * @param records the records to set
   */
  public void setRecords(final List<LayerDataObject> objects) {
    this.records.clear();
    if (objects != null) {
      this.records.addAll(objects);
    }
    fireTableDataChanged();
  }

  @Override
  public SortOrder setSortOrder(final int column) {
    final SortOrder sortOrder = super.setSortOrder(column);
    if (this.records != null) {
      final String attributeName = getFieldName(column);
      final Comparator<Record> comparitor = new DataObjectAttributeComparator(
        sortOrder == SortOrder.ASCENDING, attributeName);
      Collections.sort(this.records, comparitor);
      fireTableDataChanged();
    }
    return sortOrder;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final Record object = getRecord(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = object.getValueByPath(name);
      object.setValue(name, value);
      final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
        oldValue, value);
      getPropertyChangeSupport().firePropertyChange(event);
    }
  }

}
