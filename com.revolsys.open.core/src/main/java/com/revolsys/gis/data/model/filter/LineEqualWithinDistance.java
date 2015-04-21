package com.revolsys.gis.data.model.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.linestring.LineStringRelate;
import com.vividsolutions.jts.geom.LineString;

public class LineEqualWithinDistance implements Filter<LineString> {

  public static Filter<Record> getFilter(final Record object,
    final double maxDistance) {
    final LineString line = object.getGeometryValue();
    final LineEqualWithinDistance lineFilter = new LineEqualWithinDistance(
      line, maxDistance);
    return new DataObjectGeometryFilter<LineString>(lineFilter);
  }

  private final double maxDistance;

  private final LineString line;

  public LineEqualWithinDistance(final LineString line, final double maxDistance) {
    this.line = line;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean accept(final LineString line2) {
    final LineStringRelate relate = new LineStringRelate(line, line2,
      maxDistance);
    return relate.isEqual();
  }
}
