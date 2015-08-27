package com.revolsys.gis.graph.filter;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import java.util.function.Predicate;

/**
 * A filter for {@link Node} objects which contains an {@link Edge} matching the
 * edge filter.
 *
 * @author Paul Austin
 * @param <T>
 */
public class NodeMatchingEdgeFilter<T> implements Predicate<Node<T>> {

  private final Predicate<Edge<T>> edgeFilter;

  public NodeMatchingEdgeFilter(final Predicate<Edge<T>> edgeFilter) {
    this.edgeFilter = edgeFilter;
  }

  @Override
  public boolean test(final Node<T> node) {
    for (final Edge<T> edge : node.getEdges()) {
      if (this.edgeFilter.test(edge)) {
        return true;
      }
    }
    return false;
  }

}
