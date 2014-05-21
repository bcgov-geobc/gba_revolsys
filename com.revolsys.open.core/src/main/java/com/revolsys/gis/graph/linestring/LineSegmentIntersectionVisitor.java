package com.revolsys.gis.graph.linestring;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.visitor.AbstractEdgeListenerVisitor;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.segment.LineSegment;

public class LineSegmentIntersectionVisitor extends
  AbstractEdgeListenerVisitor<LineSegment> {

  private final Set<Geometry> intersections = new LinkedHashSet<>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public Set<Geometry> getIntersections() {
    return intersections;
  }

  @Override
  public boolean visit(final Edge<LineSegment> edge) {
    final LineSegment lineSegment = edge.getObject();
    if (lineSegment.getBoundingBox().intersects(querySeg.getBoundingBox())) {
      final Geometry intersection = querySeg.getIntersection(lineSegment);
      if (intersection != null && !intersection.isEmpty()) {
        intersections.add(intersection);
      }
    }
    return true;
  }
}
