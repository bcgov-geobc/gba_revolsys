package com.revolsys.swing.map.layer.record.table.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerDataObject;

public class RecordLayerListSelectionModel extends DefaultListSelectionModel {
  private static final long serialVersionUID = 1L;

  private final RecordLayerTableModel model;

  public RecordLayerListSelectionModel(final RecordLayerTableModel model) {
    this.model = model;
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
    super.addSelectionInterval(convertRowIndexToModel(index0), convertRowIndexToModel(index1));
    final List<LayerDataObject> objects = getObjects(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.addSelectedRecords(objects);
  }

  public int convertRowIndexToModel(final int i) {
    return this.model.getTable().convertRowIndexToModel(i);
  }

  protected List<LayerDataObject> getObjects(final int index0, final int index1) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    for (int i = index0; i <= index1; i++) {
      final int rowIndex = convertRowIndexToModel(i);
      final LayerDataObject object = this.model.getRecord(rowIndex);
      objects.add(object);
    }
    return objects;
  }

  @Override
  public boolean isSelectedIndex(final int index) {
    final int rowIndex = convertRowIndexToModel(index);
    final LayerDataObject object = this.model.getRecord(rowIndex);
    if (object != null) {
      final AbstractRecordLayer layer = this.model.getLayer();
      return layer.isSelected(object);
    }
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    final AbstractRecordLayer layer = this.model.getLayer();
    return layer.getSelectionCount() == 0;
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
    super.removeSelectionInterval(convertRowIndexToModel(index0), convertRowIndexToModel(index1));
    final List<LayerDataObject> objects = getObjects(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.unSelectRecords(objects);
  }

  @Override
  public void setSelectionInterval(final int index0, final int index1) {
    final List<LayerDataObject> objects = getObjects(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.setSelectedRecords(objects);
    super.setSelectionInterval(convertRowIndexToModel(index0), convertRowIndexToModel(index1));
  }
}
