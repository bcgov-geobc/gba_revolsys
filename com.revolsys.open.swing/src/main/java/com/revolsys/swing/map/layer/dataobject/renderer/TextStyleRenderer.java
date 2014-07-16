package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.swing.Icon;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.CoordinatesWithOrientation;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.util.PointUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.style.TextStyle;
import com.revolsys.swing.map.layer.dataobject.style.panel.TextStylePanel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TextStyleRenderer extends AbstractDataObjectLayerRenderer {

  private static final AffineTransform NOOP_TRANSFORM = AffineTransform.getTranslateInstance(
    0, 0);

  private static final Icon ICON = SilkIconLoader.getIcon("style_text");

  public static String getLabel(final DataObject object, final TextStyle style) {
    if (object == null) {
      return "Text";
    } else {
      final StringBuffer label = new StringBuffer();
      final String labelPattern = style.getTextName();
      final Matcher matcher = Pattern.compile("\\[([\\w.]+)\\]").matcher(
        labelPattern);
      while (matcher.find()) {
        final String propertyName = matcher.group(1);
        final Object value = object.getValueByPath(propertyName);
        String text;
        if (value == null) {
          text = "";
        } else {
          text = StringConverterRegistry.toString(value);
        }
        matcher.appendReplacement(label, text);
      }
      matcher.appendTail(label);

      return label.toString().trim();
    }
  }

  public static CoordinatesWithOrientation getTextLocation(
    final Viewport2D viewport, final Geometry geometry, final TextStyle style) {
    if (viewport == null) {
      return new CoordinatesWithOrientation(new DoubleCoordinates(0.0, 0.0), 0);
    }
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    if (viewportGeometryFactory != null) {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);

      Coordinates point = null;

      double orientation = style.getTextOrientation();
      final String placementType = style.getTextPlacementType();
      final Matcher matcher = Pattern.compile("point\\((.*)\\)").matcher(
        placementType);
      final CoordinatesList points = CoordinatesListUtil.get(geometry);
      final int numPoints = points.size();
      if (numPoints == 1) {
        point = points.get(0);
        point = ProjectionFactory.convert(point, geometryFactory,
          viewportGeometryFactory);

        return new CoordinatesWithOrientation(point, 0);
      } else if (numPoints > 1) {
        final boolean matches = matcher.matches();
        if (matches) {
          final String argument = matcher.group(1);
          int index;
          if (argument.matches("n(?:\\s*-\\s*(\\d+)\\s*)?")) {
            final String indexString = argument.replaceAll("[^0-9]+", "");
            index = numPoints - 1;
            if (indexString.length() > 0) {
              index -= Integer.parseInt(indexString);
            }
            if (index == 0) {
              index++;
            }
            point = ProjectionFactory.convert(points.get(index),
              geometryFactory, viewportGeometryFactory);
            final Coordinates p2 = ProjectionFactory.convert(
              points.get(index - 1), geometryFactory, viewportGeometryFactory);
            final double angle = Math.toDegrees(p2.angle2d(point));
            orientation += angle;
          } else {
            index = Integer.parseInt(argument);
            if (index + 1 == numPoints) {
              index--;
            }
            point = ProjectionFactory.convert(points.get(index),
              geometryFactory, viewportGeometryFactory);
            final Coordinates p2 = ProjectionFactory.convert(
              points.get(index + 1), geometryFactory, viewportGeometryFactory);
            final double angle = Math.toDegrees(point.angle2d(p2));
            orientation += angle;
          }

        } else if ("center".equals(placementType)) {
          if (geometry instanceof LineString && geometry.getNumPoints() > 1) {
            final double totalLength = geometry.getLength();
            final double centreLength = totalLength / 2;
            double currentLength = 0;
            for (int i = 1; i < numPoints && currentLength < centreLength; i++) {
              Coordinates p1 = points.get(i - 1);
              Coordinates p2 = points.get(i);
              final double segmentLength = p1.distance(p2);
              if (segmentLength + currentLength >= centreLength) {
                p1 = ProjectionFactory.convert(p1, geometryFactory,
                  viewportGeometryFactory);
                p2 = ProjectionFactory.convert(p2, geometryFactory,
                  viewportGeometryFactory);
                point = LineSegmentUtil.project(geometryFactory, p1, p2,
                  (centreLength - currentLength) / segmentLength);

                final double angle = Math.toDegrees(p1.angle2d(p2));
                orientation += angle;
              }
              currentLength += segmentLength;
            }
          }
        }
        if (point == null) {
          PointUtil.getPointWithin(geometry);
          point = CoordinatesUtil.get(geometryFactory.copy(geometry.getCentroid()));
          if (!viewport.getBoundingBox().contains(point)) {
            final Geometry clippedGeometry = viewport.getBoundingBox()
              .toPolygon()
              .intersection(geometry);
            if (!clippedGeometry.isEmpty()) {
              double maxArea = 0;
              double maxLength = 0;
              for (int i = 0; i < clippedGeometry.getNumGeometries(); i++) {
                final Geometry part = clippedGeometry.getGeometryN(i);
                if (part instanceof Polygon) {
                  final double area = part.getArea();
                  if (area > maxArea) {
                    maxArea = area;
                    point = CoordinatesUtil.get(part.getCentroid());
                  }
                } else if (part instanceof LineString) {
                  if (maxArea == 0) {
                    final double length = part.getLength();
                    if (length > maxLength) {
                      maxLength = length;
                      point = CoordinatesUtil.get(part.getCentroid());
                    }
                  }
                } else if (part instanceof Point) {
                  if (maxArea == 0 && maxLength == 0) {
                    point = CoordinatesUtil.get(part);
                  }
                }
              }
            }
          }
        }

        if (point != null) {
          final String orientationType = style.getTextOrientationType();
          if ("none".equals(orientationType)) {
            orientation = 0;
          }
          return new CoordinatesWithOrientation(point, orientation);
        }
      }
    }
    return null;
  }

  public static final void renderText(final Viewport2D viewport,
    final Graphics2D graphics, final DataObject object,
    final Geometry geometry, final TextStyle style) {
    final String label = getLabel(object, style);
    if (StringUtils.hasText(label) && geometry != null || viewport == null) {

      final CoordinatesWithOrientation point = getTextLocation(viewport,
        geometry, style);
      if (point != null) {
        final double orientation = point.getOrientation();

        boolean savedUseModelUnits = false;
        if (viewport != null) {
          savedUseModelUnits = viewport.setUseModelCoordinates(false, graphics);
        }
        final Paint paint = graphics.getPaint();
        final Composite composite = graphics.getComposite();
        try {
          graphics.setBackground(Color.BLACK);
          style.setTextStyle(viewport, graphics);
          graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

          final double x = point.getX();
          final double y = point.getY();
          final double[] location;
          if (viewport == null) {
            location = new double[] {
              x, y
            };
          } else {
            location = viewport.toViewCoordinates(x, y);
          }

          final AffineTransform savedTransform = graphics.getTransform();

          style.setTextStyle(viewport, graphics);

          final Measure<Length> textDx = style.getTextDx();
          double dx = Viewport2D.toDisplayValue(viewport, textDx);

          final Measure<Length> textDy = style.getTextDy();
          double dy = -Viewport2D.toDisplayValue(viewport, textDy);

          final FontMetrics fontMetrics = graphics.getFontMetrics();

          double maxWidth = 0;
          final String[] lines = label.split("[\\r\\n]");
          for (final String line : lines) {
            final Rectangle2D bounds = fontMetrics.getStringBounds(line,
              graphics);
            final double width = bounds.getWidth();
            maxWidth = Math.max(width, maxWidth);
          }
          final int descent = fontMetrics.getDescent();
          final int ascent = fontMetrics.getAscent();
          final int leading = fontMetrics.getLeading();
          final double maxHeight = lines.length * (ascent + descent)
            + (lines.length - 1) * leading;
          final String verticalAlignment = style.getTextVerticalAlignment();
          if ("top".equals(verticalAlignment)) {
          } else if ("middle".equals(verticalAlignment)) {
            dy -= maxHeight / 2 + 10;
          } else {
            dy -= maxHeight;
          }

          String horizontalAlignment = style.getTextHorizontalAlignment();
          double screenX = location[0];
          double screenY = location[1];
          final String textPlacementType = style.getTextPlacementType();
          if ("auto".equals(textPlacementType) && viewport != null) {
            if (screenX < 0) {
              screenX = 1;
              dx = 0;
              horizontalAlignment = "left";
            }
            final int viewWidth = viewport.getViewWidthPixels();
            if (screenX + maxWidth > viewWidth) {
              screenX = (int)(viewWidth - maxWidth - 1);
              dx = 0;
              horizontalAlignment = "left";
            }
            if (screenY < maxHeight) {
              screenY = 1;
              dy = 0;
            }
            final int viewHeight = viewport.getViewHeightPixels();
            if (screenY > viewHeight) {
              screenY = viewHeight - 1 - maxHeight;
              dy = 0;
            }
          }
          graphics.translate(screenX, screenY);
          if (orientation != 0) {
            graphics.rotate(-Math.toRadians(orientation), 0, 0);
          }
          graphics.translate(dx, dy);

          for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            graphics.translate(0, ascent);
            final AffineTransform lineTransform = graphics.getTransform();
            final Rectangle2D bounds = fontMetrics.getStringBounds(line,
              graphics);
            final double width = bounds.getWidth();
            final double height = bounds.getHeight();

            if ("right".equals(horizontalAlignment)) {
              graphics.translate(-width, 0);
            } else if ("center".equals(horizontalAlignment)) {
              graphics.translate(-width / 2, 0);
            }
            graphics.translate(dx, 0);

            graphics.scale(1, 1);
            if (Math.abs(orientation) > 90) {
              graphics.rotate(Math.PI, maxWidth / 2, -height / 4);
            }

            final int textBoxOpacity = style.getTextBoxOpacity();
            final Color textBoxColor = style.getTextBoxColor();
            if (textBoxOpacity > 0 && textBoxColor != null) {
              graphics.setColor(textBoxColor);
              graphics.fill(new Rectangle2D.Double(bounds.getX() - 2,
                bounds.getY() - 1, width + 4, height + 2));
            }

            if (textBoxOpacity > 0 && textBoxOpacity < 255) {
              graphics.setComposite(AlphaComposite.SrcOut);
            } else {
              graphics.setComposite(AlphaComposite.SrcOver);
            }

            final double textHaloRadius = Viewport2D.toDisplayValue(viewport,
              style.getTextHaloRadius());
            if (textHaloRadius > 0) {
              graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
              graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
              final Stroke savedStroke = graphics.getStroke();
              final Stroke outlineStroke = new BasicStroke(
                (float)(textHaloRadius + 1), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL);
              graphics.setColor(style.getTextHaloFill());
              graphics.setStroke(outlineStroke);
              final Font font = graphics.getFont();
              final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
              final TextLayout textLayout = new TextLayout(line, font,
                fontRenderContext);
              final Shape outlineShape = textLayout.getOutline(NOOP_TRANSFORM);
              graphics.draw(outlineShape);
              graphics.setStroke(savedStroke);
            }

            graphics.setColor(style.getTextFill());
            graphics.drawString(line, (float)0, (float)0);
            graphics.setTransform(lineTransform);
            graphics.translate(0, leading + descent);
          }
          graphics.setTransform(savedTransform);

        } finally {
          graphics.setPaint(paint);
          graphics.setComposite(composite);
          if (viewport != null) {
            viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
          }
        }
      }
    }
  }

  private TextStyle style;

  public TextStyleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public TextStyleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> textStyle) {
    super("textStyle", "Text Style", layer, parent, textStyle);
    this.style = new TextStyle(textStyle);
    setIcon(ICON);
  }

  @Override
  public TextStyleRenderer clone() {
    final TextStyleRenderer clone = (TextStyleRenderer)super.clone();
    clone.style = this.style.clone();
    return clone;
  }

  @Override
  public ValueField createStylePanel() {
    return new TextStylePanel(this);
  }

  public TextStyle getStyle() {
    return this.style;
  }

  @Override
  public void renderRecord(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final AbstractDataObjectLayer layer, final LayerDataObject object) {
    final Geometry geometry = object.getGeometryValue();
    renderText(viewport, graphics, object, geometry, this.style);
  }

  public void setStyle(final TextStyle style) {
    this.style = style;
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
