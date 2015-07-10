package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;

public class GeometryValueFilter implements Filter<Record> {
  private final Geometry geometry;

  public GeometryValueFilter(final Geometry geometry) {
    this.geometry = geometry;
  }

  public GeometryValueFilter(final Record record) {
    this(record.<Geometry> getGeometry());
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry value = object.getGeometry();
    if (value == this.geometry) {
      return true;
    } else if (value != null && this.geometry != null) {
      return value.equals(this.geometry);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return " geometry == " + this.geometry;
  }

}
