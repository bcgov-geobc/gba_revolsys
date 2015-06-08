package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;
import com.vividsolutions.jts.geom.LineString;

public class LineFilter<T> implements Filter<Edge<T>> {
  private final Filter<LineString> filter;

  public LineFilter(final Filter<LineString> filter) {
    this.filter = filter;
  }

  @Override
  public boolean accept(final Edge<T> edge) {
    final LineString line = edge.getLine();
    return this.filter.accept(line);
  }

}
