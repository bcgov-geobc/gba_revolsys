package com.revolsys.gis.algorithm.linematch;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;

public class LineSegmentMatchWithinDistanceFilter implements
  Filter<Edge<LineSegmentMatch>> {
  private BoundingBox boundingBox;

  private final double maxDistance;

  private final Node<LineSegmentMatch> node;

  public LineSegmentMatchWithinDistanceFilter(
    final Node<LineSegmentMatch> node, final double maxDistance) {
    this.node = node;
    this.maxDistance = maxDistance;
    this.boundingBox = new BoundingBox(node);
    boundingBox = boundingBox.expand(maxDistance);
  }

  @Override
  public boolean accept(final Edge<LineSegmentMatch> edge) {
    if (!edge.hasNode(node) && edge.distance(node) < maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }
}
