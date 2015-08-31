package com.revolsys.geometry.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.algorithm.index.IdObjectIndex;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public class NodeWithinBoundingBoxVisitor<T> implements Consumer<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final BoundingBox boundingBox) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
    final NodeWithinBoundingBoxVisitor<T> visitor = new NodeWithinBoundingBoxVisitor<T>(boundingBox,
      results);
    index.forEach(visitor, boundingBox);
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
    if (this.boundingBox.covers(node)) {
      this.matchVisitor.accept(node);
    }
  }

}
