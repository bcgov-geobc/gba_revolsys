package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OraclePreparedStatement;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class OracleSdoGeometryJdbcAttribute extends JdbcFieldDefinition {

  private final int numAxis;

  private final GeometryFactory geometryFactory;

  private final PrecisionModel[] precisionModels;

  public OracleSdoGeometryJdbcAttribute(final String name, final DataType type, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties,
    final GeometryFactory geometryFactory, final int numAxis) {
    super(name, type, sqlType, 0, 0, required, description, properties);
    this.geometryFactory = geometryFactory;
    this.numAxis = numAxis;
    this.precisionModels = new PrecisionModel[numAxis];
    final PrecisionModel precisionModel = geometryFactory.getPrecisionModel();
    if (precisionModel != null) {
      this.precisionModels[0] = precisionModel;
      this.precisionModels[1] = precisionModel;
    }
    for (int i = 0; i < this.precisionModels.length; i++) {
      if (this.precisionModels[i] == null) {
        this.precisionModels[i] = new PrecisionModel();
      }
    }
    setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
  }

  @Override
  public void addColumnName(final StringBuffer sql, final String tablePrefix) {
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_GTYPE, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_POINT.X, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_POINT.Y, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_POINT.Z, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_ELEM_INFO, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.SDO_ORDINATES");
  }

  @Override
  public OracleSdoGeometryJdbcAttribute clone() {
    return new OracleSdoGeometryJdbcAttribute(getName(), getType(), getSqlType(), isRequired(),
      getDescription(), getProperties(), this.geometryFactory, this.numAxis);
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record object) throws SQLException {
    Geometry value;
    final int geometryType = resultSet.getInt(columnIndex);
    int numAxis = geometryType / 1000;
    if (numAxis < 2) {
      numAxis = 2;
    }
    switch (geometryType % 1000) {
      case 1:
        value = toPoint(resultSet, columnIndex, numAxis);
      break;
      case 2:
        value = toLineString(resultSet, columnIndex, numAxis);
      break;
      case 3:
        value = toPolygon(resultSet, columnIndex, numAxis);
      break;
      case 5:
        value = toMultiPoint(resultSet, columnIndex, numAxis);
      break;
      case 6:
        value = toMultiLineString(resultSet, columnIndex, numAxis);
      break;
      case 7:
        value = toMultiPolygon(resultSet, columnIndex, numAxis);
      break;
      default:
        throw new IllegalArgumentException("Unsupported geometry type " + geometryType);
    }
    object.setValue(getIndex(), value);
    return columnIndex + 6;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record object) throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      synchronized (STRUCT.class) {
        final Connection connection = statement.getConnection();
        final STRUCT oracleValue = toJdbc(connection, value, this.numAxis);
        ((OraclePreparedStatement)statement).setSTRUCT(parameterIndex, oracleValue);
      }
    }
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      synchronized (STRUCT.class) {
        final Connection connection = statement.getConnection();
        final STRUCT oracleValue = toJdbc(connection, value, 2);
        ((OraclePreparedStatement)statement).setSTRUCT(parameterIndex, oracleValue);
      }
    }
    return parameterIndex + 1;
  }

  // public synchronized Object toJava(
  // final Object object)
  // throws SQLException {
  // if (object instanceof STRUCT) {
  // final STRUCT struct = (STRUCT)object;
  // final Datum[] attributes = struct.getOracleAttributes();
  // final int geometryType = attributes[0].intValue();
  // final int numAxis = geometryType / 1000;
  // switch (geometryType % 1000) {
  // case 1:
  // return toPoint(attributes, numAxis);
  // case 2:
  // return toLineString(attributes, numAxis);
  // case 3:
  // return toPolygon(attributes, numAxis);
  // default:
  // throw new IllegalArgumentException("Unsupported geometry type "
  // + geometryType);
  // }
  // }
  // return object;
  //
  // }

  public double[] toClockwiseCoordinatesArray(final LineString ring, final int dimension) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(ring);
    if (!CoordinatesListUtil.isCCW(coordinates)) {
      return toCoordinateArray(coordinates, dimension);
    } else {
      return toCoordinateArray(coordinates.reverse(), dimension);
    }
  }

  private double[] toCoordinateArray(final CoordinateSequence sequence, final int dimension) {
    int geometryDimension = sequence.getDimension();
    if (Double.isNaN(sequence.getOrdinate(0, 2))) {
      geometryDimension = 2;
    }

    final double[] ordinates = new double[dimension * sequence.size()];
    int ordinateIndex = 0;

    for (int i = 0; i < sequence.size(); i++) {
      for (int j = 0; j < dimension; j++) {
        if (j >= geometryDimension) {
          ordinates[ordinateIndex++] = 0;
        } else {
          final double ordinate = sequence.getOrdinate(i, j);
          if (Double.isNaN(ordinate)) {
            ordinates[ordinateIndex++] = 0;
          } else {
            final PrecisionModel precisionModel = this.precisionModels[j];
            ordinates[ordinateIndex++] = precisionModel.makePrecise(ordinate);
          }
        }
      }
    }
    return ordinates;
  }

  private double[] toCoordinateArray(final LineString line, final int dimension) {
    final CoordinateSequence sequence = line.getCoordinateSequence();
    final double[] coordinates = toCoordinateArray(sequence, dimension);
    return coordinates;
  }

  private double[][] toCoordinateArrays(final Polygon polygon, final int dimension) {
    final int numInteriorRing = polygon.getNumInteriorRing();
    final double[][] ordinateArrays = new double[1 + numInteriorRing][];

    final LineString exteriorRing = polygon.getExteriorRing();
    ordinateArrays[0] = toCounterClockwiseCoordinatesArray(exteriorRing, dimension);

    for (int i = 0; i < numInteriorRing; i++) {
      final LineString ring = polygon.getInteriorRingN(i);
      ordinateArrays[i + 1] = toClockwiseCoordinatesArray(ring, dimension);
    }
    return ordinateArrays;
  }

  public double[] toCounterClockwiseCoordinatesArray(final LineString ring, final int dimension) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(ring);
    if (CoordinatesListUtil.isCCW(coordinates)) {
      return toCoordinateArray(coordinates, dimension);
    } else {
      return toCoordinateArray(coordinates.reverse(), dimension);
    }
  }

  private STRUCT toJdbc(final Connection connection, final Object object, final int dimension)
    throws SQLException {
    if (object instanceof Geometry) {
      Geometry geometry = (Geometry)object;
      geometry = GeometryProjectionUtil.performCopy(geometry, this.geometryFactory);
      // TODO direct convert to SDO Geometry from JTS Geometry
      JGeometry jGeometry = null;
      if (object instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        jGeometry = toJGeometry(polygon, dimension);
      } else if (object instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        jGeometry = toJGeometry(lineString, dimension);
      } else if (object instanceof Point) {
        final Point point = (Point)geometry;
        jGeometry = toJGeometry(point, dimension);
      } else if (object instanceof Coordinates) {
        final Coordinates coordinates = (Coordinates)geometry;
        final Point point = this.geometryFactory.createPoint(coordinates);
        jGeometry = toJGeometry(point, dimension);
      } else if (object instanceof MultiPoint) {
        final MultiPoint multiPoint = (MultiPoint)geometry;
        jGeometry = toJGeometry(multiPoint, dimension);
      } else if (object instanceof MultiLineString) {
        final MultiLineString multiLineString = (MultiLineString)geometry;
        jGeometry = toJGeometry(multiLineString, dimension);
      } else if (object instanceof MultiPolygon) {
        final MultiPolygon multiPolygon = (MultiPolygon)geometry;
        jGeometry = toJGeometry(multiPolygon, dimension);
      } else {
        throw new IllegalArgumentException("Unable to convert to SDO_GEOMETRY " + object.getClass());
      }
      try {
        final STRUCT struct = JGeometry.store(jGeometry, connection);
        return struct;
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to convert Oracle JGeometry to STRUCT: "
          + e.getMessage(), e);
      }
    } else {
      throw new IllegalArgumentException("Unable to convert to SDO_GEOMETRY " + object.getClass());
    }
  }

  private JGeometry toJGeometry(final LineString lineString, final int dimension) {
    final double[] ordinates = toCoordinateArray(lineString, dimension);
    final int srid = this.geometryFactory.getSRID();
    return JGeometry.createLinearLineString(ordinates, dimension, srid);
  }

  private JGeometry toJGeometry(final MultiLineString multiLineString, final int dimension) {
    final int srid = this.geometryFactory.getSRID();
    final int numGeometries = multiLineString.getNumGeometries();
    final Object[] parts = new Object[numGeometries];
    for (int i = 0; i < numGeometries; i++) {
      final LineString line = (LineString)multiLineString.getGeometryN(i);
      final double[] ordinates = toCoordinateArray(line, dimension);
      parts[i] = ordinates;
    }
    return JGeometry.createLinearMultiLineString(parts, dimension, srid);
  }

  private JGeometry toJGeometry(final MultiPoint multiPoint, final int numAxis) {
    final int numPoints = multiPoint.getNumGeometries();
    final double[] points = new double[numPoints * numAxis];
    int k = 0;
    for (int i = 0; i < numPoints; i++) {
      final Point point = (Point)multiPoint.getGeometryN(i);
      final Coordinates coordinates = CoordinatesUtil.get(point);
      for (int j = 0; j < numAxis; j++) {
        final double value = coordinates.getValue(j);
        if (Double.isNaN(value)) {
          points[k] = 0;
        } else {
          points[k] = value;
        }
        k++;
      }
    }
    final int srid = this.geometryFactory.getSRID();
    final int[] elemInfo = new int[] {
      1, 1, numPoints
    };
    return new JGeometry(numAxis * 1000 + JGeometry.GTYPE_MULTIPOINT, srid, elemInfo, points);
  }

  private JGeometry toJGeometry(final MultiPolygon multiPolygon, final int dimension) {
    if (multiPolygon.getNumGeometries() == 1) {
      return toJGeometry((Polygon)multiPolygon.getGeometryN(0), dimension);
    } else {
      final int srid = this.geometryFactory.getSRID();
      int numCoordinates = 0;
      int numParts = 0;
      final List<double[][]> coordinateArraysList = new ArrayList<double[][]>();
      for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);
        final double[][] coordinateArrays = toCoordinateArrays(polygon, dimension);
        coordinateArraysList.add(coordinateArrays);
        numParts += coordinateArrays.length;
        for (final double[] coordinates : coordinateArrays) {
          numCoordinates += coordinates.length;
        }
      }

      final int[] elemInfo = new int[numParts * 3];
      final double[] allCoordinates = new double[numCoordinates];
      int elementIndex = 0;
      int coordinateIndex = 0;
      for (final double[][] coordinateArrays : coordinateArraysList) {
        for (int i = 0; i < coordinateArrays.length; i++) {
          final double[] coordinates = coordinateArrays[i];
          System.arraycopy(coordinates, 0, allCoordinates, coordinateIndex, coordinates.length);
          elemInfo[elementIndex] = coordinateIndex + 1;
          if (i == 0) {
            elemInfo[elementIndex + 1] = 1003;
          } else {
            elemInfo[elementIndex + 1] = 2003;
          }
          elemInfo[elementIndex + 2] = 1;

          coordinateIndex += coordinates.length;
          elementIndex += 3;
        }
      }
      return new JGeometry(dimension * 1000 + 7, srid, elemInfo, allCoordinates);
    }
  }

  private JGeometry toJGeometry(final Point point, final int dimension) {
    final CoordinatesList coordinatesList = CoordinatesListUtil.get(point);
    final int srid = this.geometryFactory.getSRID();
    final double x = coordinatesList.getX(0);
    final double y = coordinatesList.getY(0);
    if (dimension == 3) {
      double z = coordinatesList.getZ(0);
      if (Double.isNaN(z)) {
        z = 0;
      }
      return new JGeometry(x, y, z, srid);
    } else {
      return new JGeometry(x, y, srid);
    }
  }

  private JGeometry toJGeometry(final Polygon polygon, final int dimension) {
    final Object[] oridinateArrays = toCoordinateArrays(polygon, dimension);
    final int srid = this.geometryFactory.getSRID();
    return JGeometry.createLinearPolygon(oridinateArrays, dimension, srid);
  }

  private LineString toLineString(final ResultSet resultSet, final int columnIndex,
    final int numAxis) throws SQLException {
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);
    final double[] coordinates = coordinatesArray.getDoubleArray();
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(numAxis, coordinates);
    return this.geometryFactory.createLineString(coordinatesList);
  }

  private MultiLineString toMultiLineString(final ResultSet resultSet, final int columnIndex,
    final int numAxis) throws SQLException {
    final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();

    final ARRAY elemInfoArray = (ARRAY)resultSet.getArray(columnIndex + 4);
    final long[] elemInfo = elemInfoArray.getLongArray();
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);

    for (int i = 0; i < elemInfo.length; i += 3) {
      final long offset = elemInfo[i];
      final long type = elemInfo[i + 1];
      final long interpretation = elemInfo[i + 2];
      int length;
      if (i + 3 < elemInfo.length) {
        final long nextOffset = elemInfo[i + 3];
        length = (int)(nextOffset - offset) + 1;
      } else {
        length = (int)(coordinatesArray.length() - offset) + 1;
      }
      if (interpretation == 1) {
        final double[] ordinates = coordinatesArray.getDoubleArray(offset, length);
        final CoordinatesList points = new DoubleCoordinatesList(numAxis, ordinates);
        pointsList.add(points);
      } else {
        throw new IllegalArgumentException("Unsupported geometry type " + type + " interpretation "
          + interpretation);
      }
    }

    return this.geometryFactory.createMultiLineString(pointsList);
  }

  private MultiPoint toMultiPoint(final ResultSet resultSet, final int columnIndex,
    final int numAxis) throws SQLException {
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);

    final double[] coordinates = coordinatesArray.getDoubleArray();
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(numAxis, coordinates);

    return this.geometryFactory.createMultiPoint(coordinatesList);
  }

  private MultiPolygon toMultiPolygon(final ResultSet resultSet, final int columnIndex,
    final int numAxis) throws SQLException {
    final List<Polygon> polygons = new ArrayList<Polygon>();

    final ARRAY elemInfoArray = (ARRAY)resultSet.getArray(columnIndex + 4);
    final long[] elemInfo = elemInfoArray.getLongArray();
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);

    LinearRing exteriorRing = null;
    List<LinearRing> interiorRings = null;

    for (int i = 0; i < elemInfo.length; i += 3) {
      final long offset = elemInfo[i];
      final long type = elemInfo[i + 1];
      final long interpretation = elemInfo[i + 2];
      int length;
      if (i + 3 < elemInfo.length) {
        final long nextOffset = elemInfo[i + 3];
        length = (int)(nextOffset - offset) + 1;
      } else {
        length = (int)(coordinatesArray.length() - offset) + 1;
      }
      if (interpretation == 1) {
        final double[] ordinates = coordinatesArray.getDoubleArray(offset, length);
        final CoordinatesList coordinatesList = new DoubleCoordinatesList(numAxis, ordinates);
        final LinearRing ring = this.geometryFactory.createLinearRing(coordinatesList);

        switch ((int)type) {
          case 1003:
            if (exteriorRing != null) {
              // TODO convert to array of rings
              final Polygon polygon = this.geometryFactory.createPolygon(exteriorRing,
                com.vividsolutions.jts.geom.GeometryFactory.toLinearRingArray(interiorRings));
              polygons.add(polygon);
            }
            exteriorRing = ring;
            interiorRings = new ArrayList<LinearRing>();

          break;
          case 2003:
            interiorRings.add(ring);
          break;

          default:
            throw new IllegalArgumentException("Unsupported geometry type " + type);
        }
      } else {
        throw new IllegalArgumentException("Unsupported geometry type " + type + " interpretation "
          + interpretation);
      }
    }
    if (exteriorRing != null) {
      final Polygon polygon = this.geometryFactory.createPolygon(exteriorRing,
        com.vividsolutions.jts.geom.GeometryFactory.toLinearRingArray(interiorRings));
      polygons.add(polygon);
    }

    return this.geometryFactory.createMultiPolygon(polygons);
  }

  private Point toPoint(final ResultSet resultSet, final int columnIndex, final int numAxis)
    throws SQLException {
    final CoordinatesList coordinatesList;
    final double x = resultSet.getDouble(columnIndex + 1);
    final double y = resultSet.getDouble(columnIndex + 2);
    if (numAxis == 2) {
      coordinatesList = new DoubleCoordinatesList(numAxis, x, y);
    } else {
      final double z = resultSet.getDouble(columnIndex + 3);
      coordinatesList = new DoubleCoordinatesList(numAxis, x, y, z);
    }
    return this.geometryFactory.createPoint(coordinatesList);
  }

  private Polygon toPolygon(final ResultSet resultSet, final int columnIndex, final int numAxis)
    throws SQLException {
    final ARRAY elemInfoArray = (ARRAY)resultSet.getArray(columnIndex + 4);
    final long[] elemInfo = elemInfoArray.getLongArray();
    final ARRAY coordinatesArray = (ARRAY)resultSet.getArray(columnIndex + 5);

    LinearRing exteriorRing = null;
    final LinearRing[] interiorRings = new LinearRing[elemInfo.length / 3 - 1];
    int numInteriorRings = 0;

    for (int i = 0; i < elemInfo.length; i += 3) {
      final long offset = elemInfo[i];
      final long type = elemInfo[i + 1];
      final long interpretation = elemInfo[i + 2];
      int length;
      if (i + 3 < elemInfo.length) {
        final long nextOffset = elemInfo[i + 3];
        length = (int)(nextOffset - offset) + 1;
      } else {
        length = (int)(coordinatesArray.length() - offset) + 1;
      }
      if (interpretation == 1) {
        final double[] ordinates = coordinatesArray.getDoubleArray(offset, length);
        final CoordinatesList coordinatesList = new DoubleCoordinatesList(numAxis, ordinates);
        final LinearRing ring = this.geometryFactory.createLinearRing(coordinatesList);

        switch ((int)type) {
          case 1003:
            if (exteriorRing == null) {
              exteriorRing = ring;
            } else {
              throw new IllegalArgumentException("Cannot have two exterior rings on a geometry");
            }
          break;
          case 2003:
            if (numInteriorRings == interiorRings.length) {
              throw new IllegalArgumentException("Too many interior rings");
            } else {
              interiorRings[numInteriorRings++] = ring;
            }
          break;

          default:
            throw new IllegalArgumentException("Unsupported geometry type " + type);
        }
      } else {
        throw new IllegalArgumentException("Unsupported geometry type " + type + " interpretation "
          + interpretation);
      }
    }
    final Polygon polygon = this.geometryFactory.createPolygon(exteriorRing, interiorRings);
    return polygon;
  }
}
