package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.Coordinates;

public interface CoordinatesPrecisionModel {
  Coordinates getPreciseCoordinates(Coordinates coordinates);

  double getResolutionXy();

  double getResolutionZ();

  double getScaleXY();

  double getScaleZ();

  boolean isFloating();

  void makePrecise(int axisCount, double... coordinates);

  double makeXyPrecise(final double value);

  double makeZPrecise(final double value);

}
