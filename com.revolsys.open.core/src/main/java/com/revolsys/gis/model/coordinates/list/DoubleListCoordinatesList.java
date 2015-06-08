package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.util.MathUtil;

public class DoubleListCoordinatesList extends AbstractCoordinatesList {

  private static final long serialVersionUID = 4917034011117842840L;

  private List<Double> coordinates = new ArrayList<Double>();

  private final byte numAxis;

  public DoubleListCoordinatesList(final CoordinatesList coordinatesList) {
    this(coordinatesList.getNumAxis(), coordinatesList.getCoordinates());
  }

  public DoubleListCoordinatesList(final CoordinatesList coordinatesList, final int numAxis) {
    this(coordinatesList.size(), numAxis);
    coordinatesList.copy(0, this, 0, numAxis, coordinatesList.size());
  }

  public DoubleListCoordinatesList(final int numAxis) {
    this.numAxis = (byte)numAxis;
  }

  public DoubleListCoordinatesList(final int numAxis, final double... coordinates) {
    this.numAxis = (byte)numAxis;
    for (final double coordinate : coordinates) {
      this.coordinates.add(coordinate);
    }
  }

  public DoubleListCoordinatesList(final int numAxis, final List<Number> coordinates) {
    this(numAxis, MathUtil.toDoubleArray(coordinates));
  }

  public void add(final Coordinates point) {
    for (int axisIndex = 0; axisIndex < this.numAxis; axisIndex++) {
      double value;
      if (axisIndex < point.getNumAxis()) {
        value = point.getValue(axisIndex);
      } else {
        value = Double.NaN;
      }
      this.coordinates.add(value);
    }
  }

  public void add(final CoordinatesList points, final int index) {
    for (int axisIndex = 0; axisIndex < this.numAxis; axisIndex++) {
      double value;
      if (axisIndex < points.getNumAxis()) {
        value = points.getValue(index, axisIndex);
      } else {
        value = Double.NaN;
      }
      this.coordinates.add(value);
    }
  }

  public void addAll(final CoordinatesList points) {
    for (final Coordinates point : new InPlaceIterator(points)) {
      add(point);
    }
  }

  public void clear() {
    this.coordinates.clear();
  }

  @Override
  public DoubleListCoordinatesList clone() {
    final DoubleListCoordinatesList clone = (DoubleListCoordinatesList)super.clone();
    clone.coordinates = new ArrayList<Double>(this.coordinates);
    return clone;
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.size()];
    for (int i = 0; i < coordinates.length; i++) {
      final double coordinate = this.coordinates.get(i);
      coordinates[i] = coordinate;

    }
    return coordinates;
  }

  @Override
  public byte getNumAxis() {
    return this.numAxis;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final byte numAxis = getNumAxis();
    if (axisIndex < numAxis && index < size()) {
      return this.coordinates.get(index * numAxis + axisIndex);
    } else {
      return Double.NaN;
    }
  }

  public void remove(final int index) {
    for (int axisIndex = 0; axisIndex < this.numAxis; axisIndex++) {
      this.coordinates.remove(index * this.numAxis);
    }
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final byte numAxis = getNumAxis();
    if (axisIndex < numAxis) {
      if (index <= size()) {
        for (int i = this.coordinates.size(); i < (index + 1) * numAxis; i++) {
          this.coordinates.add(0.0);
        }
      }
      this.coordinates.set(index * numAxis + axisIndex, value);
    }
  }

  @Override
  public int size() {
    return this.coordinates.size() / this.numAxis;
  }
}
