package com.revolsys.gis.cs;

import java.util.List;

import junit.framework.Assert;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryFactoryTest {
  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory(
    3857, 2, 1, 1);

  public static void assertCoordinatesListEqual(final Geometry geometry,
    final CoordinatesList... pointsList) {
    System.out.println(geometry);
    final List<CoordinatesList> geometryPointsList = CoordinatesListUtil.getAll(geometry);
    Assert.assertEquals("Number of coordinates Lists", pointsList.length,
      geometryPointsList.size());
    for (int i = 0; i < pointsList.length; i++) {
      final CoordinatesList points = pointsList[i];
      final CoordinatesList geometryPoints = geometryPointsList.get(i);
      Assert.assertEquals("Coordinates not equal", points, geometryPoints);
    }
  }

  public static void assertCopyGeometry(final Geometry geometry,
    final CoordinatesList... pointsList) {
    assertCoordinatesListEqual(geometry, pointsList);
    final Geometry copy = GEOMETRY_FACTORY.copy(geometry);
    final Class<? extends Geometry> geometryClass = geometry.getClass();
    Assert.assertEquals("Geometry class", geometryClass, copy.getClass());
    Assert.assertEquals("Geometry", geometry, copy);
    assertCoordinatesListEqual(copy, pointsList);

    final Geometry copy2 = GEOMETRY_FACTORY.createGeometry(geometryClass,
      geometry);
    Assert.assertEquals("Geometry class", geometryClass, copy2.getClass());
    Assert.assertEquals("Geometry", geometry, copy2);
    assertCoordinatesListEqual(copy2, pointsList);
    assertCreateGeometryCollection(geometry, pointsList);
  }

  public static void assertCreateGeometryCollection(final Geometry geometry,
    final CoordinatesList... pointsList) {
    if (geometry instanceof GeometryCollection) {
      if (geometry.getNumGeometries() == 1) {
        final Geometry part = geometry.getGeometryN(0);
        final Class<? extends Geometry> geometryClass = geometry.getClass();

        final Geometry copy2 = GEOMETRY_FACTORY.createGeometry(geometryClass,
          part);
        Assert.assertEquals("Geometry class", geometryClass, copy2.getClass());
        Assert.assertEquals("Geometry", geometry, copy2);
        assertCoordinatesListEqual(copy2, pointsList);
      }
    } else if (!(geometry instanceof LinearRing)) {
      final GeometryCollection collection = GEOMETRY_FACTORY.createCollection(geometry);
      final Geometry copy = collection.getGeometryN(0);
      final Class<? extends Geometry> geometryClass = geometry.getClass();
      Assert.assertEquals("Geometry class", geometryClass, copy.getClass());
      Assert.assertEquals("Geometry", geometry, copy);
      assertCoordinatesListEqual(collection, pointsList);

      final Geometry copy2 = GEOMETRY_FACTORY.createGeometry(geometryClass,
        collection);
      Assert.assertEquals("Geometry class", geometryClass, copy2.getClass());
      Assert.assertEquals("Geometry", geometry, copy2);
      assertCoordinatesListEqual(copy2, pointsList);
    }

  }

  public static void main(final String[] args) {
    testCreateGeometry();
  }

  private static void testCreateGeometry() {
    final CoordinatesList pointPoints = GEOMETRY_FACTORY.createCoordinatesList(
      0, 0);
    final CoordinatesList point2Points = GEOMETRY_FACTORY.createCoordinatesList(
      20, 20);
    final CoordinatesList ringPoints = GEOMETRY_FACTORY.createCoordinatesList(
      0, 0, 0, 100, 100, 100, 100, 0, 0, 0);
    final CoordinatesList ring2Points = GEOMETRY_FACTORY.createCoordinatesList(
      20, 20, 20, 80, 80, 80, 80, 20, 20, 20);
    final CoordinatesList ring3Points = GEOMETRY_FACTORY.createCoordinatesList(
      120, 120, 120, 180, 180, 180, 180, 120, 120, 120);

    final Point point = GEOMETRY_FACTORY.createPoint(pointPoints);
    assertCopyGeometry(point, pointPoints);

    final LineString line = GEOMETRY_FACTORY.createLineString(ringPoints);
    assertCopyGeometry(line, ringPoints);

    final LinearRing linearRing = GEOMETRY_FACTORY.createLinearRing(ringPoints);
    assertCopyGeometry(linearRing, ringPoints);

    final Polygon polygon = GEOMETRY_FACTORY.createPolygon(ringPoints);
    assertCopyGeometry(polygon, ringPoints);

    final Polygon polygon2 = GEOMETRY_FACTORY.createPolygon(ringPoints,
      ring2Points);
    assertCopyGeometry(polygon2, ringPoints, ring2Points);

    final MultiPoint multiPoint = GEOMETRY_FACTORY.createMultiPoint(pointPoints);
    assertCopyGeometry(multiPoint, pointPoints);

    final MultiPoint multiPoint2 = GEOMETRY_FACTORY.createMultiPoint(
      pointPoints, point2Points);
    assertCopyGeometry(multiPoint2, pointPoints, point2Points);

    final MultiLineString multiLineString = GEOMETRY_FACTORY.createMultiLineString(ringPoints);
    assertCopyGeometry(multiLineString, ringPoints);

    final MultiLineString multiLineString2 = GEOMETRY_FACTORY.createMultiLineString(
      ringPoints, ring2Points);
    assertCopyGeometry(multiLineString2, ringPoints, ring2Points);

    final MultiPolygon multiPolygon = GEOMETRY_FACTORY.createMultiPolygon(ringPoints);
    assertCopyGeometry(multiPolygon, ringPoints);

    final MultiPolygon multiPolygon2 = GEOMETRY_FACTORY.createMultiPolygon(
      ringPoints, ring3Points);
    assertCopyGeometry(multiPolygon2, ringPoints, ring3Points);

  }
}
