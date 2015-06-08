package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;

public class NodeCoordinatesFilter<T> implements Filter<Node<T>> {
  private Filter<Coordinates> filter;

  public NodeCoordinatesFilter() {
  }

  public NodeCoordinatesFilter(final Filter<Coordinates> filter) {
    this.filter = filter;
  }

  @Override
  public boolean accept(final Node<T> node) {
    return this.filter.accept(node);
  }

  public Filter<Coordinates> getFilter() {
    return this.filter;
  }

  public void setFilter(final Filter<Coordinates> filter) {
    this.filter = filter;
  }
}
