package com.revolsys.jts.util;

import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.MathUtil;

public class GeometryTestUtil {

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G geometry(final GeometryFactory geometryFactory,
    final DataType geometryType, final int axisCount, final int partCount, final int ringCount) {
    if (DataTypes.POINT.equals(geometryType)) {
      return (G)point(geometryFactory, axisCount);
    } else if (DataTypes.MULTI_POINT.equals(geometryType)) {
      return (G)multiPoint(geometryFactory, axisCount, partCount);
    } else if (DataTypes.LINE_STRING.equals(geometryType)) {
      return (G)lineString(geometryFactory, axisCount);
    } else if (DataTypes.MULTI_LINE_STRING.equals(geometryType)) {
      return (G)multiLineString(geometryFactory, axisCount, partCount);
    } else if (DataTypes.POLYGON.equals(geometryType)) {
      return (G)polygon(geometryFactory, axisCount, ringCount);
    } else if (DataTypes.MULTI_POLYGON.equals(geometryType)) {
      return (G)multiPolygon(geometryFactory, axisCount, partCount, ringCount);
    } else {
      return null;
    }
  }

  private static Point getCentre(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
    final Point centre = areaBoundingBox.getCentre();
    return centre;
  }

  public static LinearRing linearRing(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex) {
    final Point centre = getCentre(geometryFactory);
    final double[] coordinates = new double[axisCount * 5];
    int offset = 0;
    final int baseIndex = partIndex * 2;
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, baseIndex, baseIndex,
      centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, baseIndex + 1,
      baseIndex, centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, baseIndex + 1,
      baseIndex + 1, centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, baseIndex,
      baseIndex + 1, centre);
    offset = setRingPoint(coordinates, offset, geometryFactory, axisCount, baseIndex, baseIndex,
      centre);
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  public static LineString lineString(final GeometryFactory geometryFactory, final int axisCount) {
    return lineString(geometryFactory, axisCount, 0);
  }

  public static LineString lineString(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex) {
    final Point centre = getCentre(geometryFactory);
    final double[] coordinates = new double[axisCount * 2];
    int offset = 0;
    offset = setPoint(coordinates, offset, geometryFactory, axisCount, partIndex * 2, centre);
    offset = setPoint(coordinates, offset, geometryFactory, axisCount, partIndex * 2 + 1, centre);
    return geometryFactory.lineString(axisCount, coordinates);
  }

  public static MultiLineString multiLineString(final GeometryFactory geometryFactory,
    final int axisCount, final int partCount) {
    final LineString[] lines = new LineString[partCount];
    for (int partIndex = 0; partIndex < partCount; partIndex++) {
      lines[partIndex] = linearRing(geometryFactory, axisCount, partIndex);
    }
    return geometryFactory.multiLineString(lines);
  }

  public static MultiPoint multiPoint(final GeometryFactory geometryFactory, final int axisCount,
    final int partCount) {
    final Point[] points = new Point[partCount];
    for (int partIndex = 0; partIndex < partCount; partIndex++) {
      points[partIndex] = point(geometryFactory, axisCount, partIndex);
    }
    return geometryFactory.multiPoint(points);
  }

  public static MultiPolygon multiPolygon(final GeometryFactory geometryFactory,
    final int axisCount, final int partCount, final int ringCount) {
    final Polygon[] polygons = new Polygon[partCount];
    for (int partIndex = 0; partIndex < partCount; partIndex++) {
      polygons[partIndex] = polygon(geometryFactory, axisCount, partIndex, ringCount);
    }
    return geometryFactory.multiPolygon(polygons);
  }

  public static Point point(final GeometryFactory geometryFactory, final int axisCount) {
    return point(geometryFactory, axisCount, 0);
  }

  public static Point point(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex) {
    final Point centre = getCentre(geometryFactory);
    final double[] coordinates = new double[axisCount];
    setPoint(coordinates, 0, geometryFactory, axisCount, partIndex, centre);
    return geometryFactory.point(coordinates);
  }

  public static Polygon polygon(final GeometryFactory geometryFactory, final int axisCount,
    final int ringCount) {
    return polygon(geometryFactory, axisCount, 0, ringCount);
  }

  public static Polygon polygon(final GeometryFactory geometryFactory, final int axisCount,
    final int partIndex, final int ringCount) {
    final LinearRing[] rings = new LinearRing[ringCount];
    for (int ringIndex = 0; ringIndex < ringCount; ringIndex++) {
      rings[ringIndex] = linearRing(geometryFactory, axisCount, ringIndex);
    }
    return geometryFactory.polygon(rings);
  }

  private static int setPoint(final double[] coordinates, int offset,
    final GeometryFactory geometryFactory, final int axisCount, final int partIndex,
    final Point centre) {
    coordinates[offset++] = centre.getX() + partIndex * 1;
    coordinates[offset++] = MathUtil.makePrecise(1000000, centre.getY());
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double resolution = geometryFactory.getResolution(axisIndex);
      coordinates[offset++] = axisIndex * 10 + resolution;
    }
    return offset;
  }

  private static int setRingPoint(final double[] coordinates, int offset,
    final GeometryFactory geometryFactory, final int axisCount, final int xIndex, final int yIndex,
    final Point centre) {
    coordinates[offset++] = centre.getX() + xIndex * 1;
    coordinates[offset++] = MathUtil.makePrecise(1000000, centre.getY()) + yIndex * 1;
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double resolution = geometryFactory.getResolution(axisIndex);
      coordinates[offset++] = axisIndex * 10 + resolution;
    }
    return offset;
  }

}