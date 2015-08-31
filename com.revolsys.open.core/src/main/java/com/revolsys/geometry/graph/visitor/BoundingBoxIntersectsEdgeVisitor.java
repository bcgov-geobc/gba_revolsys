package com.revolsys.geometry.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.algorithm.index.IdObjectIndex;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class BoundingBoxIntersectsEdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Edge<T> edge,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();

    final LineString line = edge.getLine();
    BoundingBox boundingBox = line.getBoundingBox();
    boundingBox = boundingBox.expand(maxDistance);
    final BoundingBoxIntersectsEdgeVisitor<T> visitor = new BoundingBoxIntersectsEdgeVisitor<T>(
      boundingBox, results);
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.forEach(visitor, boundingBox);
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
    final com.revolsys.geometry.model.BoundingBox envelope = edge.getEnvelope();
    if (this.boundingBox.intersects(envelope)) {
      super.accept(edge);
    }
  }
}
