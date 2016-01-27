package com.revolsys.swing.map.layer.record.table.model;

import java.util.List;

import javax.swing.Icon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;

public class ModeAllList extends ModeAbstractCached {
  public ModeAllList(final ListRecordLayerTableModel model) {
    super(RecordLayerTableModel.MODE_RECORDS_ALL, model);
  }

  @Override
  public void activate() {
    final AbstractRecordLayer layer = getLayer();
    for (final String propertyName : new String[] {
      "filter", AbstractRecordLayer.RECORDS_CHANGED
    }) {
      addListeners( //
        Property.addListenerRunnable(layer, propertyName, this::refresh));
    }
    addListeners( //
      Property.addListenerRunnable(layer, AbstractRecordLayer.RECORD_UPDATED, this::repaint) //
    );
    super.activate();
  }

  @Override
  public Icon getIcon() {
    return Icons.getIcon("table_filter");
  }

  @Override
  protected List<LayerRecord> getRecordsForCache() {
    final AbstractRecordLayer layer = getLayer();
    return layer.getRecords();
  }

  @Override
  public String getTitle() {
    return "Show All Records";
  }
}