package com.revolsys.gis.model.coordinates.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.util.MathUtil;

public class DoubleCoordinatesList extends AbstractCoordinatesList {
  /**
   * 
   */
  private static final long serialVersionUID = 7579865828939708871L;

  private double[] coordinates;

  private final byte axisCount;

  public DoubleCoordinatesList(final Coordinates... coordinates) {
    this(3, coordinates);
  }

  public DoubleCoordinatesList(final CoordinatesList coordinatesList) {
    this(coordinatesList.getAxisCount(), coordinatesList);
  }

  public DoubleCoordinatesList(final int axisCount) {
    this(0, axisCount);
  }

  public DoubleCoordinatesList(final int axisCount,
    final Collection<Coordinates> points) {
    this(points.size(), axisCount);
    int i = 0;
    for (final Coordinates point : points) {
      CoordinatesListUtil.setCoordinates(this.coordinates, axisCount, i++,
        point);
    }
  }

  public DoubleCoordinatesList(final int axisCount, final Coordinates... points) {
    this(axisCount, Arrays.asList(points));
  }

  public DoubleCoordinatesList(final int axisCount, final CoordinatesList points) {
    this(points.size(), axisCount);
    int i = 0;
    for (final Coordinates point : points) {
      CoordinatesListUtil.setCoordinates(this.coordinates, axisCount, i++,
        point);
    }
  }

  public DoubleCoordinatesList(final int axisCount, final double... coordinates) {
    assert axisCount > 2;
    this.axisCount = (byte)axisCount;
    this.coordinates = coordinates;
  }

  public DoubleCoordinatesList(final int size, final int axisCount) {
    assert axisCount > 2;
    assert size >= 0;
    this.coordinates = new double[size * axisCount];
    this.axisCount = (byte)axisCount;
  }

  public DoubleCoordinatesList(final int axisCount, final int vertexCount,
    final double... coordinates) {
    assert axisCount > 2;
    this.axisCount = (byte)axisCount;
    final int coordinateCount = vertexCount * axisCount;
    if (coordinates.length % axisCount != 0) {
      throw new IllegalArgumentException("coordinates.length="
        + coordinates.length + " must be a multiple of axisCount=" + axisCount);
    } else if (coordinateCount == coordinates.length) {
      this.coordinates = coordinates;
    } else if (coordinateCount > coordinates.length) {
      throw new IllegalArgumentException("axisCount=" + axisCount
        + " * vertexCount=" + vertexCount + " > coordinates.length="
        + coordinates.length);
    } else {
      this.coordinates = new double[coordinateCount];
      System.arraycopy(coordinates, 0, this.coordinates, 0, coordinateCount);
    }
  }

  public DoubleCoordinatesList(final int axisCount,
    final List<? extends Number> coordinates) {
    this(axisCount, MathUtil.toDoubleArray(coordinates));
  }

  @Override
  public DoubleCoordinatesList clone() {
    return new DoubleCoordinatesList(this);
  }

  @Override
  public int getAxisCount() {
    return axisCount;
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
    return coordinates;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      return coordinates[index * axisCount + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public int size() {
    if (axisCount < 2 || coordinates == null) {
      return 0;
    } else {
      return coordinates.length / axisCount;
    }
  }
}
