/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public enum GeometryType {
  geometryNull(0), geometryPoint(1), geometryMultipoint(2), geometryPolyline(3), geometryPolygon(
    4), geometryMultiPatch(9);

  private static class SwigNext {
    private static int next = 0;
  }

  public static GeometryType swigToEnum(final int swigValue) {
    final GeometryType[] swigValues = GeometryType.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0
        && swigValues[swigValue].swigValue == swigValue) {
      return swigValues[swigValue];
    }
    for (final GeometryType swigEnum : swigValues) {
      if (swigEnum.swigValue == swigValue) {
        return swigEnum;
      }
    }
    throw new IllegalArgumentException("No enum " + GeometryType.class
      + " with value " + swigValue);
  }

  private final int swigValue;

  @SuppressWarnings("unused")
  private GeometryType() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private GeometryType(final GeometryType swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue + 1;
  }

  @SuppressWarnings("unused")
  private GeometryType(final int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue + 1;
  }

  public final int swigValue() {
    return this.swigValue;
  }
}
