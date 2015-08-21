package com.revolsys.swing.map.layer.raster;

import java.util.Arrays;
import java.util.List;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.MappedLocation;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.NumberTableCellRenderer;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.object.ObjectListTable;
import com.revolsys.swing.table.object.ObjectListTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.MenuSourceRunnable;
import com.vividsolutions.jts.geom.Geometry;

public class TiePointsPanel extends TablePanel {
  private static final long serialVersionUID = 1L;

  private static final List<String> COLUMN_NAMES = Arrays.asList("sourcePixel.x", "sourcePixel.y",
    "targetPoint.x", "targetPoint.y");

  private static final List<String> TITLES = Arrays.asList("Source Pixel X", "Source Pixel Y",
    "Target Point X", "Target Point Y");

  private final GeoreferencedImageLayer layer;

  public TiePointsPanel(final GeoreferencedImageLayer layer) {
    super(new ObjectListTable(layer.getImage().getTiePoints(), COLUMN_NAMES, TITLES));

    this.layer = layer;

    final BaseJTable table = getTable();
    for (int i = 0; i < table.getColumnCount(); i++) {
      final TableColumnExt column = table.getColumnExt(i);
      column.setMinWidth(150);
      column.setCellRenderer(new NumberTableCellRenderer("#,###.000"));
    }

    final EnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(layer, "editable");

    final MenuFactory menu = getMenu();
    menu.addMenuItemTitleIcon("zoom", "Zoom to Tie Point", "magnifier_zoom_selected", this,
      "zoomToTiePoint");

    menu.addMenuItemTitleIcon("record", "Delete Tie Point", "table_row_delete",
      editableEnableCheck, this, "deleteTiePoint");

    final ToolBar toolBar = getToolBar();

    final MenuFactory menuFactory = MenuFactory.findMenu(layer);
    if (menuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu", menuFactory, "show", layer, this,
        10, 10);
    }

    menu.addMenuItem("zoom",
      MenuSourceRunnable.createAction("Zoom to Layer", "magnifier", "zoomToLayer"));

    toolBar.addButton("zoom", "Zoom to Layer", "magnifier", (EnableCheck)null, layer, "zoomToLayer");

    toolBar.addButton("edit", "Fit to Screen", "arrow_out", editableEnableCheck, layer,
      "fitToViewport");

  }

  public void deleteTiePoint() {
    final MappedLocation object = getEventRowObject();
    getTiePoints().remove(object);
  }

  protected MappedLocation getEventRowObject() {
    final ObjectListTableModel<MappedLocation> model = getTableModel();
    final int row = getEventRow();
    final MappedLocation object = model.getObject(row);
    return object;
  }

  public GeoreferencedImage getImage() {
    return getLayer().getImage();
  }

  public GeoreferencedImageLayer getLayer() {
    return this.layer;
  }

  public List<MappedLocation> getTiePoints() {
    return getImage().getTiePoints();
  }

  public void zoomToTiePoint() {
    final MappedLocation object = getEventRowObject();
    final GeoreferencedImage image = this.layer.getImage();
    final Geometry geometry = object.getSourceToTargetLine(image, this.layer.getBoundingBox(),
      !this.layer.isShowOriginalImage());
    if (geometry != null) {
      final Project project = Project.get();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry)
        .convert(geometryFactory)
        .expand(200);
      project.setViewBoundingBox(boundingBox);

    }
  }

}
