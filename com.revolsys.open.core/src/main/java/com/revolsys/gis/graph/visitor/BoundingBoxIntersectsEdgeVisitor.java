package com.revolsys.gis.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class BoundingBoxIntersectsEdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Edge<T> edge,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();

    final LineString line = edge.getLine();
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    BoundingBox boundingBox = new BoundingBox(geometryFactory, line.getEnvelopeInternal());
    boundingBox = boundingBox.expand(maxDistance);
    final BoundingBoxIntersectsEdgeVisitor<T> visitor = new BoundingBoxIntersectsEdgeVisitor<T>(
      boundingBox, results);
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.visit(boundingBox, visitor);
    final List<Edge<T>> list = results.getList();
    list.remove(edge);
    return list;

  }

  private final BoundingBox boundingBox;

  public BoundingBoxIntersectsEdgeVisitor(final BoundingBox boundingBox,
    final Consumer<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.boundingBox = boundingBox;
  }

  @Override
  public void accept(final Edge<T> edge) {
    final Envelope envelope = edge.getEnvelope();
    if (this.boundingBox.intersects(envelope)) {
      super.accept(edge);
    }
  }
}
