package com.revolsys.gis.algorithm.locate;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;

public class PointInArea extends RayCrossingCounter implements Visitor<LineSegment> {

  private final GeometryFactory geometryFactory;

  public PointInArea(final GeometryFactory geometryFactory, final double x, final double y) {
    super(x, y);
    this.geometryFactory = geometryFactory;
  }

  @Override
  public boolean visit(final LineSegment segment) {
    final double x1 = segment.getX(0);
    final double y1 = segment.getY(0);
    final double x2 = segment.getX(1);
    final double y2 = segment.getY(1);
    if (LineSegmentUtil.distance(x1, y1, x2, y2, getX(), getY()) < 1
      / this.geometryFactory.getScaleXY()) {
      setPointOnSegment(true);
    } else {
      countSegment(x1, y1, x2, y2);
    }
    return true;
  }
}
