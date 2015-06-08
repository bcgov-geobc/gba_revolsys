package com.revolsys.gis.model.geometry.impl;

import java.util.Collections;
import java.util.List;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.operation.buffer.BufferOp;
import com.revolsys.gis.model.geometry.operation.distance.DistanceOp;
import com.revolsys.gis.model.geometry.operation.overlay.OverlayOp;
import com.revolsys.gis.model.geometry.operation.overlay.SnapIfNeededOverlayOp;
import com.revolsys.gis.model.geometry.operation.relate.RelateOp;
import com.revolsys.gis.model.geometry.operation.valid.IsValidOp;
import com.revolsys.gis.model.geometry.util.WktWriter;
import com.revolsys.io.AbstractObjectWithProperties;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public abstract class GeometryImpl extends AbstractObjectWithProperties implements Geometry {
  private final GeometryFactoryImpl geometryFactory;

  private BoundingBox boundingBox;

  protected GeometryImpl(final GeometryFactoryImpl geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public Geometry buffer(final double distance) {
    return BufferOp.bufferOp(this, distance);
  }

  @Override
  public Geometry buffer(final double distance, final int quadrantSegments) {
    return BufferOp.bufferOp(this, distance, quadrantSegments);
  }

  @Override
  public Geometry buffer(final double distance, final int quadrantSegments, final int endCapStyle) {
    return BufferOp.bufferOp(this, distance, quadrantSegments, endCapStyle);
  }

  @Override
  public GeometryImpl clone() {
    try {
      return (GeometryImpl)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new UnsupportedOperationException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G cloneGeometry() {
    return (G)clone();
  }

  @Override
  public boolean contains(Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    if (boundingBox.contains(boundingBox2)) {
      geometry = getConvertedGeometry(geometry);
      // // optimization for rectangle arguments
      // if (isRectangle()) {
      // return RectangleContains.contains((Polygon) this, g);
      // }
      // general case
      return doContains(geometry);
    } else {
      return false;
    }

  }

  @Override
  public boolean coveredBy(Geometry geometry) {
    geometry = getConvertedGeometry(geometry);
    return geometry.covers(this);
  }

  @Override
  public boolean covers(Geometry geometry) {

    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    if (boundingBox.covers(boundingBox2)) {
      // // optimization for rectangle arguments
      // if (isRectangle) {
      // return true;
      // }
      geometry = getConvertedGeometry(geometry);
      return doCovers(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean crosses(Geometry geometry) {
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox.intersects(boundingBox2)) {
      geometry = getConvertedGeometry(geometry);
      return doCrossses(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean disjoint(final Geometry geometry) {
    return !intersects(geometry);
  }

  @Override
  public double distance(final Geometry g) {
    return DistanceOp.distance(this, g);
  }

  protected boolean doContains(final Geometry geometry) {
    return relate(geometry).isContains();
  }

  protected boolean doCovers(final Geometry geometry) {
    return relate(geometry).isCovers();
  }

  protected boolean doCrossses(final Geometry geometry) {
    return relate(geometry).isCrosses(getDimension(), geometry.getDimension());
  }

  protected boolean doIntersects(final Geometry geometry) {
    return relate(geometry).isIntersects();
  }

  protected boolean doOverlaps(final Geometry geometry) {
    return relate(geometry).isOverlaps(getDimension(), geometry.getDimension());
  }

  protected boolean doTouches(final Geometry geometry) {
    return relate(geometry).isTouches(getDimension(), geometry.getDimension());
  }

  @Override
  public double getArea() {
    return 0;
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      this.boundingBox = new BoundingBox(this);
    }
    return this.boundingBox;
  }

  protected BoundingBox getConvertedBoundingBox(final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    final com.revolsys.gis.model.geometry.GeometryFactory geometryFactory = getGeometryFactory();
    return boundingBox.convert(geometryFactory);
  }

  protected Geometry getConvertedGeometry(Geometry geometry) {
    final com.revolsys.gis.model.geometry.GeometryFactory geometryFactory = getGeometryFactory();
    geometry = geometryFactory.getGeometry(geometry);
    return geometry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> List<G> getGeometries() {
    return (List<G>)Collections.singletonList(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G getGeometry(final int i) {
    return (G)getGeometries().get(0);
  }

  @Override
  public int getGeometryCount() {
    return 1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F extends com.revolsys.gis.model.geometry.GeometryFactory> F getGeometryFactory() {
    return (F)this.geometryFactory;
  }

  @Override
  public double getLength() {
    return 0.0;
  }

  @Override
  public byte getNumAxis() {
    return this.geometryFactory.getNumAxis();
  }

  @Override
  public int getSrid() {
    return this.geometryFactory.getCoordinateSystem().getId();
  }

  @Override
  public Geometry intersection(Geometry geometry) {
    // TODO: MD - add optimization for P-A case using Point-In-Polygon
    final com.revolsys.gis.model.geometry.GeometryFactory geometryFactory = getGeometryFactory();
    if (this.isEmpty() || geometry.isEmpty()) {
      return geometryFactory.createGeometryCollection();
      // } else if (isGeometryCollection(this)) {
      // final Geometry g2 = other;
      // return GeometryCollectionMapper.map((GeometryCollection)this,
      // new GeometryCollectionMapper.MapOp() {
      // public Geometry map(Geometry g) {
      // return g.intersection(g2);
      // }
      // });
    } else {
      // if (isGeometryCollection(other))
      // return other.intersection(this);

      // checkNotGeometryCollection(this);
      // checkNotGeometryCollection(other);
      geometry = getConvertedGeometry(geometry);
      return SnapIfNeededOverlayOp.overlayOp(this, geometry, OverlayOp.INTERSECTION);
    }
  }

  @Override
  public boolean intersects(Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    if (boundingBox.intersects(boundingBox2)) {
      // if (isRectangle()) {
      // return RectangleIntersects.intersects((Polygon) this, g);
      // }
      // if (g.isRectangle()) {
      // return RectangleIntersects.intersects((Polygon) g, this);
      // }
      // general case
      geometry = getConvertedGeometry(geometry);
      return doIntersects(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isValid() {
    return IsValidOp.isValid(this);
  }

  @Override
  public boolean overlaps(Geometry geometry) {
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox.intersects(boundingBox2)) {
      geometry = getConvertedGeometry(geometry);
      return doTouches(geometry);
    } else {
      return false;
    }
  }

  @Override
  public IntersectionMatrix relate(Geometry geometry) {
    geometry = getConvertedGeometry(geometry);
    return RelateOp.relate(this, geometry);
  }

  @Override
  public boolean relate(final Geometry geometry, final String intersectionPattern) {
    return relate(geometry).matches(intersectionPattern);
  }

  @Override
  public String toString() {
    return WktWriter.toString(this);
  }

  @Override
  public boolean touches(Geometry geometry) {
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox.intersects(boundingBox2)) {
      geometry = getConvertedGeometry(geometry);
      return doTouches(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean within(Geometry geometry) {
    geometry = getConvertedGeometry(geometry);
    return geometry.contains(this);
  }
}
