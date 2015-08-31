package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;

public class CoordinatesDistanceComparator implements Comparator<Point> {

  private final boolean invert;

  private final Point point;

  public CoordinatesDistanceComparator(final Point point) {
    this.point = point;
    this.invert = false;
  }

  public CoordinatesDistanceComparator(final Point point, final boolean invert) {
    this.point = point;
    this.invert = invert;
  }

  @Override
  public int compare(final Point point1, final Point point2) {
    int compare;
    final double distance1 = point1.distance(this.point);
    final double distance2 = point2.distance(this.point);
    if (distance1 == distance2) {
      compare = point1.compareTo(point2);
    } else if (distance1 < distance2) {
      compare = -1;
    } else {
      compare = 1;
    }

    if (this.invert) {
      return -compare;
    } else {
      return compare;
    }
  }

  public boolean isInvert() {
    return this.invert;
  }

}
