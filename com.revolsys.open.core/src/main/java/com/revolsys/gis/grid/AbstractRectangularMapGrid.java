package com.revolsys.gis.grid;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class AbstractRectangularMapGrid implements RectangularMapGrid {
  private String name;

  @Override
  public BoundingBox getBoundingBox(final String mapTileName, final int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.floating3(srid);
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    return boundingBox.convert(geometryFactory);
  }

  public String getMapTileName(final Geometry geometry) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    final Geometry projectedGeometry = GeometryProjectionUtil.perform(geometry,
      coordinateSystem);
    final Point centroid = projectedGeometry.getCentroid();
    final Coordinate coordinate = centroid.getCoordinate();
    final String mapsheet = getMapTileName(coordinate.x, coordinate.y);
    return mapsheet;
  }

  @Override
  public String getName() {
    if (name == null) {
      return getClass().getName();
    } else {
      return name;
    }
  }

  @Override
  public Polygon getPolygon(final String mapTileName,
    final CoordinateSystem coordinateSystem) {
    return getPolygon(mapTileName, GeometryFactory.getFactory(coordinateSystem));
  }

  @Override
  public Polygon getPolygon(final String mapTileName,
    final GeometryFactory geometryFactory) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory);
    return polygon;
  }

  @Override
  public Polygon getPolygon(final String mapTileName,
    final GeometryFactory geometryFactory, final int numX, final int numY) {
    final RectangularMapTile mapTile = getTileByName(mapTileName);
    final BoundingBox boundingBox = mapTile.getBoundingBox();
    final Polygon polygon = boundingBox.toPolygon(geometryFactory, numX, numY);
    return polygon;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
