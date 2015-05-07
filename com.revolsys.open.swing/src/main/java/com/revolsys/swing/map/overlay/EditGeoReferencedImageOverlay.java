package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.raster.BufferedGeoReferencedImage;
import com.revolsys.raster.GeoReferencedImage;
import com.revolsys.raster.MappedLocation;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.revolsys.swing.undo.ListAddUndo;
import com.revolsys.swing.undo.SetObjectProperty;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class EditGeoReferencedImageOverlay extends AbstractOverlay {
  private static final Cursor CURSOR_MOVE_IMAGE = Icons.getCursor(
    "cursor_move", 8, 7);

  private static final Color COLOR_OUTLINE = WebColors.Black;

  private static final Color COLOR_SELECT = WebColors.Cyan;

  private static final long serialVersionUID = 1L;

  private static final GeometryStyle STYLE_MAPPED_LINE = GeometryStyle.line(
    COLOR_SELECT, 3);

  private static final GeometryStyle STYLE_IMAGE_LINE = GeometryStyle.line(
    COLOR_SELECT, 1);

  private static final MarkerStyle STYLE_VERTEX_FIRST_POINT = MarkerStyle.marker(
    SelectedRecordsRenderer.firstVertexShape(), 9, COLOR_OUTLINE, 1,
    COLOR_SELECT);

  private static final MarkerStyle STYLE_VERTEX_LAST_POINT = MarkerStyle.marker(
    SelectedRecordsRenderer.lastVertexShape(), 9, COLOR_OUTLINE, 1,
    COLOR_SELECT);

  static {
    STYLE_VERTEX_FIRST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_FIRST_POINT.setMarkerPlacement("point(0)");
    STYLE_VERTEX_FIRST_POINT.setMarkerHorizontalAlignment("center");

    STYLE_VERTEX_LAST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_LAST_POINT.setMarkerPlacement("point(n)");
    STYLE_VERTEX_LAST_POINT.setMarkerHorizontalAlignment("right");
  }

  private Point addTiePointFirstPoint;

  private GeoReferencedImage image;

  private GeoReferencedImageLayer layer;

  private Point moveCornerOppositePoint;

  private Point moveCornerPoint;

  private Point moveImageFirstPoint;

  private BoundingBox moveImageBoundingBox;

  private Point addTiePointMove;

  private GeoReferencedImage cachedImage;

  private int moveTiePointIndex = -1;

  private java.awt.Point moveTiePointEventPoint;

  private final List<Integer> closeSourcePixelIndexes = new ArrayList<Integer>();

  private final List<Integer> closeTargetPointIndexes = new ArrayList<Integer>();

  private Cursor moveCornerCursor;

  private boolean moveTiePointStarted;

  private boolean moveTiePointSource;

  private Point moveTiePointLocation;

  public static final String ACTION_MOVE_IMAGE = "moveImage";

  private static final String ACTION_MOVE_IMAGE_CORNER = "moveImageCorner";

  private boolean moveImageEnabled;

  public EditGeoReferencedImageOverlay(final MapPanel map) {
    super(map);
  }

  protected void addTiePointClear() {
    this.addTiePointFirstPoint = null;
    this.addTiePointMove = null;
    clearMapCursor();
    clearCachedImage();
    clearMouseOverGeometry();
  }

  private boolean addTiePointFinish(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (this.addTiePointFirstPoint != null) {
        try {
          Point mapPoint = getViewportPoint(event);
          final Point snapPoint = getSnapPoint();
          if (snapPoint != null) {
            mapPoint = snapPoint;
          }
          final Point sourcePoint = this.addTiePointFirstPoint;
          final Coordinates sourcePixel = this.layer.targetPointToSourcePixel(sourcePoint);
          final GeometryFactory geometryFactory = getImageGeometryFactory();
          final Point targetPoint = geometryFactory.copy(mapPoint);
          final MappedLocation mappedLocation = new MappedLocation(sourcePixel,
            targetPoint);
          addUndo(new ListAddUndo(this.image.getTiePoints(), mappedLocation));
        } finally {
          addTiePointClear();
        }
        return true;
      }
    }
    return false;
  }

  private boolean addTiePointMove(final MouseEvent event) {
    if (this.addTiePointFirstPoint != null) {
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      hasSnapPoint(event, boundingBox);

      if (getSnapPoint() == null) {
        this.addTiePointMove = getViewportPoint(event);
      } else {
        this.addTiePointMove = getSnapPoint();
      }
      repaint();
      event.consume();
      return true;
    }
    return false;
  }

  private boolean addTiePointStart(final MouseEvent event) {
    if (this.layer != null) {
      if (addTiePointFinish(event)) {
      } else if (SwingUtil.isLeftButtonAndAltDown(event)) {
        final Point mousePoint = getViewportPoint(event);
        if (getImageBoundingBox().contains(mousePoint)) {
          this.addTiePointFirstPoint = mousePoint;
          event.consume();
          return true;
        }
      }
    }
    return false;
  }

  protected void adjustBoundingBoxAspectRatio() {
    if (this.moveImageBoundingBox != null && this.moveCornerPoint != null) {
      final double imageAspectRatio = this.image.getImageAspectRatio();
      final BoundingBox boundingBox = this.moveImageBoundingBox;
      final double aspectRatio = boundingBox.getAspectRatio();
      double minX = boundingBox.getMinX();
      double maxX = boundingBox.getMaxX();
      double minY = boundingBox.getMinY();
      double maxY = boundingBox.getMaxY();
      final double width = boundingBox.getWidth();
      final double height = boundingBox.getHeight();
      if (aspectRatio < imageAspectRatio) {
        if (minX == this.moveCornerOppositePoint.getX()) {
          maxX = minX + height * imageAspectRatio;
        } else {
          minX = maxX - height * imageAspectRatio;
        }
      } else if (aspectRatio > imageAspectRatio) {
        if (minY == this.moveCornerOppositePoint.getY()) {
          maxY = minY + width / imageAspectRatio;
        } else {
          minY = maxY - width / imageAspectRatio;
        }
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      this.moveImageBoundingBox = new BoundingBox(geometryFactory, minX, minY,
        maxX, maxY);
    }
  }

  protected void appendTiePointLocation(final StringBuffer toolTip,
    final List<MappedLocation> tiePoints, final List<Integer> indices,
    final int startNumber, final boolean source) {
    if (!indices.isEmpty()) {
      int i = startNumber - 1;
      toolTip.append("<div style=\"border-bottom: solid black 1px; font-weight:bold;padding: 1px 3px 1px 3px\">");
      if (source) {
        toolTip.append("Move source pixel");
      } else {
        toolTip.append("Move target point");
      }
      toolTip.append("</div>");
      toolTip.append("<div style=\"padding: 1px 3px 1px 3px\">");
      toolTip.append("<ol start=\"");
      toolTip.append(startNumber);
      toolTip.append("\" style=\"margin: 1px 3px 1px 15px\">");
      for (final Integer index : indices) {
        final MappedLocation tiePoint = tiePoints.get(index);
        final Object value;
        if (source) {
          value = tiePoint.getSourcePixel();
        } else {
          value = tiePoint.getTargetPoint();
        }
        toolTip.append("<li");
        if (i == this.moveTiePointIndex) {
          toolTip.append(" style=\"border: 1px solid red; padding: 2px; background-color:#FFC0CB\"");
        }
        toolTip.append(">#");
        toolTip.append(index + 1);
        toolTip.append(" ");
        toolTip.append(value);
        toolTip.append("</li>");
      }
      toolTip.append("</ol></div>");
      i++;
    }
  }

  protected void cancel() {
    moveImageClear();
    moveCornerClear();
    this.moveTiePointEventPoint = null;
    this.moveTiePointLocation = null;
    addTiePointClear();
    this.closeSourcePixelIndexes.clear();
    this.closeTargetPointIndexes.clear();
    clearCachedImage();
    getMap().repaint();
  }

  protected void clear() {
    this.image = null;
    this.layer = null;
    clearUndoHistory();
    cancel();
  }

  protected void clearCachedImage() {
    this.cachedImage = null;
    System.gc();
  }

  private GeoReferencedImage getCachedImage(BoundingBox boundingBox) {
    boundingBox = boundingBox.convert(getViewportGeometryFactory());
    final Viewport2D viewport = getViewport();
    final BoundingBox viewBoundingBox = viewport.getBoundingBox();
    if (this.cachedImage == null
      || !this.cachedImage.getBoundingBox().equals(viewBoundingBox)) {
      final ImageViewport imageViewport = new ImageViewport(viewport);

      final BufferedImage image = imageViewport.getImage();
      final Graphics2D graphics = (Graphics2D)image.getGraphics();

      this.image.drawImage(graphics, viewBoundingBox,
        viewport.getViewWidthPixels(), viewport.getViewHeightPixels(),
        !this.layer.isShowOriginalImage());
      GeoReferencedImageLayerRenderer.render(imageViewport, graphics,
        this.image, !this.layer.isShowOriginalImage());
      this.cachedImage = new BufferedGeoReferencedImage(
        imageViewport.getBoundingBox(), image);
    }
    return this.cachedImage;
  }

  public BoundingBox getImageBoundingBox() {
    if (this.image == null) {
      return new BoundingBox();
    } else {
      return this.layer.getBoundingBox();
    }
  }

  public GeometryFactory getImageGeometryFactory() {
    if (this.image == null) {
      return getGeometryFactory();
    } else {
      return this.layer.getGeometryFactory();
    }
  }

  public GeoReferencedImageLayer getLayer() {
    return this.layer;
  }

  protected BoundingBox getMoveBoundingBox(final MouseEvent event) {
    BoundingBox boundingBox = getImageBoundingBox();
    final Point mousePoint = getViewportPoint(event);
    final GeometryFactory imageGeometryFactory = getImageGeometryFactory();
    final Point imagePoint = imageGeometryFactory.copy(mousePoint);

    final double deltaX = imagePoint.getX() - this.moveImageFirstPoint.getX();
    final double deltaY = imagePoint.getY() - this.moveImageFirstPoint.getY();
    boundingBox = boundingBox.move(deltaX, deltaY);
    return boundingBox;
  }

  private MappedLocation getMoveTiePoint() {
    if (this.moveTiePointIndex > -1) {
      int tiePointIndex;
      final int targetSize = this.closeTargetPointIndexes.size();
      if (this.moveTiePointIndex < targetSize) {
        tiePointIndex = this.closeTargetPointIndexes.get(this.moveTiePointIndex);
        this.moveTiePointSource = false;
      } else if (this.moveTiePointIndex - targetSize < this.closeSourcePixelIndexes.size()) {
        tiePointIndex = this.closeSourcePixelIndexes.get(this.moveTiePointIndex
          - targetSize);
        this.moveTiePointSource = true;
      } else {
        return null;
      }
      return this.image.getTiePoints().get(tiePointIndex);
    }
    return null;
  }

  protected boolean isApplicable(final MouseEvent event) {
    final BoundingBox imageBoundingBox = getImageBoundingBox();
    final Point point = getPoint(event);
    final double distance = getDistance(event);

    return imageBoundingBox.distance(point) < distance * 2;
  }

  private boolean isInImage() {
    final Point mousePoint = getMousePoint();
    return isInImage(mousePoint);
  }

  private boolean isInImage(final MouseEvent event) {
    final Point mousePoint = getPoint(event);
    final boolean inImage = isInImage(mousePoint);
    return inImage;
  }

  private boolean isInImage(final Point mousePoint) {
    if (mousePoint == null) {
      return false;
    } else {
      final BoundingBox imageBoundingBox = getImageBoundingBox();
      final boolean inImage = imageBoundingBox.contains(mousePoint);
      return inImage;
    }
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    if (this.layer != null) {
      final int keyCode = event.getKeyCode();
      if (this.moveCornerPoint != null) {
        if (keyCode == KeyEvent.VK_SHIFT) {
          adjustBoundingBoxAspectRatio();
          repaint();
          event.consume();
        } else if (keyCode == KeyEvent.VK_CONTROL
          || keyCode == KeyEvent.VK_META) {
          event.consume();
        }
      } else {
        if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
          if (isInImage()) {
            setMapCursor(CURSOR_MOVE_IMAGE);
            event.consume();
          }
        }
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent event) {
    if (this.layer != null) {
      final int keyCode = event.getKeyCode();
      if (this.moveTiePointIndex > -1 || this.addTiePointFirstPoint != null
        || this.moveTiePointStarted) {
        final char keyChar = event.getKeyChar();
        if (keyChar >= '1' && keyChar <= '9') {
          event.consume();
        }
      }
      if (keyCode == KeyEvent.VK_ESCAPE) {
        cancel();
        repaint();
      } else if (keyCode == KeyEvent.VK_CONTROL) {
        if (this.moveImageFirstPoint == null) {
          moveImageClear();
        }
      }

    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    final char keyChar = e.getKeyChar();
    if (keyChar >= '1' && keyChar <= '9') {
      final int index = keyChar - '1';
      if (!this.moveTiePointStarted && this.moveTiePointIndex > -1) {
        if (index < this.closeSourcePixelIndexes.size()
          + this.closeTargetPointIndexes.size()) {

          this.moveTiePointIndex = index;
          setMoveTiePointToolTip();
        }
        e.consume();
      } else if (index < getSnapPointLocationMap().size()) {
        setSnapPointIndex(index);
        setSnapLocations(getSnapPointLocationMap());
        if (this.moveTiePointStarted) {
          if (!this.moveTiePointSource) {
            moveTiePointFinish(null);
            e.consume();
          }
        } else if (this.addTiePointFirstPoint != null) {
          addTiePointFinish(null);
          e.consume();
        }
        getMap().repaint();
      }

    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (this.layer != null) {
      if (isApplicable(event)) {
        if (addTiePointFinish(event)) {
        } else if (addTiePointStart(event)) {
        }
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (this.layer != null) {
      if (moveTiePointDrag(event)) {
      } else if (moveCornerDrag(event)) {
      } else if (moveImageDrag(event)) {
      }
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (this.layer != null) {
      if (moveTiePointMove(event)) {
      } else if (moveCornerMove(event)) {
      } else if (moveImageMove(event)) {
      } else if (addTiePointMove(event)) {
      } else if (!hasOverlayAction()) {
        clearMapCursor();
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (this.layer != null) {
      if (moveTiePointStart(event)) {
      } else if (isInImage() && SwingUtil.isLeftButtonAndAltDown(event)) {
        event.consume();
      } else if (moveCornerStart(event)) {
      } else if (moveImageStart(event)) {
      }
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (this.layer != null) {
      if (event.getButton() == MouseEvent.BUTTON1) {
        if (moveTiePointFinish(event)) {
        } else if (moveCornerFinish(event)) {
        } else if (moveCornerFinish(event)) {
        } else if (moveImageFinish(event)) {
        }
      }
    }
  }

  protected void moveCornerClear() {
    clearOverlayAction(ACTION_MOVE_IMAGE_CORNER);
    clearMapCursor(this.moveCornerCursor);
    this.moveImageBoundingBox = null;
    this.moveCornerCursor = null;
    this.moveCornerOppositePoint = null;
    this.moveCornerPoint = null;
  }

  private boolean moveCornerDrag(final MouseEvent event) {
    if (this.moveCornerPoint == null) {
      return false;
    } else {
      final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
      final Point mousePoint = getViewportPoint(event);
      this.moveImageBoundingBox = new BoundingBox(viewportGeometryFactory,
        CoordinatesUtil.get(mousePoint),
        CoordinatesUtil.get(this.moveCornerOppositePoint));

      if (SwingUtil.isShiftDown(event)) {
        adjustBoundingBoxAspectRatio();
      }
      setMapCursor(this.moveCornerCursor);
      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveCornerFinish(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (clearOverlayAction(ACTION_MOVE_IMAGE_CORNER)) {
        try {
          final SetObjectProperty setBBox = new SetObjectProperty(this,
            "imageBoundingBox", getImageBoundingBox(),
            this.moveImageBoundingBox);
          addUndo(setBBox);
          event.consume();
        } finally {
          moveCornerClear();
        }
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean moveCornerMove(final MouseEvent event) {
    if (this.layer != null) {
      final Point oldPoint = this.moveCornerPoint;

      final Point mousePoint = getViewportPoint(event);
      final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();

      Point closestPoint = null;
      final double maxDistance = getDistance(event);
      double closestDistance = Double.MAX_VALUE;
      if (oldPoint != null) {
        final double distance = oldPoint.distance(mousePoint);
        if (distance < maxDistance) {
          closestPoint = oldPoint;
          closestDistance = distance;
        }
      }
      int closestIndex = -1;
      final BoundingBox imageBoundingBox = getImageBoundingBox();
      if (imageBoundingBox.isEmpty()) {
        return false;
      } else {
        final GeometryFactory imageGeometryFactory = imageBoundingBox.getGeometryFactory();
        for (int i = 0; i < 4; i++) {
          final Coordinates point = imageBoundingBox.getCornerPoint(i);
          final Point mapPoint = viewportGeometryFactory.copy(imageGeometryFactory.createPoint(point));
          final double distance = mapPoint.distance(mousePoint);
          if (distance < maxDistance && distance < closestDistance) {
            closestPoint = mapPoint;
            closestDistance = distance;
            closestIndex = i;
          }
        }

        if (closestPoint != oldPoint) {
          clearMapCursor(this.moveCornerCursor);
          this.moveCornerPoint = closestPoint;
          if (closestIndex == -1) {
            this.moveCornerOppositePoint = null;
          } else {
            this.moveCornerOppositePoint = imageGeometryFactory.createPoint(imageBoundingBox.getCornerPoint(closestIndex + 2));
          }
          switch (closestIndex) {
            case 0:
              this.moveCornerCursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            break;
            case 1:
              this.moveCornerCursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            break;
            case 2:
              this.moveCornerCursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            break;
            case 3:
              this.moveCornerCursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            break;
            default:
              this.moveCornerCursor = null;
            break;
          }
          if (this.moveCornerCursor != null) {
            setMapCursor(this.moveCornerCursor);
          }
        }
      }
    }
    if (this.moveCornerPoint == null) {
      return false;
    } else {
      event.consume();
      return true;
    }
  }

  private boolean moveCornerStart(final MouseEvent event) {
    if (this.layer != null) {
      if (event.getButton() == MouseEvent.BUTTON1) {
        if (this.moveCornerPoint != null) {
          if (setOverlayAction(ACTION_MOVE_IMAGE_CORNER)) {
            event.consume();
            return true;
          }
        }
      }
    }
    return false;
  }

  protected void moveImageClear() {
    this.moveImageFirstPoint = null;
    this.moveImageBoundingBox = null;
    clearOverlayAction(ACTION_MOVE_IMAGE);
    clearMapCursor(CURSOR_MOVE_IMAGE);
  }

  private boolean moveImageDrag(final MouseEvent event) {
    if (this.moveImageFirstPoint == null) {
      return false;
    } else {
      setMapCursor(CURSOR_MOVE_IMAGE);
      this.moveImageBoundingBox = getMoveBoundingBox(event);
      repaint();
      event.consume();
      return true;
    }
  }

  private boolean moveImageFinish(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (clearOverlayAction(ACTION_MOVE_IMAGE)) {
        final BoundingBox boundingBox = getMoveBoundingBox(event);
        final SetObjectProperty setBBox = new SetObjectProperty(this,
          "imageBoundingBox", getImageBoundingBox(), boundingBox);
        addUndo(setBBox);
        moveImageClear();
        event.consume();
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean moveImageMove(final MouseEvent event) {
    if (isInImage(event)) {
      if (SwingUtil.isControlOrMetaDown(event)) {
        setMapCursor(CURSOR_MOVE_IMAGE);
        event.consume();
        this.moveImageEnabled = true;
        return true;
      }
    }
    this.moveImageEnabled = false;
    clearMapCursor(CURSOR_MOVE_IMAGE);
    return false;
  }

  private boolean moveImageStart(final MouseEvent event) {
    if (this.layer != null) {
      if (this.moveImageEnabled) {
        if (event.getButton() == MouseEvent.BUTTON1
          && SwingUtil.isControlOrMetaDown(event)) {
          if (setOverlayAction(ACTION_MOVE_IMAGE)) {
            final Point mousePoint = getViewportPoint(event);
            final GeometryFactory imageGeometryFactory = getImageGeometryFactory();
            final Point imagePoint = imageGeometryFactory.copy(mousePoint);
            final boolean inImage = isInImage(imagePoint);
            if (inImage) {
              setMapCursor(CURSOR_MOVE_IMAGE);
              this.moveImageFirstPoint = imagePoint;
              event.consume();
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean moveTiePointDrag(final MouseEvent event) {
    if (this.moveTiePointStarted) {
      if (this.moveTiePointSource) {
        this.moveTiePointLocation = getViewportPoint(event);
      } else {
        final BoundingBox boundingBox = getHotspotBoundingBox(event);
        if (hasSnapPoint(event, boundingBox)) {
          this.moveTiePointLocation = getSnapPoint();
        } else {
          this.moveTiePointLocation = getViewportPoint(event);
        }
      }
      event.consume();
      repaint();
      return true;
    }
    return false;
  }

  private boolean moveTiePointFinish(final MouseEvent event) {
    if (this.moveTiePointStarted) {
      final MappedLocation tiePoint = getMoveTiePoint();
      if (tiePoint != null) {
        Point point = getPoint(event);
        if (this.moveTiePointSource) {
          final Coordinates sourcePixel = this.layer.targetPointToSourcePixel(point);

          final SetObjectProperty setSourcePixel = new SetObjectProperty(
            tiePoint, "sourcePixel", tiePoint.getSourcePixel(), sourcePixel);
          addUndo(setSourcePixel);
        } else {
          final Point snapPoint = getSnapPoint();
          if (snapPoint != null) {
            point = snapPoint;
          }
          final GeometryFactory imageGeometryFactory = getImageGeometryFactory();
          point = imageGeometryFactory.copy(point);
          tiePoint.setTargetPoint(point);
          final SetObjectProperty setTargetPoint = new SetObjectProperty(
            tiePoint, "targetPoint", tiePoint.getTargetPoint(), point);
          addUndo(setTargetPoint);
        }
        this.closeSourcePixelIndexes.clear();
        this.closeTargetPointIndexes.clear();
        this.moveTiePointLocation = null;
        this.moveTiePointStarted = false;
        this.moveTiePointIndex = -1;
        clearCachedImage();
        clearMapCursor();
        clearMouseOverGeometry();
        if (event != null) {
          event.consume();
        }
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean moveTiePointMove(final MouseEvent event) {
    if (this.image != null) {
      final List<MappedLocation> tiePoints = this.image.getTiePoints();
      if (!tiePoints.isEmpty()) {
        if (!this.closeSourcePixelIndexes.isEmpty()
          && !this.closeTargetPointIndexes.isEmpty()) {
          clearMapCursor(CURSOR_NODE_EDIT);
        }
        this.closeSourcePixelIndexes.clear();
        this.closeTargetPointIndexes.clear();
        final BoundingBox hotSpot = getHotspotBoundingBox(event);
        int i = 0;

        for (final MappedLocation tiePoint : tiePoints) {
          final Point targetPoint = tiePoint.getTargetPoint();
          final Point sourcePoint = this.layer.sourcePixelToTargetPoint(tiePoint);
          if (hotSpot.contains(sourcePoint)) {
            this.closeSourcePixelIndexes.add(i);
          }
          if (hotSpot.contains(targetPoint)) {
            this.closeTargetPointIndexes.add(i);
          }
          i++;
        }
        this.moveTiePointIndex = 0;
        this.moveTiePointEventPoint = event.getPoint();
        if (setMoveTiePointToolTip()) {
          setMapCursor(CURSOR_NODE_EDIT);
          event.consume();
          return true;
        } else {
          this.moveTiePointEventPoint = null;
          this.moveTiePointIndex = -1;
          getMap().clearToolTipText();
          return false;
        }
      }
    }
    return false;
  }

  // TODO escape and undo for move tie point
  private boolean moveTiePointStart(final MouseEvent event) {
    if (this.moveTiePointIndex > -1) {
      this.moveTiePointStarted = true;
      getMap().clearToolTipText();
      event.consume();
      repaint();
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void paintComponent(final Graphics2D graphics) {
    if (this.layer != null && this.layer.isVisible() && this.layer.isExists()
      && this.image != null) {
      final boolean showOriginalImage = this.layer.isShowOriginalImage();
      BoundingBox boundingBox = getImageBoundingBox();
      BoundingBox outlineBoundingBox = boundingBox;
      if (this.moveImageBoundingBox != null) {
        if (showOriginalImage) {
          boundingBox = this.moveImageBoundingBox;
        }
        outlineBoundingBox = this.moveImageBoundingBox;
      }
      super.paintComponent(graphics);
      final Viewport2D viewport = getViewport();

      final GeoReferencedImage cachedImage = getCachedImage(boundingBox);
      GeoReferencedImageLayerRenderer.renderAlpha(graphics, viewport,
        cachedImage, 0.5f, false);

      if (outlineBoundingBox != null && !outlineBoundingBox.isEmpty()) {
        final Polygon imageBoundary = outlineBoundingBox.toPolygon(1);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);

        GeometryStyleRenderer.renderOutline(viewport, graphics, imageBoundary,
          GeometryStyle.line(Color.GREEN, 3));

        MarkerStyleRenderer.renderMarkerVertices(viewport, graphics,
          imageBoundary,
          MarkerStyle.marker("cross", 11, WebColors.Black, 1, WebColors.Lime));

        final int tiePointCount = this.image.getTiePoints().size();
        final GeometryFactory viewGeometryFactory = getGeometryFactory();
        if (this.image != null && tiePointCount > 0) {
          final MappedLocation moveTiePoint = getMoveTiePoint();
          for (int i = 0; i < tiePointCount; i++) {
            final MappedLocation mappedLocation = this.image.getTiePoints()
                .get(i);
            if (!this.moveTiePointStarted || mappedLocation != moveTiePoint) {
              final LineString line = mappedLocation.getSourceToTargetLine(
                this.image, boundingBox, !showOriginalImage);
              renderTiePointLine(graphics, viewport, line);
            }
          }
          if (this.moveTiePointStarted && moveTiePoint != null
            && this.moveTiePointLocation != null) {
            Point sourcePoint = null;
            Point targetPoint = null;
            final GeometryFactory imageGeometryFactory = getImageGeometryFactory();

            if (this.moveTiePointSource) {
              sourcePoint = imageGeometryFactory.copy(this.moveTiePointLocation);
              targetPoint = moveTiePoint.getTargetPoint();
            } else {
              sourcePoint = moveTiePoint.getSourcePoint(this.image,
                boundingBox, !showOriginalImage);
              targetPoint = imageGeometryFactory.copy(this.moveTiePointLocation);
            }
            if (sourcePoint != null && targetPoint != null) {
              final LineString line = imageGeometryFactory.createLineString(
                sourcePoint, targetPoint);
              renderTiePointLine(graphics, viewport, line);
            }
          }

        }
        if (!showOriginalImage) {

          final double width = this.image.getImageWidth() - 1;
          final double height = this.image.getImageHeight() - 1;
          final double[] targetCoordinates = MappedLocation.toModelCoordinates(
            this.image, boundingBox, true, 0, height, width, height, width, 0,
            0, 0, 0, height);
          final LineString line = viewGeometryFactory.createLineString(new DoubleCoordinatesList(
            2, targetCoordinates));
          GeometryStyleRenderer.renderLineString(viewport, graphics, line,
            STYLE_IMAGE_LINE);
        }
        if (this.addTiePointFirstPoint != null && this.addTiePointMove != null) {
          final LineString line = viewGeometryFactory.createLineString(
            this.addTiePointFirstPoint, this.addTiePointMove);
          renderTiePointLine(graphics, viewport, line);
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (source instanceof GeoReferencedImageLayer) {
      final GeoReferencedImageLayer layer = (GeoReferencedImageLayer)source;
      if ("editable".equals(propertyName)) {
        if (!BooleanStringConverter.getBoolean(event.getNewValue())) {
          if (this.layer == layer) {
            setLayer(null);
          }
        } else {
          setLayer(layer);
        }
      } else if (this.layer == layer) {
        clearCachedImage();
        if ("boundingBox".equals(propertyName)) {
          final BoundingBox boundingBox = layer.getBoundingBox();
          setImageBoundingBox(boundingBox);
        } else if ("hasChanges".equals(propertyName)) {
          clear();
          setLayer(layer);
        } else if ("deleted".equals(propertyName)) {
          clear();
        }
      }
    } else if (source == this.image) {
      clearCachedImage();
    } else if ("scale".equals(propertyName)) {
      clearCachedImage();
    }
  }

  protected void renderTiePointLine(final Graphics2D graphics,
    final Viewport2D viewport, LineString line) {
    if (line != null) {
      GeometryStyleRenderer.renderLineString(viewport, graphics, line,
        STYLE_MAPPED_LINE);
      line = viewport.getGeometryFactory().copy(line);
      MarkerStyleRenderer.renderMarkers(viewport, graphics,
        CoordinatesListUtil.get(line), STYLE_VERTEX_FIRST_POINT,
        STYLE_VERTEX_LAST_POINT, null);
    }
  }

  public void setImageBoundingBox(BoundingBox boundingBox) {
    if (boundingBox == null) {
      boundingBox = new BoundingBox(getGeometryFactory());
    }
    if (this.image != null) {
      this.image.setBoundingBox(boundingBox);
    }
    setGeometryFactory(boundingBox.getGeometryFactory());
    clearCachedImage();
  }

  public void setLayer(final GeoReferencedImageLayer layer) {
    final GeoReferencedImageLayer oldLayer = this.layer;
    if (oldLayer != layer) {
      clear();
      this.layer = layer;
      final Viewport2D viewport = getViewport();
      setGeometryFactory(viewport.getGeometryFactory());
      setEnabled(layer != null);
      if (layer != null) {
        this.image = layer.getImage();
        setImageBoundingBox(layer.getBoundingBox());
      }
      if (oldLayer != null) {
        oldLayer.setEditable(false);
      }
    }
    firePropertyChange("layer", oldLayer, layer);
  }

  protected boolean setMoveTiePointToolTip() {
    this.moveTiePointStarted = false;
    if (!this.closeSourcePixelIndexes.isEmpty()
      || !this.closeTargetPointIndexes.isEmpty()) {
      final List<MappedLocation> tiePoints = this.image.getTiePoints();
      final StringBuffer toolTip = new StringBuffer();
      toolTip.append("<html>");

      appendTiePointLocation(toolTip, tiePoints, this.closeTargetPointIndexes,
        1, false);
      appendTiePointLocation(toolTip, tiePoints, this.closeSourcePixelIndexes,
        this.closeTargetPointIndexes.size() + 1, true);
      toolTip.append("</html>");
      getMap().setToolTipText(this.moveTiePointEventPoint, toolTip);
      return true;
    }
    return false;
  }

}
