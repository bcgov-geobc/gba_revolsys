package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.visitor.CreateListVisitor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class NodeLessThanDistanceOfGeometryVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final Geometry geometry,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    BoundingBox env = BoundingBox.getBoundingBox(geometry);
    env = env.expand(maxDistance);
    final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
    final NodeLessThanDistanceOfGeometryVisitor<T> visitor = new NodeLessThanDistanceOfGeometryVisitor<T>(
      geometry, maxDistance, results);
    index.visit(env, visitor);
    return results.getList();
  }

  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeLessThanDistanceOfGeometryVisitor(final Geometry geometry, final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
    this.geometryFactory = GeometryFactory.getFactory(geometry);
  }

  @Override
  public boolean visit(final Node<T> node) {
    final Coordinates coordinate = node;
    final Point point = this.geometryFactory.createPoint(coordinate);
    final double distance = this.geometry.distance(point);
    if (distance < this.maxDistance) {
      this.matchVisitor.visit(node);
    }
    return true;
  }

}
