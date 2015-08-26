package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;
import com.vividsolutions.jts.geom.LineString;

public class NodeOnEdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Node<T> node,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final Coordinates point = node;
    BoundingBox boundingBox = new BoundingBox(point);
    boundingBox = boundingBox.expand(maxDistance);
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    final NodeOnEdgeVisitor<T> visitor = new NodeOnEdgeVisitor<T>(node, boundingBox, maxDistance,
      results);
    index.visit(boundingBox, visitor);
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;

  }

  private final BoundingBox boundingBox;

  private final double maxDistance;

  private final Node<T> node;

  private final Coordinates point;

  public NodeOnEdgeVisitor(final Node<T> node, final BoundingBox boundingBox,
    final double maxDistance, final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.node = node;
    this.boundingBox = boundingBox;
    this.maxDistance = maxDistance;
    this.point = node;
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    if (!edge.hasNode(this.node)) {
      final LineString line = edge.getLine();
      if (line.getEnvelopeInternal().intersects(this.boundingBox)) {
        if (LineStringUtil.isPointOnLine(line, this.point, this.maxDistance)) {
          super.visit(edge);
        }
      }
    }
    return true;
  }

}
