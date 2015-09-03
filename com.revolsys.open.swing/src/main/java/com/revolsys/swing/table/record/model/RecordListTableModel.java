package com.revolsys.swing.table.record.model;

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

import com.revolsys.data.comparator.RecordFieldComparator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.row.RecordRowTable;
import com.revolsys.util.Reorderable;
import com.vividsolutions.jts.geom.Geometry;

public class RecordListTableModel extends RecordRowTableModel implements Reorderable {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final AbstractRecordLayer layer) {
    return createPanel(layer.getRecordDefinition(), new ArrayList<LayerRecord>(),
      layer.getFieldNames());
  }

  public static TablePanel createPanel(final AbstractRecordLayer layer,
    final Collection<LayerRecord> objects) {
    return createPanel(layer.getRecordDefinition(), objects, layer.getFieldNames());
  }

  public static TablePanel createPanel(final RecordDefinition recordDefinition,
    final Collection<LayerRecord> objects, final Collection<String> attributeNames) {
    final RecordListTableModel model = new RecordListTableModel(recordDefinition, objects,
      attributeNames);
    final JTable table = new RecordRowTable(model);
    return new TablePanel(table);
  }

  public static TablePanel createPanel(final RecordDefinition recordDefinition,
    final List<LayerRecord> objects, final String... attributeNames) {
    return createPanel(recordDefinition, objects, Arrays.asList(attributeNames));
  }

  private final List<LayerRecord> records = new ArrayList<LayerRecord>();

  public RecordListTableModel(final RecordDefinition recordDefinition,
    final Collection<LayerRecord> objects, final Collection<String> columnNames) {
    super(recordDefinition, columnNames);
    if (objects != null) {
      this.records.addAll(objects);
    }
    setEditable(true);
  }

  public void add(final int index, final LayerRecord object) {
    this.records.add(index, object);
    fireTableRowsInserted(index, index + 1);
  }

  public void add(final LayerRecord... objects) {
    for (final LayerRecord object : objects) {
      this.records.add(object);
      fireTableRowsInserted(this.records.size() - 1, this.records.size());
    }
  }

  public void addAll(final Collection<LayerRecord> objects) {
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
  public List<LayerRecord> getRecords() {
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
        final RecordDefinition recordDefinition = getRecordDefinition();
        final DataType dataType = recordDefinition.getFieldType(attributeName);
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
    final List<LayerRecord> rowsToRemove = getRecords(rows);
    removeAll(rowsToRemove);
  }

  public void removeAll(final Collection<LayerRecord> objects) {
    for (final LayerRecord object : objects) {
      final int row = this.records.indexOf(object);
      if (row != -1) {
        this.records.remove(row);
        fireTableRowsDeleted(row, row + 1);
      }
    }
  }

  public void removeAll(final LayerRecord... removedFeatures) {
    removeAll(Arrays.asList(removedFeatures));
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final Record object = getRecord(fromIndex);
    if (object instanceof LayerRecord) {
      final LayerRecord layerRecord = (LayerRecord)object;
      removeAll(layerRecord);
      add(toIndex, layerRecord);
      clearSortedColumns();
    }
    firePropertyChange("reorder", false, true);
  }

  /**
   * @param records the records to set
   */
  public void setRecords(final List<LayerRecord> records) {
    this.records.clear();
    if (records != null) {
      this.records.addAll(records);
    }
    fireTableDataChanged();
  }

  @Override
  public SortOrder setSortOrder(final int column) {
    final SortOrder sortOrder = super.setSortOrder(column);
    if (this.records != null) {
      final String attributeName = getFieldName(column);
      final Comparator<Record> comparitor = new RecordFieldComparator(
        sortOrder == SortOrder.ASCENDING, attributeName);
      Collections.sort(this.records, comparitor);
      fireTableDataChanged();
    }
    return sortOrder;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Record object = getRecord(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = object.getValueByPath(name);
      object.setValue(name, value);
      final PropertyChangeEvent event = new PropertyChangeEvent(object, name, oldValue, value);
      getPropertyChangeSupport().firePropertyChange(event);
    }
  }

}
