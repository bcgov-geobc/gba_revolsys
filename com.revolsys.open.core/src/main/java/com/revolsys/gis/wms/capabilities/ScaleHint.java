package com.revolsys.gis.wms.capabilities;

public class ScaleHint {
  private double min;

  private double max;

  public double getMax() {
    return max;
  }

  public double getMin() {
    return min;
  }

  public void setMax(final double max) {
    this.max = max;
  }

  public void setMin(final double min) {
    this.min = min;
  }

}
