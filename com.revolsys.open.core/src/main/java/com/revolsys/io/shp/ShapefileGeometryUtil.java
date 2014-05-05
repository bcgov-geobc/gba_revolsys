package com.revolsys.io.shp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.MathUtil;

public final class ShapefileGeometryUtil {
  public static final Map<String, Method> GEOMETRY_TYPE_READ_METHOD_MAP = new LinkedHashMap<String, Method>();

  public static final Map<String, Method> GEOMETRY_TYPE_WRITE_METHOD_MAP = new LinkedHashMap<String, Method>();
  static {
    addReadWriteMethods("Point");
    addReadWriteMethods("Polygon");
    addReadWriteMethods("Polyline");
    addReadWriteMethods("Multipoint");

    for (final boolean z : Arrays.asList(false, true)) {
      for (final boolean m : Arrays.asList(false, true)) {
        final String hasZ = String.valueOf(z).toUpperCase();
        final String hasM = String.valueOf(m).toUpperCase();
        GEOMETRY_TYPE_READ_METHOD_MAP.put("LINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("LINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_READ_METHOD_MAP.put("MULTILINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("MULTILINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_READ_METHOD_MAP.put("MULTIPOLYGON" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYGON" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("MULTIPOLYGON" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYGON" + hasZ + hasM));
      }
    }

  }

  public static final ShapefileGeometryUtil INSTANCE = new ShapefileGeometryUtil();

  public static final ShapefileGeometryUtil SHP_INSTANCE = new ShapefileGeometryUtil(
    true, true);

  private static void addMethod(final String action,
    final Map<String, Method> methodMap, final String geometryType,
    final boolean hasZ, final boolean hasM, final Class<?>... parameterTypes) {
    final String geometryTypeKey = (geometryType + hasZ + hasM).toUpperCase();
    String methodName = action + geometryType;
    if (hasZ) {
      methodName += "Z";
    }
    if (hasM) {
      methodName += "M";
    }
    final Method method = JavaBeanUtil.getMethod(ShapefileGeometryUtil.class,
      methodName, parameterTypes);
    methodMap.put(geometryTypeKey, method);
  }

  private static void addReadWriteMethods(final String geometryType) {
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false,
      false, GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, false,
      GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false, true,
      GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, true,
      GeometryFactory.class, EndianInput.class);

    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false,
      false, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true,
      false, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false,
      true, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true,
      true, EndianOutput.class, Geometry.class);
  }

  public static Method getReadMethod(String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final Method method = GEOMETRY_TYPE_READ_METHOD_MAP.get(geometryTypeKey);
    if (method == null) {
      throw new IllegalArgumentException("Cannot get Shape Reader for: "
        + geometryTypeKey);
    }
    return method;
  }

  public static int getShapeType(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
      final int axisCount = geometryFactory.getAxisCount();
      final boolean hasZ = axisCount > 2;
      final boolean hasM = axisCount > 3;

      if (geometry instanceof Point) {
        if (hasM) {
          return ShapefileConstants.POINT_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.POINT_Z_SHAPE;
        } else {
          return ShapefileConstants.POINT_SHAPE;
        }
      } else if (geometry instanceof MultiPoint) {
        if (hasM) {
          return ShapefileConstants.MULTI_POINT_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.MULTI_POINT_Z_SHAPE;
        } else {
          return ShapefileConstants.MULTI_POINT_SHAPE;
        }
      } else if ((geometry instanceof LineString)
        || (geometry instanceof MultiLineString)) {
        if (hasM) {
          return ShapefileConstants.POLYLINE_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.POLYLINE_Z_SHAPE;
        } else {
          return ShapefileConstants.POLYLINE_SHAPE;
        }
      } else if ((geometry instanceof Polygon)
        || (geometry instanceof MultiPolygon)) {
        if (hasM) {
          return ShapefileConstants.POLYGON_ZM_SHAPE;
        } else if (hasZ) {
          return ShapefileConstants.POLYGON_Z_SHAPE;
        } else {
          return ShapefileConstants.POLYGON_SHAPE;
        }
      } else {
        throw new IllegalArgumentException("Unsupported geometry type: "
          + geometry.getGeometryType());
      }
    }
    return ShapefileConstants.NULL_SHAPE;
  }

  public static Method getWriteMethod(final Geometry geometry) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
    final int axisCount = geometryFactory.getAxisCount();
    final boolean hasZ = axisCount > 2;
    final boolean hasM = axisCount > 3;
    final String geometryType = geometry.getGeometryType();
    final String geometryTypeKey = geometryType.toUpperCase() + hasZ + hasM;
    return getWriteMethod(geometryTypeKey);
  }

  public static Method getWriteMethod(String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final Method method = GEOMETRY_TYPE_WRITE_METHOD_MAP.get(geometryTypeKey);
    if (method == null) {
      throw new IllegalArgumentException("Cannot get Shape Writer for: "
        + geometryTypeKey);
    }
    return method;
  }

  private boolean clockwise = true;

  private boolean writeLength = false;

  public ShapefileGeometryUtil() {

  }

  public ShapefileGeometryUtil(final boolean clockwise) {
    this.clockwise = clockwise;
  }

  public ShapefileGeometryUtil(final boolean clockwise,
    final boolean writeLength) {
    this.clockwise = clockwise;
    this.writeLength = writeLength;
  }

  public List<double[]> createCoordinatesLists(final int[] partIndex,
    final int axisCount) {
    final List<double[]> parts = new ArrayList<>(partIndex.length);
    for (int i = 0; i < partIndex.length; i++) {
      final int partNumPoints = partIndex[i];
      final double[] points = new double[partNumPoints * axisCount];
      parts.add(points);
    }
    return parts;
  }

  public Geometry createPolygonGeometryFromParts(
    final GeometryFactory geometryFactory, final List<double[]> parts,
    final int axisCount) {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    final List<LinearRing> currentParts = new ArrayList<>();
    for (final double[] coordinates : parts) {
      final LinearRing ring = geometryFactory.linearRing(axisCount, coordinates);
      final boolean ringClockwise = !ring.isCounterClockwise();
      if (ringClockwise == clockwise) {
        if (!currentParts.isEmpty()) {
          final Polygon polygon = geometryFactory.polygon(currentParts);
          polygons.add(polygon);
          currentParts.clear();
        }
      }
      currentParts.add(ring);
    }
    if (!currentParts.isEmpty()) {
      final Polygon polygon = geometryFactory.polygon(currentParts);
      polygons.add(polygon);
    }
    if (polygons.size() == 1) {
      return polygons.get(0);
    } else {
      return geometryFactory.multiPolygon(polygons);
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V read(final GeometryFactory geometryFactory,
    final EndianInput in, final int shapeType) throws IOException {
    switch (shapeType) {
      case ShapefileConstants.NULL_SHAPE:
        return null;
      case ShapefileConstants.POINT_SHAPE:
        return (V)readPoint(geometryFactory, in);
      case ShapefileConstants.POINT_M_SHAPE:
        return (V)readPointM(geometryFactory, in);
      case ShapefileConstants.POINT_Z_SHAPE:
        return (V)readPointZ(geometryFactory, in);
      case ShapefileConstants.POINT_ZM_SHAPE:
        return (V)readPointZM(geometryFactory, in);

      case ShapefileConstants.MULTI_POINT_SHAPE:
        return (V)readMultipoint(geometryFactory, in);
      case ShapefileConstants.MULTI_POINT_M_SHAPE:
        return (V)readMultipointM(geometryFactory, in);
      case ShapefileConstants.MULTI_POINT_Z_SHAPE:
        return (V)readMultipointZ(geometryFactory, in);
      case ShapefileConstants.MULTI_POINT_ZM_SHAPE:
        return (V)readMultipointZM(geometryFactory, in);

      case ShapefileConstants.POLYLINE_SHAPE:
        return (V)readPolyline(geometryFactory, in);
      case ShapefileConstants.POLYLINE_M_SHAPE:
        return (V)readPolylineM(geometryFactory, in);
      case ShapefileConstants.POLYLINE_Z_SHAPE:
        return (V)readPolylineZ(geometryFactory, in);
      case ShapefileConstants.POLYLINE_ZM_SHAPE:
        return (V)readPolylineZM(geometryFactory, in);

      case ShapefileConstants.POLYGON_SHAPE:
        return (V)readPolygon(geometryFactory, in);
      case ShapefileConstants.POLYGON_M_SHAPE:
        return (V)readPolygonM(geometryFactory, in);
      case ShapefileConstants.POLYGON_Z_SHAPE:
        return (V)readPolygonZ(geometryFactory, in);
      case ShapefileConstants.POLYGON_ZM_SHAPE:
        return (V)readPolygonZM(geometryFactory, in);
      default:
        throw new IllegalArgumentException(
          "Shapefile shape type not supported: " + shapeType);
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V read(final Method method,
    final GeometryFactory geometryFactory, final EndianInput in) {
    return (V)JavaBeanUtil.method(method, this, geometryFactory, in);
  }

  public void readCoordinates(final EndianInput in, final int vertexCount,
    final int axisCount, final double[] points, final int axisIndex)
    throws IOException {
    for (int j = 0; j < vertexCount; j++) {
      final double d = in.readLEDouble();
      points[j * axisCount + axisIndex] = d;
    }
  }

  public void readCoordinates(final EndianInput in, final int[] partIndex,
    final List<double[]> coordinateLists, final int ordinate,
    final int vertexCount, final int axisCount) throws IOException {
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final double[] coordinates = coordinateLists.get(i);
      readCoordinates(in, vertexCount, axisCount, coordinates, ordinate);
    }
  }

  public int[] readIntArray(final EndianInput in, final int count)
    throws IOException {
    final int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      final int value = in.readLEInt();
      values[i] = value;
    }
    return values;
  }

  public MultiPoint readMultipoint(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    final double[] points = readXYCoordinates(in, vertexCount, 2);
    return geometryFactory.multiPoint(points);
  }

  public MultiPoint readMultipointM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    final double[] points = readXYCoordinates(in, vertexCount, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, vertexCount, 4, points, 3);
    return geometryFactory.multiPoint(new DoubleCoordinatesList(4, points));
  }

  public MultiPoint readMultipointZ(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    final double[] points = readXYCoordinates(in, vertexCount, 3);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, vertexCount, 3, points, 2);
    return geometryFactory.multiPoint(new DoubleCoordinatesList(3, points));
  }

  public MultiPoint readMultipointZM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    final double[] points = readXYCoordinates(in, vertexCount, 4);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, vertexCount, 4, points, 2);
    in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
    readCoordinates(in, vertexCount, 4, points, 3);
    return geometryFactory.multiPoint(new DoubleCoordinatesList(4, points));
  }

  public int[] readPartIndex(final EndianInput in, final int numParts,
    final int vertexCount) throws IOException {
    final int[] partIndex = new int[numParts];
    if (numParts > 0) {
      int startIndex = in.readLEInt();
      for (int i = 1; i < partIndex.length; i++) {
        final int index = in.readLEInt();
        partIndex[i - 1] = index - startIndex;
        startIndex = index;
      }
      partIndex[partIndex.length - 1] = vertexCount - startIndex;
    }
    return partIndex;
  }

  public Point readPoint(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double[] points = readXYCoordinates(in, 1, 2);
    return geometryFactory.point(points);
  }

  public Point readPointM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = 0;
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(4, x, y, z,
      m);
    return geometryFactory.point(points);
  }

  public void readPoints(final EndianInput in, final int[] partIndex,
    final List<double[]> coordinateLists, final int vertexCount,
    final int axisCount) throws IOException {
    for (int i = 0; i < partIndex.length; i++) {
      final double[] coordinates = coordinateLists.get(i);
      readXYCoordinates(in, vertexCount, axisCount, coordinates);
    }
  }

  public Point readPointZ(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(3, x, y, z);
    return geometryFactory.point(points);
  }

  public Point readPointZM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    final double m = in.readLEDouble();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(4, x, y, z,
      m);
    return geometryFactory.point(points);
  }

  public Geometry readPolygon(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, vertexCount);

    final List<double[]> parts = createCoordinatesLists(partIndex, 2);

    readPoints(in, partIndex, parts, vertexCount, 2);

    return createPolygonGeometryFromParts(geometryFactory, parts, 2);

  }

  public Geometry readPolygonM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, vertexCount);

    final List<double[]> parts = createCoordinatesLists(partIndex, 4);
    readPoints(in, partIndex, parts, vertexCount, 4);
    readCoordinates(in, partIndex, parts, 3, vertexCount, 4);
    return createPolygonGeometryFromParts(geometryFactory, parts, 4);

  }

  public Geometry readPolygonZ(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, vertexCount);

    final List<double[]> parts = createCoordinatesLists(partIndex, 3);
    readPoints(in, partIndex, parts, vertexCount, 3);
    readCoordinates(in, partIndex, parts, 2, vertexCount, 3);
    return createPolygonGeometryFromParts(geometryFactory, parts, 3);
  }

  public Geometry readPolygonZM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, vertexCount);

    final List<double[]> parts = createCoordinatesLists(partIndex, 4);
    readPoints(in, partIndex, parts, vertexCount, 4);
    readCoordinates(in, partIndex, parts, 2, vertexCount, 4);
    readCoordinates(in, partIndex, parts, 3, vertexCount, 4);
    return createPolygonGeometryFromParts(geometryFactory, parts, 4);
  }

  public Geometry readPolyline(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 2;
    if (numParts == 1) {
      in.readLEInt();
      final double[] points = readXYCoordinates(in, vertexCount, axisCount);

      return geometryFactory.lineString(2, points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = vertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();

      }
      final List<LineString> lines = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final double[] coordinates = readXYCoordinates(in, numCoords, axisCount);
        lines.add(geometryFactory.lineString(2, coordinates));
      }
      return geometryFactory.multiLineString(lines);
    }
  }

  public Geometry readPolylineM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 4;
    if (numParts == 1) {
      in.readLEInt();
      final double[] points = readXYCoordinates(in, vertexCount, axisCount);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, vertexCount, 3, points, 3);
      return geometryFactory.lineString(3, points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = vertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      final List<double[]> pointsList = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final double[] coordinates = readXYCoordinates(in, numCoords, axisCount);
        pointsList.add(coordinates);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final double[] points = pointsList.get(i);
        readCoordinates(in, vertexCount, 4, points, 3);
      }
      final List<LineString> lines = new ArrayList<>();
      for (final double[] coordinates : pointsList) {
        lines.add(geometryFactory.lineString(4, coordinates));
      }
      return geometryFactory.multiLineString(lines);
    }
  }

  public Geometry readPolylineZ(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 3;
    if (numParts == 1) {
      in.readLEInt();
      final double[] points = readXYCoordinates(in, vertexCount, axisCount);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, vertexCount, 3, points, 2);
      return geometryFactory.lineString(3, points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = vertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      final List<double[]> pointsList = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final double[] points = readXYCoordinates(in, numCoords, axisCount);
        pointsList.add(points);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final double[] points = pointsList.get(i);
        readCoordinates(in, vertexCount, 3, points, 2);
      }
      final List<LineString> lines = new ArrayList<>();
      for (final double[] coordinates : pointsList) {
        lines.add(geometryFactory.lineString(3, coordinates));
      }
      return geometryFactory.multiLineString(lines);
    }
  }

  public Geometry readPolylineZM(final GeometryFactory geometryFactory,
    final EndianInput in) throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 4;
    if (numParts == 1) {
      in.readLEInt();
      final double[] points = readXYCoordinates(in, vertexCount, axisCount);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, vertexCount, 4, points, 2);
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      readCoordinates(in, vertexCount, 4, points, 3);
      return geometryFactory.lineString(4, points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = vertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      final List<double[]> pointsList = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final double[] points = readXYCoordinates(in, numCoords, axisCount);
        pointsList.add(points);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final double[] points = pointsList.get(i);
        readCoordinates(in, vertexCount, 4, points, 2);
      }
      in.skipBytes(2 * MathUtil.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final double[] points = pointsList.get(i);
        readCoordinates(in, vertexCount, 4, points, 3);
      }
      final List<LineString> lines = new ArrayList<>();
      for (final double[] coordinates : pointsList) {
        lines.add(geometryFactory.lineString(4, coordinates));
      }
      return geometryFactory.multiLineString(lines);
    }
  }

  public double[] readXYCoordinates(final EndianInput in,
    final int vertexCount, final int axisCount) throws IOException {
    final double[] coordinates = new double[vertexCount * axisCount];
    readXYCoordinates(in, axisCount, vertexCount, coordinates);
    return coordinates;
  }

  public void readXYCoordinates(final EndianInput in, final int axisCount,
    final int vertexCount, final double[] points) throws IOException {
    for (int j = 0; j < vertexCount; j++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      points[j * axisCount] = x;
      points[j * axisCount + 1] = y;
    }
  }

  public void write(final Method method, final EndianOutput out,
    final Geometry geometry) {
    JavaBeanUtil.method(method, this, out, geometry);
  }

  public void writeEnvelope(final EndianOutput out, final BoundingBox envelope)
    throws IOException {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public void writeMCoordinates(final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    if (coordinates.getAxisCount() >= 4) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double m = coordinates.getM(i);
        if (!Double.isNaN(m)) {
          out.writeLEDouble(m);
        } else {
          out.writeLEDouble(0);
        }
      }
    } else {
      for (int i = 0; i < coordinates.size(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeMCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMCoordinatesRange(out, geometry);
    for (int n = 0; n < geometry.getGeometryCount(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      writeMCoordinates(out, coordinates);
    }
  }

  public void writeMCoordinates(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    writeMCoordinatesRange(out, pointsList);
    for (final CoordinatesList points : pointsList) {
      writeMCoordinates(out, points);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out,
    final Geometry geometry) throws IOException {
    double minM = Double.MAX_VALUE;
    double maxM = -Double.MAX_VALUE;
    for (int n = 0; n < geometry.getGeometryCount(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      if (coordinates.getAxisCount() >= 4) {
        for (int i = 0; i < coordinates.size(); i++) {
          final double m = coordinates.getM(i);
          if (!Double.isNaN(m)) {
            minM = Math.min(minM, m);
            maxM = Math.max(maxM, m);
          }
        }
      }
    }
    if (minM == Double.MAX_VALUE && maxM == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    double minM = Double.MAX_VALUE;
    double maxM = -Double.MAX_VALUE;
    for (final CoordinatesList ring : pointsList) {
      for (int i = 0; i < ring.size(); i++) {
        double m = ring.getValue(i, 2);
        if (Double.isNaN(m)) {
          m = 0;
        }
        minM = Math.min(m, minM);
        maxM = Math.max(m, maxM);
      }
    }
    if (minM == Double.MAX_VALUE && maxM == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public void writeMultipoint(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.POLYLINE_SHAPE, 8);
  }

  private void writeMultipoint(final EndianOutput out, final Geometry geometry,
    final int shapeType, final int wordsPerPoint) throws IOException {
    if (geometry instanceof MultiPoint || geometry instanceof Point) {
      final int vertexCount = geometry.getVertexCount();
      if (writeLength) {
        final int recordLength = 20 + wordsPerPoint * vertexCount;
        // (BYTES_IN_INT + 4 * BYTES_IN_DOUBLE + BYTES_IN_INT +
        // (vertexCount * 2 * BYTES_IN_DOUBLE)) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      final BoundingBox envelope = geometry.getBoundingBox();
      out.writeLEInt(shapeType);
      writeEnvelope(out, envelope);
      out.writeLEInt(vertexCount);
      writeXYCoordinates(out, geometry);
    } else {
      throw new IllegalArgumentException("Expecting " + MultiPoint.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writeMultipointM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writeMultipointZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_Z_SHAPE, 12);
    writeZCoordinates(out, geometry);
  }

  public void writeMultipointZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writePoint(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (writeLength) {
        final int recordLength = 10;
        // (BYTES_IN_INT + 2 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_SHAPE);
      out.writeLEDouble(point.getX());
      out.writeLEDouble(point.getY());
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writePointM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (writeLength) {
        final int recordLength = 14;
        // (BYTES_IN_INT + 3 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_M_SHAPE);
      out.writeLEDouble(point.getX());
      out.writeLEDouble(point.getY());
      out.writeLEDouble(point.getM());
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writePointZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (writeLength) {
        final int recordLength = 14;
        // (BYTES_IN_INT + 3 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_Z_SHAPE);
      out.writeLEDouble(point.getX());
      out.writeLEDouble(point.getY());
      out.writeLEDouble(point.getZ());
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writePointZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (writeLength) {
        final int recordLength = 18;
        // (BYTES_IN_INT + 4 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_ZM_SHAPE);
      out.writeLEDouble(point.getX());
      out.writeLEDouble(point.getY());
      out.writeLEDouble(point.getZ());
      out.writeLEDouble(point.getM());
    } else {
      throw new IllegalArgumentException("Expecting " + Point.class
        + " geometry got " + geometry.getClass());
    }
  }

  public void writePolygon(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolygon(out, geometry, ShapefileConstants.POLYGON_SHAPE, 0, 8);
  }

  private List<CoordinatesList> writePolygon(final EndianOutput out,
    final Geometry geometry, final int shapeType, final int headerOverhead,
    final int wordsPerPoint) throws IOException {

    int vertexCount = 0;

    final List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry part = geometry.getGeometry(i);
      if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        final LineString exterior = polygon.getExteriorRing();
        CoordinatesList exteroirPoints = CoordinatesListUtil.get(exterior);
        final boolean exteriorClockwise = !exterior.isCounterClockwise();
        if (exteriorClockwise != clockwise) {
          exteroirPoints = exteroirPoints.reverse();
        }
        rings.add(exteroirPoints);
        vertexCount += exteroirPoints.size();
        final int numHoles = polygon.getNumInteriorRing();
        for (int j = 0; j < numHoles; j++) {
          final LineString interior = polygon.getInteriorRing(j);
          CoordinatesList interiorCoords = CoordinatesListUtil.get(interior);
          final boolean interiorClockwise = !interior.isCounterClockwise();
          if (interiorClockwise == clockwise) {
            interiorCoords = interiorCoords.reverse();
          }
          rings.add(interiorCoords);
          vertexCount += interiorCoords.size();
        }
      } else {
        throw new IllegalArgumentException("Expecting " + Polygon.class
          + " geometry got " + part.getClass());
      }
    }
    final int numParts = rings.size();

    if (writeLength) {
      final int recordLength = 22 + headerOverhead + 2 * numParts
        + wordsPerPoint * vertexCount;

      out.writeInt(recordLength);
    }
    out.writeLEInt(shapeType);
    final BoundingBox envelope = geometry.getBoundingBox();
    writeEnvelope(out, envelope);
    out.writeLEInt(numParts);
    out.writeLEInt(vertexCount);

    int partIndex = 0;
    for (final CoordinatesList ring : rings) {
      out.writeLEInt(partIndex);
      partIndex += ring.size();
    }

    for (final CoordinatesList ring : rings) {
      writeXYCoordinates(out, ring);
    }
    return rings;
  }

  public void writePolygonM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_M_SHAPE, 8, 12);
    writeMCoordinates(out, rings);
  }

  public void writePolygonZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_Z_SHAPE, 8, 12);
    writeZCoordinates(out, rings);
  }

  public void writePolygonZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final List<CoordinatesList> rings = writePolygon(out, geometry,
      ShapefileConstants.POLYGON_ZM_SHAPE, 16, 16);
    writeZCoordinates(out, rings);
    writeMCoordinates(out, rings);
  }

  public void writePolyline(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_SHAPE, 8);
  }

  private void writePolyline(final EndianOutput out, final Geometry geometry,
    final int shapeType, final int wordsPerPoint) throws IOException {
    if (geometry instanceof LineString || geometry instanceof MultiLineString) {
      final int numCoordinates = geometry.getVertexCount();
      final int numGeometries = geometry.getGeometryCount();
      final BoundingBox envelope = geometry.getBoundingBox();

      if (writeLength) {
        // final int recordLength = ((3 + numGeometries) * BYTES_IN_INT + (4 + 2
        // * numCoordinates)
        // * BYTES_IN_DOUBLE) / 2;
        final int recordLength = 22 + numGeometries * 2 + numCoordinates
          * wordsPerPoint;
        out.writeInt(recordLength);
      }
      out.writeLEInt(shapeType);
      writeEnvelope(out, envelope);
      out.writeLEInt(numGeometries);
      out.writeLEInt(numCoordinates);
      writePolylinePartIndexes(out, geometry);
      writeXYCoordinates(out, geometry);
    } else {
      throw new IllegalArgumentException("Expecting " + LineString.class
        + " or " + MultiLineString.class + " geometry got "
        + geometry.getClass());
    }
  }

  public void writePolylineM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writePolylinePartIndexes(final EndianOutput out,
    final Geometry geometry) throws IOException {
    int partIndex = 0;
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final LineString line = (LineString)geometry.getGeometry(i);
      out.writeLEInt(partIndex);
      partIndex += line.getVertexCount();
    }
  }

  public void writePolylineZ(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_Z_SHAPE, 12);
    writeZCoordinates(out, geometry);
  }

  public void writePolylineZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writeXYCoordinates(final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    for (int i = 0; i < coordinates.size(); i++) {
      out.writeLEDouble(coordinates.getX(i));
      out.writeLEDouble(coordinates.getY(i));
    }
  }

  public void writeXYCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry subGeometry = geometry.getGeometry(i);
      final CoordinatesList points = CoordinatesListUtil.get(subGeometry);
      writeXYCoordinates(out, points);
    }
  }

  public void writeZCoordinates(final EndianOutput out,
    final CoordinatesList coordinates) throws IOException {
    if (coordinates.getAxisCount() >= 3) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double z = coordinates.getZ(i);
        if (Double.isNaN(z)) {
          out.writeLEDouble(0);
        } else {
          out.writeLEDouble(z);
        }
      }
    } else {
      for (int i = 0; i < coordinates.size(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeZCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeZCoordinatesRange(out, geometry);
    for (int n = 0; n < geometry.getGeometryCount(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      writeZCoordinates(out, coordinates);
    }
  }

  public void writeZCoordinates(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    writeZCoordinatesRange(out, pointsList);
    for (final CoordinatesList points : pointsList) {
      writeZCoordinates(out, points);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out,
    final Geometry geometry) throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = -Double.MAX_VALUE;
    for (int n = 0; n < geometry.getGeometryCount(); n++) {
      final Geometry subGeometry = geometry.getGeometry(n);
      final CoordinatesList coordinates = CoordinatesListUtil.get(subGeometry);
      if (coordinates.getAxisCount() >= 3) {
        for (int i = 0; i < coordinates.size(); i++) {
          final double z = coordinates.getZ(i);
          if (!Double.isNaN(z)) {
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
          }
        }
      }
    }
    if (minZ == Double.MAX_VALUE && maxZ == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out,
    final List<CoordinatesList> pointsList) throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = -Double.MAX_VALUE;
    for (final CoordinatesList ring : pointsList) {
      for (int i = 0; i < ring.size(); i++) {
        double z = ring.getValue(i, 2);
        if (Double.isNaN(z)) {
          z = 0;
        }
        minZ = Math.min(z, minZ);
        maxZ = Math.max(z, maxZ);
      }
    }
    if (minZ == Double.MAX_VALUE || maxZ == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    }
  }

}
