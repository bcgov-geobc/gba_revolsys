package com.revolsys.swing.map.layer.record.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePanel;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePreview;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryStyleRenderer extends AbstractRecordLayerRenderer {

  private static final Icon ICON = Icons.getIcon("style_geometry");

  public static GeneralPath getLineShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo(15, 0);
    path.lineTo(0, 15);
    path.lineTo(15, 15);
    return path;
  }

  public static GeneralPath getPolygonShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo(7, 0);
    path.lineTo(15, 8);
    path.lineTo(15, 15);
    path.lineTo(8, 15);
    path.lineTo(0, 7);
    path.lineTo(0, 0);
    path.closePath();
    return path;
  }

  public static Shape getShape(final Viewport2D viewport, final GeometryStyle style,
    final Geometry geometry) {
    final BoundingBox viewExtent = viewport.getBoundingBox();
    if (geometry != null) {
      if (!viewExtent.isEmpty()) {
        final BoundingBox geometryExtent = BoundingBox.getBoundingBox(geometry);
        if (geometryExtent.intersects(viewExtent)) {
          final GeometryFactory geometryFactory = viewport.getGeometryFactory();
          final Geometry convertedGeometry = geometryFactory.createGeometry(geometry);
          // TODO clipping
          return GeometryShapeUtil.toShape(viewport, convertedGeometry);
        }
      }
    }
    return null;
  }

  public static final void renderGeometry(final Viewport2D viewport, final Graphics2D graphics,
    final Geometry geometry, final GeometryStyle style) {
    if (geometry != null) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometryN(i);
        if (part instanceof Point) {
          final Point point = (Point)part;
          MarkerStyleRenderer.renderMarker(viewport, graphics, point, style);
        } else if (part instanceof LineString) {
          final LineString lineString = (LineString)part;
          renderLineString(viewport, graphics, lineString, style);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          renderPolygon(viewport, graphics, polygon, style);
        }
      }
    }
  }

  public static final void renderLineString(final Viewport2D viewport, final Graphics2D graphics,
    final LineString lineString, final GeometryStyle style) {
    final Shape shape = getShape(viewport, style, lineString);
    if (shape != null) {
      final boolean savedUseModelUnits = viewport.setUseModelCoordinates(false, graphics);
      final Paint paint = graphics.getPaint();
      try {
        style.setLineStyle(viewport, graphics);
        graphics.draw(shape);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  public static final void renderOutline(final Viewport2D viewport, final Graphics2D graphics,
    final Geometry geometry, final GeometryStyle style) {
    if (geometry != null) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometryN(i);
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          MarkerStyleRenderer.renderMarker(viewport, graphics, point, style);
        } else if (part instanceof LineString) {
          final LineString lineString = (LineString)part;
          renderLineString(viewport, graphics, lineString, style);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          renderLineString(viewport, graphics, polygon.getExteriorRing(), style);
          for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
            final LineString ring = polygon.getInteriorRingN(j);
            renderLineString(viewport, graphics, ring, style);
          }
        }
      }
    }
  }

  public static final void renderPolygon(final Viewport2D viewport, final Graphics2D graphics,
    final Polygon polygon, final GeometryStyle style) {
    final Shape shape = getShape(viewport, style, polygon);
    if (shape != null) {
      final boolean savedUseModelUnits = viewport.setUseModelCoordinates(false, graphics);
      final Paint paint = graphics.getPaint();
      try {
        style.setFillStyle(viewport, graphics);
        graphics.fill(shape);
        style.setLineStyle(viewport, graphics);
        graphics.draw(shape);
      } finally {
        viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
        graphics.setPaint(paint);
      }
    }
  }

  private GeometryStyle style;

  public GeometryStyleRenderer(final AbstractRecordLayer layer) {
    this(layer, new GeometryStyle());
  }

  public GeometryStyleRenderer(final AbstractRecordLayer layer, final GeometryStyle style) {
    this(layer, null, style);
  }

  public GeometryStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    this(layer, parent, new GeometryStyle());
  }

  public GeometryStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent,
    final GeometryStyle style) {
    super("geometryStyle", "Geometry Style", layer, parent);
    this.style = style;
    setIcon(ICON);
  }

  public GeometryStyleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> geometryStyle) {
    super("geometryStyle", "Geometry Style", layer, parent, geometryStyle);
    this.style = new GeometryStyle(geometryStyle);
    setIcon(ICON);
  }

  @Override
  public GeometryStyleRenderer clone() {
    final GeometryStyleRenderer clone = (GeometryStyleRenderer)super.clone();
    clone.style = this.style.clone();
    return clone;
  }

  @Override
  public GeometryStylePanel createStylePanel() {
    return new GeometryStylePanel(this);
  }

  @Override
  public Icon getIcon() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return super.getIcon();
    } else {
      final GeometryStyle geometryStyle = getStyle();
      Shape shape = null;
      final DataType geometryDataType = layer.getGeometryType();
      if (DataTypes.POINT.equals(geometryDataType)
          || DataTypes.MULTI_POINT.equals(geometryDataType)) {
        return this.style.getMarker().getIcon(geometryStyle);
      } else if (DataTypes.LINE_STRING.equals(geometryDataType)
          || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        shape = GeometryStylePreview.getLineShape(16);
      } else if (DataTypes.POLYGON.equals(geometryDataType)
          || DataTypes.POLYGON.equals(geometryDataType)) {
        shape = getPolygonShape();
      } else {
        return super.getIcon();
      }

      final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D graphics = image.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (DataTypes.POLYGON.equals(geometryDataType)) {
        graphics.setPaint(geometryStyle.getPolygonFill());
        graphics.fill(shape);
      }
      final Color color = geometryStyle.getLineColor();
      graphics.setColor(color);

      graphics.draw(shape);
      graphics.dispose();
      return new ImageIcon(image);

    }
  }

  public GeometryStyle getStyle() {
    return this.style;
  }

  @Override
  public void renderRecord(final Viewport2D viewport, final Graphics2D graphics,
    final BoundingBox visibleArea, final AbstractRecordLayer layer, final LayerRecord object) {
    final Geometry geometry = object.getGeometryValue();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    renderGeometry(viewport, graphics, geometry, this.style);
  }

  public void setStyle(final GeometryStyle style) {
    this.style = style;
    getPropertyChangeSupport().firePropertyChange("style", null, style);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (this.style != null) {
      final Map<String, Object> styleMap = this.style.toMap();
      map.putAll(styleMap);
    }
    return map;
  }
}
