package com.revolsys.swing.map.table;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.JToggleButton;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class DataObjectLayerTablePanel extends TablePanel {

  private final DataObjectLayer layer;

  public DataObjectLayerTablePanel(final DataObjectLayer layer,
    final JTable table) {
    super(table);
    this.layer = layer;
    final MenuFactory menu = getMenu();
    final DataObjectMetaData metaData = layer.getMetaData();
    final boolean hasGeometry = metaData.getGeometryAttributeIndex() != -1;
    if (hasGeometry) {
      menu.addMenuItemTitleIcon("zoom", "Zoom to Record",
        "magnifier_zoom_selected", this, "zoomToRecord");
    }

    menu.addMenuItemTitleIcon("record", "View/Edit Record", "table_edit", this,
      "editRecord");

    final ObjectPropertyEnableCheck canDeleteObjectsEnableCheck = new ObjectPropertyEnableCheck(
      layer, "canDeleteObjects");
    menu.addMenuItemTitleIcon("record", "Delete Record", "table_delete",
      canDeleteObjectsEnableCheck, this, "deleteRecord");

    final ToolBar toolBar = getToolBar();

    final ObjectPropertyEnableCheck canAddObjectsEnableCheck = new ObjectPropertyEnableCheck(
      layer, "canAddObjects");
    toolBar.addButton("record", "Add New Record", "table_row_insert",
      canAddObjectsEnableCheck, layer, "addNewRecord");

    // Filter buttons
    final DataObjectLayerTableModel tableModel = getTableModel();

    final JToggleButton clearFilter = toolBar.addToggleButtonTitleIcon(
      "filter_group", "Show All Records", "filter_delete", tableModel,
      "setMode", DataObjectLayerTableModel.MODE_ALL);
    clearFilter.doClick();

    final ObjectPropertyEnableCheck selectableEnableCheck = new ObjectPropertyEnableCheck(
      layer, "selectionCount", 0, true);
    toolBar.addToggleButton("filter_group", "Show Only Selected Records",
      "filter_selected", selectableEnableCheck, tableModel, "setMode",
      DataObjectLayerTableModel.MODE_SELECTED);

    final ObjectPropertyEnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(
      layer, "editable");
    toolBar.addToggleButton("filter_group", "Show Only Changed Records",
      "filter_changes", editableEnableCheck, tableModel, "setMode",
      DataObjectLayerTableModel.MODE_CHANGES);
  }

  public void deleteRecord() {
    final DataObject object = getEventRowObject();
    layer.deleteObjects(object);
  }

  public void editRecord() {
    final DataObject object = getEventRowObject();
    LayerUtil.showForm(layer, object);
  }

  protected DataObject getEventRowObject() {
    final DataObjectRowTableModel model = getTableModel();
    final int row = getEventRow();
    final DataObject object = model.getObject(row);
    return object;
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public DataObjectLayerTableModel getTableModel() {
    final JTable table = getTable();
    return (DataObjectLayerTableModel)table.getModel();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    super.mouseClicked(e);
    if (SwingUtil.isLeftButtonAndNoModifiers(e) && e.getClickCount() == 2) {
      editRecord();
    }
  }

  public void zoomToRecord() {
    final DataObject object = getEventRowObject();
    final Project project = layer.getProject();
    final Geometry geometry = object.getGeometryValue();
    if (geometry != null) {
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry)
        .convert(geometryFactory)
        .expandPercent(0.1);
      project.setViewBoundingBox(boundingBox);
    }
  }
}
