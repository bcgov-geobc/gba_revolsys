package com.revolsys.gis.model.geometry.impl;

import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.operation.PolygonContains;
import com.revolsys.gis.model.geometry.operation.PolygonCovers;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.revolsys.gis.model.geometry.operation.chain.SegmentStringUtil;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.FastSegmentSetIntersectionFinder;
import com.revolsys.gis.model.geometry.util.PolygonUtil;

public class PolygonImpl extends GeometryImpl implements Polygon, Iterable<LinearRing> {

  private final MultiLinearRing rings;

  protected PolygonImpl(final GeometryFactoryImpl geometryFactory, final List<LinearRing> rings) {
    super(geometryFactory);
    this.rings = geometryFactory.createMultiLinearRing(rings);
  }

  @Override
  public PolygonImpl clone() {
    return (PolygonImpl)super.clone();
  }

  @Override
  protected boolean doContains(final Geometry geometry) {
    return new PolygonContains(this).contains(geometry);
  }

  @Override
  protected boolean doCovers(final Geometry geometry) {
    return new PolygonCovers(this).covers(geometry);
  }

  @Override
  protected boolean doIntersects(final Geometry geometry) {
    /**
     * Do point-in-poly tests first, since they are cheaper and may result in a
     * quick positive result. If a point of any test components lie in target,
     * result is true
     */
    final boolean isInPrepGeomArea = PolygonUtil.isAnyTestComponentInTarget(this, geometry);
    if (isInPrepGeomArea) {
      return true;
    } else {
      /**
       * If any segments intersect, result is true
       */
      final List<SegmentString> lineSegStr = SegmentStringUtil.extractSegmentStrings(geometry);
      final boolean segsIntersect = FastSegmentSetIntersectionFinder.get(this).intersects(
        lineSegStr);
      if (segsIntersect) {
        return true;
      } else {
        /**
         * If the test has dimension = 2 as well, it is necessary to test for
         * proper inclusion of the target. Since no segments intersect, it is
         * sufficient to test representative points.
         */
        if (geometry.getDimension() == 2) {
          // TODO: generalize this to handle GeometryCollections
          final boolean isPrepGeomInArea = PolygonUtil.isAnyTargetComponentInAreaTest(geometry,
            getCoordinatesLists());
          if (isPrepGeomInArea) {
            return true;
          }
        }
      }
      return false;
    }
  }

  @Override
  public double getArea() {
    double area = 0.0;
    final MultiLinearRing rings = getRings();
    final LinearRing exteriorRing = rings.getGeometry(0);
    area += Math.abs(CoordinatesListUtil.signedArea(exteriorRing));
    for (int i = 1; i < rings.getGeometryCount(); i++) {
      final LinearRing ring = rings.getGeometry(i);
      area -= Math.abs(CoordinatesListUtil.signedArea(ring));
    }
    return area;
  }

  @Override
  public int getBoundaryDimension() {
    return 1;
  }

  @Override
  public List<CoordinatesList> getCoordinatesLists() {
    return this.rings.getCoordinatesLists();
  }

  @Override
  public int getDimension() {
    return 2;
  }

  @Override
  public LinearRing getExteriorRing() {
    return getRing(0);
  }

  @Override
  public Point getFirstPoint() {
    return this.rings.getFirstPoint();
  }

  @Override
  public double getLength() {
    double length = 0.0;
    for (final LinearRing ring : getRings()) {
      length += ring.getLength();
    }
    return length;
  }

  @Override
  public LinearRing getRing(final int index) {
    return getRings().getGeometry(index);
  }

  @Override
  public int getRingCount() {
    return this.rings.getGeometryCount();
  }

  @Override
  public MultiLinearRing getRings() {
    return this.rings;
  }

  @Override
  public boolean isEmpty() {
    return this.rings.isEmpty();
  }

  @Override
  public Iterator<LinearRing> iterator() {
    final List<LinearRing> geometries = this.rings.getGeometries();
    return geometries.iterator();
  }
}
