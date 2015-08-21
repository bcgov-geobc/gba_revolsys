package com.revolsys.gis.tin;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.Coordinates;

public class TriangleContainsPointFilter implements Filter<Triangle> {
  private final Coordinates point;

  public TriangleContainsPointFilter(final Coordinates point) {
    this.point = point;
  }

  @Override
  public boolean accept(final Triangle triangle) {
    return triangle.contains(this.point);
  }
}
