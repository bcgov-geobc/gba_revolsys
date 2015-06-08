package com.revolsys.swing.map.util;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class GeometryShapeUtil {
  public static void addCoordinateSequence(final Viewport2D viewport, final GeneralPath path,
    final CoordinateSequence sequence) {
    double x = sequence.getOrdinate(0, 0);
    double y = sequence.getOrdinate(0, 1);
    double[] screenCoords = viewport.toViewCoordinates(x, y);
    float screenX = (float)screenCoords[0];
    float screenY = (float)screenCoords[1];
    path.moveTo(screenX, screenY);
    for (int i = 0; i < sequence.size(); i++) {
      x = sequence.getOrdinate(i, 0);
      y = sequence.getOrdinate(i, 1);
      screenCoords = viewport.toViewCoordinates(x, y);
      screenX = (float)screenCoords[0];
      screenY = (float)screenCoords[1];
      path.lineTo(screenX, screenY);
    }
  }

  public static void addLineString(final Viewport2D viewport, final GeneralPath path,
    final LineString line) {
    final CoordinateSequence sequence = line.getCoordinateSequence();
    addCoordinateSequence(viewport, path, sequence);
  }

  public static Shape toShape(final Viewport2D viewport, final BoundingBox boundingBox) {
    final double x = boundingBox.getMinX();
    final double y = boundingBox.getMinY();
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();
    final Rectangle2D rectangle2d = new Rectangle2D.Double(x, y, width, height);
    return rectangle2d;
  }

  public static Shape toShape(final Viewport2D viewport, final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return toShape(viewport, point);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return toShape(viewport, line);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return toShape(viewport, polygon);
    }
    return null;
  }

  public static Shape toShape(final Viewport2D viewport, final LineString line) {
    final GeneralPath path = new GeneralPath(PathIterator.WIND_EVEN_ODD);
    addLineString(viewport, path, line);
    return path;
  }

  public static Shape toShape(final Viewport2D viewport, final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double boxSize = viewport.getModelUnitsPerViewUnit();
    return new Rectangle2D.Double(x, y, boxSize, boxSize);
  }

  public static Shape toShape(final Viewport2D viewport, final Polygon polygon) {
    final GeneralPath path = new GeneralPath(PathIterator.WIND_EVEN_ODD);
    final LineString exteriorRing = polygon.getExteriorRing();
    addLineString(viewport, path, exteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString interiorRing = polygon.getInteriorRingN(i);
      addLineString(viewport, path, interiorRing);
    }
    return path;
  }

  private GeometryShapeUtil() {
  }
}
