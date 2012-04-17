/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.3
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class Envelope {
  public static long getCPtr(final Envelope obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public Envelope() {
    this(EsriFileGdbJNI.new_Envelope__SWIG_0(), true);
  }

  public Envelope(final double xmin, final double xmax, final double ymin,
    final double ymax) {
    this(EsriFileGdbJNI.new_Envelope__SWIG_1(xmin, xmax, ymin, ymax), true);
  }

  public Envelope(final long cPtr, final boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_Envelope(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  @Override
  protected void finalize() {
    delete();
  }

  public double getXMax() {
    return EsriFileGdbJNI.Envelope_xMax_get(swigCPtr, this);
  }

  public double getXMin() {
    return EsriFileGdbJNI.Envelope_xMin_get(swigCPtr, this);
  }

  public double getYMax() {
    return EsriFileGdbJNI.Envelope_yMax_get(swigCPtr, this);
  }

  public double getYMin() {
    return EsriFileGdbJNI.Envelope_yMin_get(swigCPtr, this);
  }

  public double getZMax() {
    return EsriFileGdbJNI.Envelope_zMax_get(swigCPtr, this);
  }

  public double getZMin() {
    return EsriFileGdbJNI.Envelope_zMin_get(swigCPtr, this);
  }

  public boolean IsEmpty() {
    return EsriFileGdbJNI.Envelope_IsEmpty(swigCPtr, this);
  }

  public void SetEmpty() {
    EsriFileGdbJNI.Envelope_SetEmpty(swigCPtr, this);
  }

  public void setXMax(final double value) {
    EsriFileGdbJNI.Envelope_xMax_set(swigCPtr, this, value);
  }

  public void setXMin(final double value) {
    EsriFileGdbJNI.Envelope_xMin_set(swigCPtr, this, value);
  }

  public void setYMax(final double value) {
    EsriFileGdbJNI.Envelope_yMax_set(swigCPtr, this, value);
  }

  public void setYMin(final double value) {
    EsriFileGdbJNI.Envelope_yMin_set(swigCPtr, this, value);
  }

  public void setZMax(final double value) {
    EsriFileGdbJNI.Envelope_zMax_set(swigCPtr, this, value);
  }

  public void setZMin(final double value) {
    EsriFileGdbJNI.Envelope_zMin_set(swigCPtr, this, value);
  }

}
