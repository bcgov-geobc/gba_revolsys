package com.revolsys.gis.data.model.comparator;

import java.util.Comparator;

import com.revolsys.data.record.Record;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Polygon;

public class RecordGeometryAreaComparitor implements Comparator<Record> {

  private boolean clockwise = false;

  private boolean decending = false;

  public RecordGeometryAreaComparitor() {
  }

  public RecordGeometryAreaComparitor(final boolean decending, final boolean clockwise) {
    this.decending = decending;
    this.clockwise = clockwise;
  }

  @Override
  public int compare(final Record object1, final Record object2) {
    if (object1 == object2) {
      return 0;
    }
    int compare = -1;
    final Polygon geometry1 = object1.getGeometryValue();
    final Polygon geometry2 = object2.getGeometryValue();
    final double area1 = geometry1.getArea();
    final double area2 = geometry2.getArea();
    compare = Double.compare(area1, area2);
    if (compare == 0) {
      compare = geometry1.compareTo(geometry2);
      if (compare == 0) {
        final boolean clockwise1 = !CGAlgorithms.isCCW(geometry1.getExteriorRing().getCoordinates());
        final boolean clockwise2 = !CGAlgorithms.isCCW(geometry2.getExteriorRing().getCoordinates());
        if (clockwise1) {
          if (clockwise2) {
            return 0;
          } else {
            if (this.clockwise) {
              compare = -1;
            } else {
              compare = 1;
            }
          }
        } else {
          if (clockwise2) {
            if (this.clockwise) {
              compare = 1;
            } else {
              compare = -1;
            }
          } else {
            return 0;
          }
        }
      }
    }
    if (this.decending) {
      return -compare;
    } else {
      return compare;
    }
  }

}
