package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.MathUtil;

public class CustomRectangularMapGrid extends AbstractRectangularMapGrid {

  private GeometryFactory geometryFactory;

  private double originX;

  private double originY;

  private double tileHeight;

  private double tileWidth;

  public BoundingBox getBoundingBox(final String name) {
    final double[] coordinates = MathUtil.toDoubleArraySplit(name, "_");
    if (coordinates.length == 2) {
      final double x1 = coordinates[0];
      final double y1 = coordinates[1];
      final double x2 = x1 + this.tileWidth;
      final double y2 = y1 + this.tileHeight;
      return new BoundingBox(this.geometryFactory, x1, y1, x2, y2);
    } else {
      return null;
    }
  }

  @Override
  public CoordinateSystem getCoordinateSystem() {
    return this.geometryFactory.getCoordinateSystem();
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    return name;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public double getGridValue(final double origin, final double gridSize, final double value) {
    final int xIndex = (int)Math.floor((value - origin) / gridSize);
    final double minX = origin + xIndex * gridSize;
    return minX;
  }

  public String getMapTileName(final Coordinates coordinates) {
    final double x = coordinates.getX();
    final double y = coordinates.getY();
    return getMapTileName(x, y);
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final double tileX = getGridValue(this.originX, this.tileWidth, x);
    final double tileY = getGridValue(this.originY, this.tileHeight, y);

    return MathUtil.toString(tileX, 1) + "_" + MathUtil.toString(tileY, 1);
  }

  public double getOriginX() {
    return this.originX;
  }

  public double getOriginY() {
    return this.originY;
  }

  @Override
  public RectangularMapTile getTileByLocation(final double x, final double y) {
    final String name = getMapTileName(x, y);
    if (name == null) {
      return null;
    } else {
      return getTileByName(name);
    }
  }

  @Override
  public RectangularMapTile getTileByName(final String name) {
    final BoundingBox boundingBox = getBoundingBox(name);
    if (boundingBox == null) {
      return null;
    } else {
      return new SimpleRectangularMapTile(this, name, name, boundingBox);
    }
  }

  @Override
  public double getTileHeight() {
    return this.tileHeight;
  }

  @Override
  public List<RectangularMapTile> getTiles(final BoundingBox boundingBox) {
    final BoundingBox envelope = boundingBox.convert(getGeometryFactory());

    final List<RectangularMapTile> tiles = new ArrayList<RectangularMapTile>();
    final double minX = getGridValue(this.originX, this.tileWidth, envelope.getMinX());
    final double minY = getGridValue(this.originY, this.tileHeight, envelope.getMinY());
    final double maxX = getGridValue(this.originX, this.tileWidth, envelope.getMaxX());
    final double maxY = getGridValue(this.originY, this.tileHeight, envelope.getMaxY());

    final int numX = (int)Math.ceil((maxX - minX) / this.tileWidth);
    final int numY = (int)Math.ceil((maxY - minY) / this.tileWidth);
    if (numX > 8 || numY > 8) {
      return tiles;
    }
    for (int i = 0; i < numY; i++) {
      final double y = minY + i * this.tileHeight;
      for (int j = 0; j < numX; j++) {
        final double x = minX + j * this.tileWidth;
        final RectangularMapTile tile = getTileByLocation(x, y);
        tiles.add(tile);
      }
    }
    return tiles;
  }

  @Override
  public double getTileWidth() {
    return this.tileWidth;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setOriginX(final double originX) {
    this.originX = originX;
  }

  public void setOriginY(final double originY) {
    this.originY = originY;
  }

  public void setTileHeight(final double tileHeight) {
    this.tileHeight = tileHeight;
  }

  public void setTileWidth(final double tileWidth) {
    this.tileWidth = tileWidth;
  }

}
