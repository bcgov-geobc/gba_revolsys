package com.revolsys.gis.jts;

import java.util.Iterator;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class LineStringCoordinatesListIterator implements Iterator<LineString>,
  Iterable<LineString> {
  private final GeometryFactory factory;

  private int index = 0;

  private final CoordinatesList points;

  public LineStringCoordinatesListIterator(final GeometryFactory factory,
    final CoordinatesList points) {
    this.factory = factory;
    this.points = points;
  }

  public LineStringCoordinatesListIterator(final LineString line) {
    this(GeometryFactory.getFactory(line), CoordinatesListUtil.get(line));
  }

  @Override
  public boolean hasNext() {
    return this.index < this.points.size() - 1;
  }

  @Override
  public Iterator<LineString> iterator() {
    return this;
  }

  @Override
  public LineString next() {
    final LineString lineString = this.factory.createLineString(this.points.subList(this.index, 2));
    this.index++;
    return lineString;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");

  }
}
