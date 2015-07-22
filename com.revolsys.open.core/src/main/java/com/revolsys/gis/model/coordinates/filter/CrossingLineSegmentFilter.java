package com.revolsys.gis.model.coordinates.filter;

import java.util.function.Predicate;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineSegment;

public class CrossingLineSegmentFilter implements Predicate<LineSegment> {
  private final LineSegment line;

  public CrossingLineSegmentFilter(final LineSegment line) {
    this.line = line;
  }

  @Override
  public boolean test(final LineSegment line) {
    if (this.line == line) {
      return false;
    } else {
      final Geometry intersection = this.line.getIntersection(line);
      if (intersection instanceof Point) {
        final Point intersectionPoint = (Point)intersection;
        if (this.line.isEndPoint(intersectionPoint)) {
          return false;
        } else {
          return true;
        }
      } else {
        return false;
      }
    }
  }
}