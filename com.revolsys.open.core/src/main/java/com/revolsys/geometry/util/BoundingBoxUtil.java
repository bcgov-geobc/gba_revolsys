package com.revolsys.geometry.util;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.MathUtil;

public class BoundingBoxUtil {
  public static boolean covers(final double minX1, final double minY1, final double maxX1,
    final double maxY1, final double minX2, final double minY2, final double maxX2,
    final double maxY2) {
    return minX2 >= minX1 && maxX2 <= maxX1 && minY2 >= minY1 && maxY2 <= maxY1;
  }

  public static double[] createBounds(final double... bounds) {
    final int axisCount = bounds.length;
    final double[] newBounds = createBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = bounds[axisIndex];
      if (!MathUtil.isNanOrInfinite(coordinate)) {
        newBounds[axisIndex] = coordinate;
        newBounds[axisCount + axisCount] = coordinate;
      }
    }
    return newBounds;
  }

  public static double[] createBounds(final GeometryFactory geometryFactory,
    final double... bounds) {
    final int axisCount = bounds.length;
    final double[] newBounds = createBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double coordinate = bounds[axisIndex];
      if (geometryFactory != null) {
        coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
      }
      if (!MathUtil.isNanOrInfinite(coordinate)) {
        newBounds[axisIndex] = coordinate;
        newBounds[axisCount + axisIndex] = coordinate;
      }
    }
    return newBounds;
  }

  public static double[] createBounds(final GeometryFactory geometryFactory, final int axisCount,
    Point point) {
    point = point.convert(geometryFactory, axisCount);
    final double[] bounds = createBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double coordinate = point.getCoordinate(axisIndex);
      if (geometryFactory != null) {
        coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
      }
      if (!MathUtil.isNanOrInfinite(coordinate)) {
        bounds[axisIndex] = coordinate;
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
    return bounds;
  }

  public static double[] createBounds(final GeometryFactory geometryFactory, final Point point) {
    final int axisCount = point.getAxisCount();
    return createBounds(geometryFactory, axisCount, point);
  }

  public static double[] createBounds(final int axisCount) {
    final double[] newBounds = new double[axisCount * 2];
    for (int i = 0; i < newBounds.length; i++) {
      newBounds[i] = Double.NaN;
    }
    return newBounds;
  }

  public static double[] createBounds(final int axisCount, final Point point) {
    final double[] bounds = createBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = point.getCoordinate(axisIndex);
      if (!MathUtil.isNanOrInfinite(coordinate)) {
        bounds[axisIndex] = coordinate;
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
    return bounds;
  }

  public static double[] createBounds(final Point point) {
    final int axisCount = point.getAxisCount();
    return createBounds(axisCount, point);
  }

  public static void expand(final double[] bounds, final int axisCount,
    final double... coordinates) {
    for (int axisIndex = 0; axisIndex < axisCount && axisIndex < coordinates.length; axisIndex++) {
      final double coordinate = coordinates[axisIndex];
      expand(bounds, axisCount, axisIndex, coordinate);
    }
  }

  public static void expand(final double[] bounds, final int axisCount, final int axisIndex,
    final double coordinate) {
    if (!MathUtil.isNanOrInfinite(coordinate)) {
      final double min = bounds[axisIndex];
      if (coordinate < min || Double.isNaN(min)) {
        bounds[axisIndex] = coordinate;
      }
      final double max = bounds[axisCount + axisIndex];
      if (coordinate > max || Double.isNaN(max)) {
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
  }

  public static void expand(final double[] bounds, final int axisCount, final Point point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = point.getCoordinate(axisIndex);
      expand(bounds, axisCount, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final double... values) {
    final int axisCount = bounds.length / 2;
    for (int i = 0; i < values.length; i++) {
      final double value = values[i];
      final int axisIndex = i % axisCount;
      expand(geometryFactory, bounds, axisCount, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final int axisIndex, double coordinate) {
    if (geometryFactory != null) {
      coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
    }
    if (!MathUtil.isNanOrInfinite(coordinate)) {
      final int axisCount = bounds.length / 2;
      final double min = bounds[axisIndex];
      if (coordinate < min || Double.isNaN(min)) {
        bounds[axisIndex] = coordinate;
      }
      final double max = bounds[axisCount + axisIndex];
      if (coordinate > max || Double.isNaN(max)) {
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final int axisCount, final int axisIndex, double coordinate) {
    if (geometryFactory != null) {
      coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
    }
    if (!MathUtil.isNanOrInfinite(coordinate)) {
      final double min = bounds[axisIndex];
      if (coordinate < min || Double.isNaN(min)) {
        bounds[axisIndex] = coordinate;
      }
      final double max = bounds[axisCount + axisIndex];
      if (coordinate > max || Double.isNaN(max)) {
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    Point point) {
    final int axisCount = bounds.length / 2;
    point = point.convert(geometryFactory, axisCount);
    final int count = Math.min(axisCount, point.getAxisCount());
    for (int axisIndex = 0; axisIndex < count; axisIndex++) {
      final double coordinate = point.getCoordinate(axisIndex);
      if (!MathUtil.isNanOrInfinite(coordinate)) {
        expand(geometryFactory, bounds, axisCount, axisIndex, coordinate);
      }
    }
  }

  public static void expandX(final double[] bounds, final int axisCount, final double value) {
    expand(bounds, axisCount, 0, value);
  }

  public static void expandY(final double[] bounds, final int axisCount, final double value) {
    expand(bounds, axisCount, 1, value);
  }

  public static void expandZ(final double[] bounds, final int axisCount, final double value) {
    expand(bounds, axisCount, 2, value);
  }

  public static double getMax(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      final int axisCount = bounds.length / 2;
      if (axisIndex < 0 || axisIndex > axisCount) {
        return Double.NaN;
      } else {
        final double max = bounds[axisCount + axisIndex];
        return max;
      }
    }
  }

  public static double getMin(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      final int axisCount = bounds.length / 2;
      if (axisIndex < 0 || axisIndex >= axisCount) {
        return Double.NaN;
      } else {
        final double min = bounds[axisIndex];
        return min;
      }
    }
  }

  /**
   * Point intersects the bounding box of the line.
   *
   * @param lineStart
   * @param lineEnd
   * @param point
   * @return
   */
  public static boolean intersects(final double p1X, final double p1Y, final double p2X,
    final double p2Y, final double qX, final double qY) {
    if (qX >= (p1X < p2X ? p1X : p2X) && qX <= (p1X > p2X ? p1X : p2X)
      && qY >= (p1Y < p2Y ? p1Y : p2Y) && qY <= (p1Y > p2Y ? p1Y : p2Y)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean intersects(final double[] bounds1, final double[] bounds2) {
    final int axisCount1 = bounds1.length / 2;
    final double minX1 = bounds1[0];
    final double minY1 = bounds1[1];
    final double maxX1 = bounds1[axisCount1];
    final double maxY1 = bounds1[axisCount1 + 1];

    final int axisCount2 = bounds2.length / 2;
    final double minX2 = bounds2[0];
    final double minY2 = bounds2[1];
    final double maxX2 = bounds2[axisCount2];
    final double maxY2 = bounds2[axisCount2 + 1];

    return !(minX2 > maxX1 || maxX2 < minX1 || minY2 > maxY1 || maxY2 < minY1);
  }

  /**
   * Point intersects the bounding box of the line.
   *
   * @param lineStart
   * @param lineEnd
   * @param point
   * @return
   */
  public static boolean intersects(final Point lineStart, final Point lineEnd, final Point point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();
    return intersects(x1, y1, x2, y2, x, y);
  }

  /**
   * Tests whether the envelope defined by p1-p2
   * and the envelope defined by q1-q2
   * intersect.
   *
   * @param p1 one extremal point of the envelope P
   * @param p2 another extremal point of the envelope P
   * @param q1 one extremal point of the envelope Q
   * @param q2 another extremal point of the envelope Q
   * @return <code>true</code> if Q intersects P
   */
  public static boolean intersects(final Point line1Start, final Point line1End,
    final Point line2Start, final Point line2End) {
    final double line1x1 = line1Start.getX();
    final double line1y1 = line1Start.getY();
    final double line1x2 = line1End.getX();
    final double line1y2 = line1End.getY();

    final double line2x1 = line2Start.getX();
    final double line2y1 = line2Start.getY();
    final double line2x2 = line2End.getX();
    final double line2y2 = line2End.getY();
    return intersectsMinMax(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1, line2x2, line2y2);
  }

  public static boolean intersectsMinMax(final double p1X, final double p1Y, final double p2X,
    final double p2Y, final double q1X, final double q1Y, final double q2X, final double q2Y) {
    double minp = Math.min(p1X, p2X);
    double maxq = Math.max(q1X, q2X);
    if (minp > maxq) {
      return false;
    } else {
      double minq = Math.min(q1X, q2X);
      double maxp = Math.max(p1X, p2X);
      if (maxp < minq) {
        return false;
      } else {
        minp = Math.min(p1Y, p2Y);
        maxq = Math.max(q1Y, q2Y);
        if (minp > maxq) {
          return false;
        } else {
          minq = Math.min(q1Y, q2Y);
          maxp = Math.max(p1Y, p2Y);
          if (maxp < minq) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
  }

  public static boolean isEmpty(final BoundingBox boundingBox) {
    if (boundingBox == null) {
      return true;
    } else {
      return boundingBox.isEmpty();
    }
  }
}
