package com.revolsys.gis.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public class NodeWithinBoundingBoxVisitor<T> implements Consumer<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final BoundingBox boundingBox) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
    final NodeWithinBoundingBoxVisitor<T> visitor = new NodeWithinBoundingBoxVisitor<T>(boundingBox,
      results);
    index.visit(boundingBox, visitor);
    return results.getList();
  }

  private final BoundingBox boundingBox;

  private final Consumer<Node<T>> matchVisitor;

  public NodeWithinBoundingBoxVisitor(final BoundingBox boundingBox,
    final Consumer<Node<T>> matchVisitor) {
    this.boundingBox = boundingBox;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Node<T> node) {
    if (this.boundingBox.contains(node)) {
      this.matchVisitor.accept(node);
    }
  }

}
