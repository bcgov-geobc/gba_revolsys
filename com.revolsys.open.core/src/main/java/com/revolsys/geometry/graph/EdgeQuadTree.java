package com.revolsys.geometry.graph;

import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.algorithm.index.AbstractIdObjectQuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;

public class EdgeQuadTree<T> extends AbstractIdObjectQuadTree<Edge<T>> {
  private final Graph<T> graph;

  public EdgeQuadTree(final Graph<T> graph) {
    this.graph = graph;
    final Collection<Integer> ids = graph.getEdgeIds();
    add(ids);
  }

  @Override
  public BoundingBox getEnvelope(final Edge<T> edge) {
    if (edge == null) {
      return BoundingBox.EMPTY;
    } else {
      final LineString line = edge.getLine();
      if (line == null) {
        return BoundingBox.EMPTY;
      } else {
        final BoundingBox envelope = line.getBoundingBox();
        return envelope;
      }
    }
  }

  @Override
  public int getId(final Edge<T> edge) {
    return edge.getId();
  }

  @Override
  public Edge<T> getObject(final Integer id) {
    return this.graph.getEdge(id);
  }

  @Override
  public List<Edge<T>> getObjects(final List<Integer> ids) {
    return this.graph.getEdges(ids);
  }
}
