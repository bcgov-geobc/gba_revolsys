package com.revolsys.swing.map.layer.dataobject.table;

import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.RecordDefinition;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.InvokeMethodEnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.action.enablecheck.OrEnableCheck;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.component.AttributeFilterPanel;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.TableRowCount;
import com.revolsys.swing.table.dataobject.editor.DataObjectTableCellEditor;
import com.revolsys.swing.table.dataobject.model.DataObjectRowTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowPropertyEnableCheck;
import com.revolsys.swing.table.dataobject.row.DataObjectRowRunnable;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectLayerTablePanel extends TablePanel implements
PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static final String FILTER_GEOMETRY = "filter_geometry";

  public static final String FILTER_ATTRIBUTE = "filter_attribute";

  private final AbstractDataObjectLayer layer;

  private final DataObjectLayerTableModel tableModel;

  private final JToggleButton selectedButton;

  private final DataObjectTableCellEditor tableCellEditor;

  public DataObjectLayerTablePanel(final AbstractDataObjectLayer layer,
    final DataObjectLayerTable table) {
    super(table);
    this.layer = layer;
    this.tableCellEditor = table.getTableCellEditor();
    this.tableCellEditor.setPopupMenu(getMenu());
    table.getTableCellEditor().addMouseListener(this);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    this.tableModel = getTableModel();
    final RecordDefinition metaData = layer.getMetaData();
    final boolean hasGeometry = metaData.getGeometryAttributeIndex() != -1;
    final EnableCheck deletableEnableCheck = new DataObjectRowPropertyEnableCheck(
        "deletable");

    final EnableCheck modifiedEnableCheck = new DataObjectRowPropertyEnableCheck(
        "modified");
    final EnableCheck deletedEnableCheck = new DataObjectRowPropertyEnableCheck(
        "deleted");
    final EnableCheck notEnableCheck = new DataObjectRowPropertyEnableCheck(
      "deleted", false);
    final OrEnableCheck modifiedOrDeleted = new OrEnableCheck(
      modifiedEnableCheck, deletedEnableCheck);

    final EnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(
      layer, "editable");

    final EnableCheck cellEditingEnableCheck = new ObjectPropertyEnableCheck(
      this, "editingCurrentCell");

    // Right click Menu
    final MenuFactory menu = getMenu();

    menu.addMenuItemTitleIcon("record", "View/Edit Record", "table_edit",
      notEnableCheck, this, "editRecord");

    if (hasGeometry) {
      menu.addMenuItemTitleIcon("record", "Zoom to Record",
        "magnifier_zoom_selected", this, "zoomToRecord");
    }
    menu.addMenuItemTitleIcon("record", "Delete Record", "table_row_delete",
      deletableEnableCheck, this, "deleteRecord");

    menu.addMenuItem("record", DataObjectRowRunnable.createAction(
      "Revert Record", "arrow_revert", modifiedOrDeleted, "revertChanges"));

    menu.addMenuItem("record", DataObjectRowRunnable.createAction(
      "Revert Empty Fields", "field_empty_revert", modifiedEnableCheck,
        "revertEmptyFields"));

    menu.addMenuItemTitleIcon("dnd", "Copy Record", "page_copy", this,
        "copyRecord");

    menu.addMenuItemTitleIcon("dataTransfer", "Cut Field Value", "cut",
      cellEditingEnableCheck, this, "cutFieldValue");
    menu.addMenuItemTitleIcon("dataTransfer", "Copy Field Value", "page_copy",
      this, "copyFieldValue");
    menu.addMenuItemTitleIcon("dataTransfer", "Paste Field Value",
      "paste_plain", cellEditingEnableCheck, this, "pasteFieldValue");

    if (hasGeometry) {
      menu.addMenuItemTitleIcon("dnd", "Paste Geometry", "geometry_paste",
        new AndEnableCheck(editableEnableCheck, new InvokeMethodEnableCheck(
          this, "canPasteRecordGeometry")), this, "pasteGeometry");

      final MenuFactory editMenu = new MenuFactory("Edit Record Operations");
      final DataType geometryDataType = metaData.getGeometryAttribute()
          .getType();
      if (geometryDataType == DataTypes.LINE_STRING
          || geometryDataType == DataTypes.MULTI_LINE_STRING) {
        if (DirectionalAttributes.getProperty(metaData)
            .hasDirectionalAttributes()) {
          editMenu.addMenuItemTitleIcon("geometry",
            DataObjectLayerForm.FLIP_RECORD_NAME,
            DataObjectLayerForm.FLIP_RECORD_ICON, editableEnableCheck, this,
              "flipRecordOrientation");
          editMenu.addMenuItemTitleIcon("geometry",
            DataObjectLayerForm.FLIP_LINE_ORIENTATION_NAME,
            DataObjectLayerForm.FLIP_LINE_ORIENTATION_ICON,
            editableEnableCheck, this, "flipLineOrientation");
          editMenu.addMenuItemTitleIcon("geometry",
            DataObjectLayerForm.FLIP_FIELDS_NAME,
            DataObjectLayerForm.FLIP_FIELDS_ICON, editableEnableCheck, this,
              "flipFields");
        } else {
          editMenu.addMenuItemTitleIcon("geometry", "Flip Line Orientation",
            "flip_line", editableEnableCheck, this, "flipLineOrientation");
        }
      }
      if (editMenu.getItemCount() > 0) {
        menu.addComponentFactory("record", 2, editMenu);
      }
    }

    // Toolbar
    final ToolBar toolBar = getToolBar();

    final MenuFactory menuFactory = ObjectTreeModel.findMenu(layer);
    if (menuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        ObjectTree.class, "showMenu", menuFactory, layer, this, 10, 10);
    }
    toolBar.addComponent("count", new TableRowCount(this.tableModel));

    toolBar.addButtonTitleIcon("table", "Refresh", "table_refresh", this,
        "refresh");

    final AttributeFilterPanel attributeFilterPanel = new AttributeFilterPanel(
      this, this.tableModel);
    toolBar.addComponent("search", attributeFilterPanel);

    toolBar.addButtonTitleIcon("search", "Advanced Search", "filter_edits",
      attributeFilterPanel, "showAdvancedFilter");

    final EnableCheck hasFilter = new ObjectPropertyEnableCheck(
      this.tableModel, "hasFilter");

    toolBar.addButton("search", "Clear Search", "filter_delete", hasFilter,
      attributeFilterPanel, "clear");

    // Filter buttons

    final JToggleButton clearFilter = toolBar.addToggleButtonTitleIcon(
      FILTER_ATTRIBUTE, -1, "Show All Records", "table_filter",
      this.tableModel, "setAttributeFilterMode",
      DataObjectLayerTableModel.MODE_ALL);
    clearFilter.doClick();

    toolBar.addToggleButton(FILTER_ATTRIBUTE, -1, "Show Only Changed Records",
      "change_table_filter", editableEnableCheck, this.tableModel,
      "setAttributeFilterMode", DataObjectLayerTableModel.MODE_EDITS);

    this.selectedButton = toolBar.addToggleButton(FILTER_ATTRIBUTE, -1,
      "Show Only Selected Records", "filter_selected", null, this.tableModel,
      "setAttributeFilterMode", DataObjectLayerTableModel.MODE_SELECTED);

    if (hasGeometry) {
      final JToggleButton showAllGeometries = toolBar.addToggleButtonTitleIcon(
        FILTER_GEOMETRY, -1, "Show All Records ", "world_filter",
        this.tableModel, "setFilterByBoundingBox", false);
      showAllGeometries.doClick();

      toolBar.addToggleButtonTitleIcon(FILTER_GEOMETRY, -1,
        "Show Records on Map", "map_filter", this.tableModel,
        "setFilterByBoundingBox", true);
    }
    Property.addListener(layer, this);
  }

  public boolean canPasteRecordGeometry() {
    final LayerDataObject record = getEventRowObject();
    return this.layer.canPasteRecordGeometry(record);
  }

  public void copyFieldValue() {
    if (isEditingCurrentCell()) {
      final JComponent editorComponent = this.tableCellEditor.getEditorComponent();
      SwingUtil.dndCopy(editorComponent);
    } else {
      final DataObjectRowTableModel model = getTableModel();
      final int row = getEventRow();
      final int column = getEventColumn();
      final Object value = model.getValueAt(row, column);

      final String displayValue = model.toDisplayValue(row, column, value);
      final StringSelection transferable = new StringSelection(displayValue);
      ClipboardUtil.setContents(transferable);
    }
  }

  public void copyRecord() {
    final LayerDataObject record = getEventRowObject();
    this.layer.copyRecordsToClipboard(Collections.singletonList(record));
  }

  public void cutFieldValue() {
    if (isEditingCurrentCell()) {
      final JComponent editorComponent = this.tableCellEditor.getEditorComponent();
      SwingUtil.dndCut(editorComponent);
    }
  }

  public void deleteRecord() {
    final LayerDataObject object = getEventRowObject();
    this.layer.deleteRecords(object);
  }

  public void editRecord() {
    final LayerDataObject object = getEventRowObject();
    if (object != null && !object.isDeleted()) {
      this.layer.showForm(object);
    }
  }

  public void flipFields() {
    final Record record = getEventRowObject();
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record);
    property.reverseAttributes(record);
  }

  public void flipLineOrientation() {
    final Record record = getEventRowObject();
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record);
    property.reverseGeometry(record);
  }

  public void flipRecordOrientation() {
    final Record record = getEventRowObject();
    DirectionalAttributes.reverse(record);
  }

  public Collection<? extends String> getColumnNames() {
    return this.layer.getColumnNames();
  }

  protected LayerDataObject getEventRowObject() {
    final DataObjectRowTableModel model = getTableModel();
    final int row = getEventRow();
    if (row > -1) {
      final LayerDataObject object = model.getRecord(row);
      return object;
    } else {
      return null;
    }
  }

  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DataObjectLayerTableModel getTableModel() {
    final JTable table = getTable();
    return (DataObjectLayerTableModel)table.getModel();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    super.mouseClicked(e);
    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
      this.tableCellEditor.stopCellEditing();
      editRecord();
    }
  }

  public void pasteFieldValue() {
    if (isEditingCurrentCell()) {
      final JComponent editorComponent = this.tableCellEditor.getEditorComponent();
      SwingUtil.dndPaste(editorComponent);
    }
  }

  public void pasteGeometry() {
    final LayerDataObject record = getEventRowObject();
    this.layer.pasteRecordGeometry(record);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof LayerDataObject) {
      repaint();
    } else if (source == this.layer) {
      final String propertyName = event.getPropertyName();
      if ("recordsChanged".equals(propertyName)) {
        this.tableModel.refresh();
      }
      repaint();
    }
  }

  public void refresh() {
    this.tableModel.refresh();
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Property.removeListener(this.layer, this);
  }

  public void setAttributeFilterMode(final String mode) {
    if (DataObjectLayerTableModel.MODE_SELECTED.equals(mode)) {
      this.selectedButton.doClick();
    }
  }

  public void zoomToRecord() {
    final Record object = getEventRowObject();
    final Project project = this.layer.getProject();
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
