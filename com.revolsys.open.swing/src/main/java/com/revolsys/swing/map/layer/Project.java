package com.revolsys.swing.map.layer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.CollectionUtil;

public class Project extends LayerGroup {

  private static WeakReference<Project> project = new WeakReference<Project>(
    null);

  public static Project get() {
    return Project.project.get();
  }

  public static void set(final Project project) {
    Project.project = new WeakReference<Project>(project);
  }

  private LayerGroup baseMapLayers = new LayerGroup("Base Maps");

  private BoundingBox viewBoundingBox = new BoundingBox();

  public Project() {
    this("Project");
    setGeometryFactory(GeometryFactory.getFactory(3005));
  }

  public Project(final String name) {
    super(name);
    baseMapLayers.setLayerGroup(this);
    set(this);
  }

  private void addChangedLayers(final LayerGroup group,
    final List<Layer> layersWithChanges) {
    for (final Layer layer : group) {
      if (layer instanceof LayerGroup) {
        final LayerGroup subGroup = (LayerGroup)layer;
        addChangedLayers(subGroup, layersWithChanges);
      } else if (layer.isHasChanges()) {
        layersWithChanges.add(layer);
      }
    }

  }

  @Override
  public void delete() {
    super.delete();
    this.baseMapLayers = null;
    this.viewBoundingBox = null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final String name) {
    if (name.equals("Base Maps")) {
      return (V)baseMapLayers;
    } else {
      return (V)super.getLayer(name);
    }
  }

  @Override
  public Project getProject() {
    return this;
  }

  public BoundingBox getViewBoundingBox() {
    return viewBoundingBox;
  }

  public boolean saveChangesWithPrompt() {
    final List<Layer> layersWithChanges = new ArrayList<Layer>();
    addChangedLayers(this, layersWithChanges);

    if (layersWithChanges.isEmpty()) {
      return true;
    } else {
      final MapPanel mapPanel = MapPanel.get(this);
      final JLabel message = new JLabel(
        "<html><body><p><b>The following layers have un-saved changes.</b></p>"
          + "<p><b>Do you want to save the changes before continuing?</b></p><ul><li>"
          + CollectionUtil.toString("</li>\n<li>", layersWithChanges)
          + "</li></ul></body></html>");

      final int option = JOptionPane.showConfirmDialog(mapPanel, message,
        "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE);
      if (option == JOptionPane.CANCEL_OPTION) {
        return false;
      } else if (option == JOptionPane.NO_OPTION) {
        return true;
      } else {
        for (final Iterator<Layer> iterator = layersWithChanges.iterator(); iterator.hasNext();) {
          final Layer layer = iterator.next();
          if (layer.saveChanges()) {
            iterator.remove();
          }
        }
        if (layersWithChanges.isEmpty()) {
          return true;
        } else {
          final JLabel message2 = new JLabel(
            "<html><body><p><b>The following layers could not be saved.</b></p>"
              + "<p><b>Do you want to ignore these changes and continue?</b></p><ul><li>"
              + CollectionUtil.toString("</li>\n<li>", layersWithChanges)
              + "</li></ul></body></html>");

          final int option2 = JOptionPane.showConfirmDialog(mapPanel, message2,
            "Ignore Changes", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
          if (option2 == JOptionPane.CANCEL_OPTION) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
  }

  public void setViewBoundingBox(BoundingBox viewBoundingBox) {

    if (!viewBoundingBox.isNull()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox oldValue = this.viewBoundingBox;
      if (viewBoundingBox.getWidth() == 0) {
        if (geometryFactory.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
          viewBoundingBox = viewBoundingBox.expand(0.000009 * 100, 0);
        } else {
          viewBoundingBox = viewBoundingBox.expand(100, 0);
        }
      }
      if (viewBoundingBox.getHeight() == 0) {
        if (geometryFactory.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
          viewBoundingBox = viewBoundingBox.expand(0, 0.000009 * 100);
        } else {
          viewBoundingBox = viewBoundingBox.expand(0, 100);
        }
      }
      this.viewBoundingBox = viewBoundingBox;
      firePropertyChange("viewBoundingBox", oldValue, viewBoundingBox);
    }
  }
}
