package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.undo.UndoableEdit;

import com.revolsys.awt.WebColors;
import com.revolsys.data.record.Record;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.listener.EnableComponentListener;
import com.revolsys.swing.listener.InvokeMethodSelectedItemListener;
import com.revolsys.swing.map.border.FullSizeLayoutManager;
import com.revolsys.swing.map.border.MapRulerBorder;
import com.revolsys.swing.map.component.MapPointerLocation;
import com.revolsys.swing.map.component.SelectMapCoordinateSystem;
import com.revolsys.swing.map.component.SelectMapScale;
import com.revolsys.swing.map.component.SelectMapUnitsPerPixel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.list.LayerGroupListModel;
import com.revolsys.swing.map.listener.FileDropTargetListener;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.EditGeometryOverlay;
import com.revolsys.swing.map.overlay.EditGeoreferencedImageOverlay;
import com.revolsys.swing.map.overlay.LayerRendererOverlay;
import com.revolsys.swing.map.overlay.MouseOverlay;
import com.revolsys.swing.map.overlay.SelectRecordsOverlay;
import com.revolsys.swing.map.overlay.ToolTipOverlay;
import com.revolsys.swing.map.overlay.ZoomOverlay;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SwingWorkerProgressBar;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;

public class MapPanel extends JPanel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static final List<Long> SCALES = Arrays.asList(500000000L, 250000000L, 100000000L,
    50000000L, 25000000L, 10000000L, 5000000L, 2500000L, 1000000L, 500000L, 250000L, 100000L,
    50000L, 25000L, 10000L, 5000L, 2500L, 1000L, 500L, 250L, 100L, 50L, 25L, 10L, 5L);

  public static final BoundingBox BC_ENVELOPE = new BoundingBox(GeometryFactory.getFactory(3005, 3,
    1000, 1000), 25000, 340000, 1900000, 1750000);

  public static final String MAP_CONTROLS_WORKING_AREA = "mapControlsCWorkingArea";

  public static final String MAP_PANEL = "mapPanel";

  public static final String MAP_TABLE_WORKING_AREA = "mapTablesCWorkingArea";

  private static final PrecisionModel SCALE_PRECISION_MODEL = new PrecisionModel(10);

  public static MapPanel get(final Layer layer) {
    if (layer == null) {
      return null;
    } else {
      final LayerGroup project = layer.getProject();
      if (project == null) {
        return null;
      } else {
        return project.getProperty(MAP_PANEL);
      }
    }
  }

  private String overlayAction;

  private List<Long> scales = new ArrayList<Long>();

  private LayerGroup baseMapLayers;

  private LayerRendererOverlay baseMapOverlay;

  private final JLayeredPane layeredPane;

  private LayerRendererOverlay layerOverlay;

  private MouseOverlay mouseOverlay;

  private int overlayIndex = 1;

  private Project project;

  private double scale = 500000000;

  private final UndoManager undoManager = new UndoManager();

  private BasePanel leftStatusBar = new BasePanel(new FlowLayout(FlowLayout.LEFT));

  private BasePanel rightStatusBar = new BasePanel(new FlowLayout(FlowLayout.RIGHT));

  private final ToolBar toolBar = new ToolBar();

  private Viewport2D viewport;

  private final LinkedList<BoundingBox> zoomHistory = new LinkedList<BoundingBox>();

  private int zoomHistoryIndex = -1;

  private FileDropTargetListener fileDropListener;

  private boolean updateZoomHistory = true;

  private ToolTipOverlay toolTipOverlay;

  private SwingWorkerProgressBar progressBar;

  private JButton zoomBookmarkButton;

  private JPanel statusBarPanel;

  private boolean settingBoundingBox = false;

  private boolean settingScale;

  private ComboBox baseMapLayerField;

  private JLabel overlayActionLabel;

  public MapPanel() {
    this(new Project());
  }

  public MapPanel(final Project project) {
    super(new BorderLayout());
    this.project = project;
    this.baseMapLayers = project.getBaseMapLayers();
    project.setProperty(MAP_PANEL, this);
    this.layeredPane = new JLayeredPane();
    this.layeredPane.setOpaque(true);
    this.layeredPane.setBackground(Color.WHITE);
    this.layeredPane.setVisible(true);
    this.layeredPane.setLayout(new FullSizeLayoutManager());

    add(this.layeredPane, BorderLayout.CENTER);

    this.viewport = new ComponentViewport2D(project, this.layeredPane);
    Property.addListener(this.viewport, this);

    createScales();
    this.viewport.setScales(getScales());

    this.layeredPane.setBorder(new MapRulerBorder(this.viewport));

    this.baseMapOverlay = new LayerRendererOverlay(this);
    this.baseMapOverlay.setLayer(NullLayer.INSTANCE);
    this.layeredPane.add(this.baseMapOverlay, new Integer(0));
    Property.addListener(this.baseMapOverlay, "layer", this);

    this.layerOverlay = new LayerRendererOverlay(this, project);
    this.layeredPane.add(this.layerOverlay, new Integer(1));

    Property.addListener(this.baseMapLayers, this);
    Property.addListener(project, this);

    addMapOverlays();

    addToolBar();

    addStatusBar();

    zoomToWorld();

    this.fileDropListener = new FileDropTargetListener(this);
    this.undoManager.addKeyMap(this);
  }

  public void addBaseMap(final Layer layer) {
    if (layer != null) {
      this.baseMapLayers.add(layer);
    }
    if (this.baseMapLayers.size() == 1) {
      if (layer.isVisible()) {
        setBaseMapLayer(layer);
      }
    }
  }

  private void addLayerControls() {
    this.toolBar.addButtonTitleIcon("layers", "Refresh All Layers", "arrow_refresh", this,
      "refresh");

    final SelectMapCoordinateSystem selectCoordinateSystem = new SelectMapCoordinateSystem(this);
    this.toolBar.addComponent("layers", selectCoordinateSystem);

    final LayerGroupListModel baseMapLayersModel = new LayerGroupListModel(this.baseMapLayers, true);
    this.baseMapLayerField = new ComboBox(baseMapLayersModel);
    this.baseMapLayerField.setMaximumSize(new Dimension(200, 22));
    this.baseMapLayerField.addItemListener(new InvokeMethodSelectedItemListener(this,
      "setBaseMapLayer"));
    if (this.baseMapLayers.size() > 0) {
      this.baseMapLayerField.setSelectedIndex(1);
    }
    this.baseMapLayerField.setToolTipText("Base Map");
    this.toolBar.addComponent("layers", this.baseMapLayerField);
    Property.addListener(this.baseMapOverlay, "layer", this);
    this.toolBar.addButtonTitleIcon("layers", "Refresh Base Map", "map_refresh",
      this.baseMapOverlay, "refresh");
  }

  public void addMapOverlay(final int zIndex, final JComponent overlay) {
    this.layeredPane.add(overlay, new Integer(zIndex));
    if (overlay instanceof PropertyChangeListener) {
      final PropertyChangeListener listener = (PropertyChangeListener)overlay;
      Property.addListener(this, listener);
      Property.addListener(this.project, listener);
      Property.addListener(this.baseMapLayers, listener);
    }
    Property.addListener(overlay, this);
  }

  public void addMapOverlay(final JComponent overlay) {
    final int zIndex = 100 * this.overlayIndex++;
    addMapOverlay(zIndex, overlay);
  }

  protected void addMapOverlays() {
    new ZoomOverlay(this);
    new SelectRecordsOverlay(this);
    new EditGeometryOverlay(this);
    this.mouseOverlay = new MouseOverlay(this.layeredPane);
    new EditGeoreferencedImageOverlay(this);
    this.toolTipOverlay = new ToolTipOverlay(this);
  }

  private void addPointerLocation(final boolean geographics) {
    final MapPointerLocation location = new MapPointerLocation(this, geographics);
    this.leftStatusBar.add(location);
  }

  protected void addStatusBar() {
    this.statusBarPanel = new JPanel(new BorderLayout());
    this.statusBarPanel.setMinimumSize(new Dimension(200, 42));
    add(this.statusBarPanel, BorderLayout.SOUTH);
    this.statusBarPanel.add(this.leftStatusBar, BorderLayout.WEST);
    this.statusBarPanel.add(this.rightStatusBar, BorderLayout.EAST);

    addPointerLocation(false);
    addPointerLocation(true);

    this.overlayActionLabel = new JLabel();
    this.overlayActionLabel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),
      BorderFactory.createEmptyBorder(1, 3, 1, 3)));
    this.overlayActionLabel.setMinimumSize(new Dimension(20, 30));
    this.overlayActionLabel.setVisible(false);
    this.overlayActionLabel.setForeground(WebColors.Green);
    this.leftStatusBar.add(this.overlayActionLabel);

    this.progressBar = new SwingWorkerProgressBar();
    this.rightStatusBar.add(this.progressBar);
  }

  protected void addToolBar() {
    add(this.toolBar, BorderLayout.NORTH);

    addZoomButtons();

    addUndoButtons();

    addLayerControls();

  }

  public void addUndo(final UndoableEdit edit) {
    this.undoManager.addEdit(edit);
  }

  protected void addUndoButtons() {
    final EnableCheck canUndo = new ObjectPropertyEnableCheck(this.undoManager, "canUndo");
    final EnableCheck canRedo = new ObjectPropertyEnableCheck(this.undoManager, "canRedo");

    this.toolBar.addButton("undo", "Undo", "arrow_undo", canUndo, this.undoManager, "undo");
    this.toolBar.addButton("undo", "Redo", "arrow_redo", canRedo, this.undoManager, "redo");
  }

  public void addZoomBookmark() {
    final BoundingBox boundingBox = getBoundingBox();
    if (!boundingBox.isEmpty()) {
      final String name = JOptionPane.showInputDialog(this, "Enter bookmark name",
        "Add Zoom Bookmark", JOptionPane.QUESTION_MESSAGE);
      if (Property.hasValue(name)) {
        final Project project = getProject();
        project.addZoomBookmark(name, boundingBox);
      }
    }
  }

  private void addZoomButtons() {
    this.toolBar.addButtonTitleIcon("zoom", "Zoom to World", "magnifier_zoom_world", this,
      "zoomToWorld");

    this.toolBar.addButtonTitleIcon("zoom", "Zoom to British Columbia", "zoom_bc", this,
      "setBoundingBox", MapPanel.BC_ENVELOPE);

    this.toolBar.addButtonTitleIcon("zoom", "Zoom In", "magnifier_zoom_in", this, "zoomIn");

    this.toolBar.addButtonTitleIcon("zoom", "Zoom Out", "magnifier_zoom_out", this, "zoomOut");

    final JButton zoomPreviousButton = this.toolBar.addButtonTitleIcon("zoom", "Zoom Previous",
      "magnifier_zoom_left", this, "zoomPrevious");
    zoomPreviousButton.setEnabled(false);
    Property.addListener(this, "zoomPreviousEnabled", new EnableComponentListener(
      zoomPreviousButton));

    final JButton zoomNextButton = this.toolBar.addButtonTitleIcon("zoom", "Zoom Next",
      "magnifier_zoom_right", this, "zoomNext");
    zoomNextButton.setEnabled(false);
    Property.addListener(this, "zoomNextEnabled", new EnableComponentListener(zoomNextButton));

    this.toolBar.addButtonTitleIcon("zoom", "Zoom To Selected", "magnifier_zoom_selected", this,
      "zoomToSelected");

    this.zoomBookmarkButton = this.toolBar.addButtonTitleIcon("zoom", "Zoom Bookmarks",
      "zoom_bookmark", this, "showZoomBookmarkMenu");

    this.toolBar.addComponent("zoom", new SelectMapScale(this));
    this.toolBar.addComponent("zoom", new SelectMapUnitsPerPixel(this));
  }

  public boolean clearOverlayAction(final String overlayAction) {
    if (this.overlayAction == overlayAction) {
      this.overlayAction = null;
      this.overlayActionLabel.setText("");
      this.overlayActionLabel.setVisible(false);
      return true;
    }
    return false;
  }

  public void clearToolTipText() {
    this.toolTipOverlay.clearText();
  }

  public void clearZoomHistory() {
    this.zoomHistory.clear();
    this.zoomHistoryIndex = -1;
  }

  public void createScales() {
    // double multiplier = 0.001;
    // for (int i = 0; i < 9; i++) {
    // addScale(1 * multiplier);
    // addScale(2 * multiplier);
    // addScale(5 * multiplier);
    // multiplier *= 10;
    // }
    // Collections.reverse(this.scales);
    this.scales = SCALES;
  }

  public void destroy() {
    Property.removeAllListeners(this);
    setDropTarget(null);
    this.layerOverlay.dispose();
    for (final Component overlay : this.layeredPane.getComponents()) {
      if (overlay instanceof AbstractOverlay) {
        final AbstractOverlay abstractOverlay = (AbstractOverlay)overlay;
        abstractOverlay.destroy();
      }
    }
    removeAll();

    this.layeredPane.removeAll();
    this.statusBarPanel.removeAll();
    this.leftStatusBar = null;
    this.rightStatusBar = null;
    if (this.baseMapLayers != null) {
      this.baseMapLayers.delete();
    }
    this.baseMapLayers = null;
    this.baseMapOverlay = null;
    this.fileDropListener = null;
    this.layerOverlay = null;
    this.progressBar = null;
    this.project = null;
    this.toolBar.clear();
    this.toolTipOverlay = null;
    this.undoManager.die();
    Property.removeAllListeners(this.viewport.getPropertyChangeSupport());
    this.viewport = null;
    this.zoomBookmarkButton = null;
    this.zoomHistory.clear();
  }

  @Override
  protected void finalize() throws Throwable {
    this.layerOverlay.dispose();
    super.finalize();
  }

  public Layer getBaseMapLayer() {
    return this.baseMapOverlay.getLayer();
  }

  public LayerGroup getBaseMapLayers() {
    return this.baseMapLayers;
  }

  public LayerRendererOverlay getBaseMapOverlay() {
    return this.baseMapOverlay;
  }

  public BoundingBox getBoundingBox() {
    return this.viewport.getBoundingBox();
  }

  public FileDropTargetListener getFileDropListener() {
    return this.fileDropListener;
  }

  public GeometryFactory getGeometryFactory() {
    return this.project.getGeometryFactory();
  }

  public LayerRendererOverlay getLayerOverlay() {
    return this.layerOverlay;
  }

  public JPanel getLeftStatusBar() {
    return this.leftStatusBar;
  }

  public Point getMapMousePosition() {
    final Point mousePosition = this.layeredPane.getMousePosition();
    if (mousePosition != null) {
      mousePosition.x -= this.layeredPane.getInsets().left;
      mousePosition.y -= this.layeredPane.getInsets().top;
    }
    return mousePosition;
  }

  @SuppressWarnings("unchecked")
  public <T extends JComponent> T getMapOverlay(final Class<T> overlayClass) {
    for (final Component component : this.layeredPane.getComponents()) {
      if (overlayClass.isAssignableFrom(component.getClass())) {
        return (T)component;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends JComponent> List<T> getMapOverlays(final Class<T> overlayClass) {
    final List<T> overlays = new ArrayList<T>();
    for (final Component component : this.layeredPane.getComponents()) {
      if (overlayClass.isAssignableFrom(component.getClass())) {
        overlays.add((T)component);
      }
    }
    return overlays;
  }

  public MouseOverlay getMouseOverlay() {
    return this.mouseOverlay;
  }

  public String getOverlayAction() {
    return this.overlayAction;
  }

  public SwingWorkerProgressBar getProgressBar() {
    return this.progressBar;
  }

  public Project getProject() {
    return this.project;
  }

  public JPanel getRightStatusBar() {
    return this.rightStatusBar;
  }

  public double getScale() {
    return this.scale;
  }

  public List<Long> getScales() {
    return this.scales;
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  public UndoManager getUndoManager() {
    return this.undoManager;
  }

  public double getUnitsPerPixel() {
    if (this.viewport == null) {
      return 1;
    } else {
      return this.viewport.getUnitsPerPixel();
    }
  }

  public Viewport2D getViewport() {
    return this.viewport;
  }

  public double getZoomInScale(final double scale) {
    final long scaleCeil = (long)Math.floor(scale);
    for (final double nextScale : this.scales) {
      final long newScale = (long)Math.floor(nextScale);
      if (newScale < scaleCeil) {
        return nextScale;
      }
    }
    return this.scales.get(this.scales.size() - 1);
  }

  public double getZoomOutScale(final double scale) {
    final long scaleCeil = (long)Math.floor(scale);
    final List<Long> scales = new ArrayList<Long>(this.scales);
    Collections.reverse(scales);
    for (final Long nextScale : scales) {
      final long newScale = nextScale;
      if (newScale > scaleCeil) {
        return nextScale;
      }
    }
    return scales.get(0);
  }

  public boolean hasOverlayAction() {
    return this.overlayAction != null;
  }

  public boolean isZoomNextEnabled() {
    return this.zoomHistoryIndex < this.zoomHistory.size() - 1;
  }

  public boolean isZoomPreviousEnabled() {
    return this.zoomHistoryIndex > 0;
  }

  public void moveToFront(final JComponent overlay) {
    this.layeredPane.moveToFront(overlay);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if ("srid".equals(propertyName)) {
      final Integer srid = (Integer)event.getNewValue();
      setGeometryFactory(GeometryFactory.floating3(srid));
    } else if ("viewBoundingBox".equals(propertyName)) {
      final BoundingBox boundingBox = (BoundingBox)event.getNewValue();
      setBoundingBox(boundingBox);
    } else if (source == this.viewport) {
      if ("scale".equals(propertyName)) {
        final double scale = this.viewport.getScale();
        setScale(scale);
      }
    } else if (source == this.baseMapOverlay) {
      if ("layer".equals(propertyName)) {
        final Layer layer = (Layer)event.getNewValue();
        if (layer != null && this.baseMapLayerField != null) {
          this.baseMapLayerField.setSelectedItem(layer);
        }
      }
    } else if (source == this.baseMapLayers) {
      if ("layers".equals(propertyName)) {
        if (this.baseMapOverlay != null
          && (this.baseMapOverlay.getLayer() == null || NullLayer.INSTANCE.equals(this.baseMapOverlay.getLayer()))) {
          final Layer layer = (Layer)event.getNewValue();
          if (layer != null && layer.isVisible()) {
            this.baseMapOverlay.setLayer(layer);
          }
        }
      }
    }
    repaint();
  }

  public void refresh() {
    final Project project = getProject();
    if (project != null) {
      project.refresh();
    }
  }

  public synchronized void setBaseMapLayer(final Layer layer) {
    if (layer == NullLayer.INSTANCE || this.baseMapLayers.contains(layer)) {
      final Layer oldValue = getBaseMapLayer();
      this.baseMapOverlay.setLayer(layer);
      firePropertyChange("baseMapLayer", oldValue, layer);
    }
  }

  public synchronized void setBoundingBox(final BoundingBox boundingBox) {
    if (!this.settingBoundingBox) {
      this.settingBoundingBox = true;
      try {
        final BoundingBox oldBoundingBox = getBoundingBox();
        final double oldUnitsPerPixel = getUnitsPerPixel();

        final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
        final boolean zoomNextEnabled = isZoomNextEnabled();
        final BoundingBox resizedBoundingBox = this.viewport.setBoundingBox(boundingBox);
        if (this.project != null) {
          this.project.setViewBoundingBox(resizedBoundingBox);

          setScale(this.viewport.getScale());
          synchronized (this.zoomHistory) {
            if (this.updateZoomHistory) {
              BoundingBox currentBoundingBox = null;
              if (this.zoomHistoryIndex > -1) {
                currentBoundingBox = this.zoomHistory.get(this.zoomHistoryIndex);
                if (!currentBoundingBox.equals(resizedBoundingBox)) {
                  while (this.zoomHistory.size() > this.zoomHistoryIndex + 1) {
                    this.zoomHistory.removeLast();
                  }
                  for (int i = this.zoomHistory.size() - 1; i > this.zoomHistoryIndex; i++) {
                    this.zoomHistory.remove(i);
                  }
                  this.zoomHistory.add(resizedBoundingBox);
                  this.zoomHistoryIndex = this.zoomHistory.size() - 1;
                  if (this.zoomHistory.size() > 50) {
                    this.zoomHistory.removeFirst();

                    this.zoomHistoryIndex--;
                  }
                }
              } else {
                this.zoomHistory.add(resizedBoundingBox);
                this.zoomHistoryIndex = 0;
              }
            }
          }
          firePropertyChange("unitsPerPixel", oldUnitsPerPixel, getUnitsPerPixel());
          firePropertyChange("boundingBox", oldBoundingBox, resizedBoundingBox);
          firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled, isZoomPreviousEnabled());
          firePropertyChange("zoomNextEnabled", zoomNextEnabled, isZoomNextEnabled());

          repaint();
        }
      } finally {
        this.settingBoundingBox = false;
      }
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final GeometryFactory oldValue = getGeometryFactory();
    if (geometryFactory != oldValue) {

      this.project.setGeometryFactory(geometryFactory);
      firePropertyChange("geometryFactory", oldValue, geometryFactory);
      repaint();
    }
  }

  public void setMapOverlayEnabled(final Class<? extends JComponent> overlayClass,
    final boolean enabled) {
    final JComponent component = getMapOverlay(overlayClass);
    if (component != null) {
      component.setEnabled(enabled);
    }
  }

  public boolean setOverlayAction(final String overlayAction) {
    if (this.overlayAction == null) {
      this.overlayAction = overlayAction;
      if (overlayAction == null) {
        this.overlayActionLabel.setText("");
        this.overlayActionLabel.setVisible(false);
      } else {
        this.overlayActionLabel.setText(CaseConverter.toCapitalizedWords(overlayAction));
        this.overlayActionLabel.setVisible(true);
      }
    }
    return this.overlayAction == overlayAction;
  }

  public synchronized void setScale(double scale) {
    if (!this.settingScale && !Double.isNaN(scale) && !Double.isInfinite(scale)) {
      try {
        this.settingScale = true;
        scale = SCALE_PRECISION_MODEL.makePrecise(scale);
        if (scale >= 0.1) {
          final double oldValue = this.scale;
          final double oldUnitsPerPixel = getUnitsPerPixel();
          if (scale != oldValue) {
            this.viewport.setScale(scale);
            this.scale = scale;
            firePropertyChange("scale", oldValue, scale);
            final double unitsPerPixel = getUnitsPerPixel();
            if (Math.abs(unitsPerPixel - oldUnitsPerPixel) > 0.0001) {
              firePropertyChange("unitsPerPixel", oldUnitsPerPixel, unitsPerPixel);
            }
            repaint();
          }
        }
      } finally {
        this.settingScale = false;
      }
    }
  }

  public void setToolTipText(final Point2D location, CharSequence text) {
    if (text == null) {
      text = "";
    }
    if (SwingUtilities.isEventDispatchThread()) {
      this.toolTipOverlay.setText(location, text);
    } else {
      Invoke.later(this.toolTipOverlay, "setText", location, text);
    }
  }

  public void setUnitsPerPixel(final double unitsPerPixel) {
    if (this.viewport != null) {
      double scale = this.viewport.getScaleForUnitsPerPixel(unitsPerPixel);
      scale = SCALE_PRECISION_MODEL.makePrecise(scale);
      final double oldUnitsPerPixel = getUnitsPerPixel();
      if (!MathUtil.precisionEqual(unitsPerPixel, oldUnitsPerPixel, 10000000.0)) {
        setScale(scale);
      }
    }
  }

  private void setZoomHistoryIndex(int zoomHistoryIndex) {
    synchronized (this.zoomHistory) {
      this.updateZoomHistory = false;
      try {
        final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
        final boolean zoomNextEnabled = isZoomNextEnabled();
        final int zoomHistorySize = this.zoomHistory.size();
        if (zoomHistoryIndex < 1) {
          zoomHistoryIndex = 0;
        } else if (zoomHistoryIndex >= zoomHistorySize) {
          zoomHistoryIndex = zoomHistorySize - 2;
        }
        this.zoomHistoryIndex = zoomHistoryIndex;
        final BoundingBox boundingBox = this.zoomHistory.get(zoomHistoryIndex);
        this.viewport.setBoundingBox(boundingBox);

        this.project.setViewBoundingBox(boundingBox);
        firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled, isZoomPreviousEnabled());
        firePropertyChange("zoomNextEnabled", zoomNextEnabled, isZoomNextEnabled());
      } finally {
        this.updateZoomHistory = true;
      }
    }
  }

  public void showZoomBookmarkMenu() {
    final PopupMenu menu = new PopupMenu();
    final MenuFactory factory = menu.getMenu();
    factory.addMenuItemTitleIcon("default", "Add Bookmark", "add", this, "addZoomBookmark");

    final Project project = getProject();
    for (final Entry<String, BoundingBox> entry : project.getZoomBookmarks().entrySet()) {
      final String name = entry.getKey();
      final BoundingBox boundingBox = entry.getValue();
      factory.addMenuItemTitleIcon("bookmark", "Zoom to " + name, "magnifier", this, "zoomTo",
        boundingBox);
    }
    menu.show(this.zoomBookmarkButton, 0, 20);
  }

  public void zoom(final com.vividsolutions.jts.geom.Point mapPoint, final int steps) {
    final BoundingBox extent = getBoundingBox();
    double factor = steps * 2;
    if (factor < 0) {
      factor = 1 / -factor;
    }

    final double x = mapPoint.getX();
    final double x1 = extent.getMinX();
    final double width = extent.getWidth();
    final double newWidth = width * factor;
    final double deltaX = x - x1;
    final double percentX = deltaX / width;
    final double newDeltaX = newWidth * percentX;
    final double newX1 = x - newDeltaX;

    final double y = mapPoint.getY();
    final double y1 = extent.getMinY();
    final double height = extent.getHeight();
    final double newHeight = height * factor;
    final double deltaY = y - y1;
    final double percentY = deltaY / height;
    final double newDeltaY = newHeight * percentY;
    final double newY1 = y - newDeltaY;

    final GeometryFactory newGeometryFactory = extent.getGeometryFactory();
    final BoundingBox newBoundingBox = new BoundingBox(newGeometryFactory, newX1, newY1, newX1
      + newWidth, newY1 + newHeight);
    setBoundingBox(newBoundingBox);
  }

  public void zoomIn() {
    final double scale = getScale();
    final double newScale = getZoomInScale(scale);
    setScale(newScale);
  }

  public void zoomNext() {
    setZoomHistoryIndex(this.zoomHistoryIndex + 1);
  }

  public void zoomOut() {
    final double scale = getScale();
    final double newScale = getZoomOutScale(scale);
    setScale(newScale);
  }

  public void zoomPrevious() {
    setZoomHistoryIndex(this.zoomHistoryIndex - 1);
  }

  /**
   * Zoom to the bounding box with a 5% padding on each side
   *
   * @param boundingBox
   */
  public void zoomTo(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.convert(geometryFactory).expandPercent(0.1);
    setBoundingBox(boundingBox);
  }

  public void zoomTo(final Geometry geometry) {
    if (geometry != null) {
      final Geometry convertedGeometry = getGeometryFactory().copy(geometry);
      final BoundingBox boudingBox = BoundingBox.getBoundingBox(convertedGeometry);
      zoomTo(boudingBox);
    }
  }

  public void zoomTo(final Layer layer) {
    if (layer != null && layer.isExists() && layer.isVisible()) {
      final BoundingBox boundingBox = layer.getBoundingBox(true);
      zoomTo(boundingBox);
    }
  }

  public void zoomTo(final Record object) {
    if (object != null) {
      final Geometry geometry = object.getGeometry();
      zoomTo(geometry);
    }
  }

  public void zoomToSelected() {
    zoomToSelected(this.project);
  }

  public void zoomToSelected(final Layer layer) {
    final BoundingBox boundingBox = layer.getSelectedBoundingBox();
    if (!boundingBox.isEmpty()) {
      zoomTo(boundingBox);
    }
  }

  public void zoomToWorld() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final BoundingBox boundingBox = coordinateSystem.getAreaBoundingBox();
    setBoundingBox(boundingBox);
  }

}
