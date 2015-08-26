package com.revolsys.gis.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;
import com.vividsolutions.jts.geom.LineString;

public class OnLineNodeVisitor<T> implements Consumer<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final LineString line,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    BoundingBox env = BoundingBox.getBoundingBox(line);
    env = env.expand(maxDistance);
    final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
    final OnLineNodeVisitor<T> visitor = new OnLineNodeVisitor<T>(line, results);
    index.visit(env, visitor);
    return results.getList();
  }

  private final LineString line;

  private final Consumer<Node<T>> matchVisitor;

  public OnLineNodeVisitor(final LineString line, final Consumer<Node<T>> matchVisitor) {
    this.line = line;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Node<T> node) {
    final Coordinates point = node;
    if (LineStringUtil.isPointOnLine(this.line, point)) {
      this.matchVisitor.accept(node);
    }
  }

}
