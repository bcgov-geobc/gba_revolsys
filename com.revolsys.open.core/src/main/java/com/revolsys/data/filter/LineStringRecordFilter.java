package com.revolsys.data.filter;

import java.util.function.Predicate;

import com.revolsys.data.record.Record;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringRecordFilter implements Predicate<Record> {

  public static final LineStringRecordFilter FILTER = new LineStringRecordFilter();

  private LineStringRecordFilter() {
  }

  @Override
  public boolean test(final Record object) {
    final Geometry geometry = object.getGeometry();
    return geometry instanceof LineString;
  }

  @Override
  public String toString() {
    return "LineString";
  }

}
