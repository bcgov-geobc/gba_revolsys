package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;

public class PointArrayCoordinatesList extends AbstractCoordinatesList {
  private static final long serialVersionUID = 5567278244212676984L;

  private byte numAxis;

  private List<Coordinates> points = new ArrayList<Coordinates>();

  public PointArrayCoordinatesList() {
  }

  public PointArrayCoordinatesList(final byte numAxis) {
    this.numAxis = numAxis;
  }

  public PointArrayCoordinatesList(final byte numAxis,
    final Coordinates... points) {
    this.numAxis = numAxis;
    for (final Coordinates point : points) {
      add(point);
    }
  }

  public void add(final Coordinates coordinates) {
    points.add(coordinates);
  }

  public void clear() {
    points.clear();
  }

  @Override
  public PointArrayCoordinatesList clone() {
    final PointArrayCoordinatesList clone = (PointArrayCoordinatesList)super.clone();
    clone.points = new ArrayList<Coordinates>(points);
    return null;
  }

  @Override
  public byte getNumAxis() {
    return numAxis;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final Coordinates point = points.get(index);
    return point.getValue(axisIndex);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final Coordinates point = points.get(index);
    point.setValue(axisIndex, value);
  }

  @Override
  public int size() {
    return points.size();
  }

}
