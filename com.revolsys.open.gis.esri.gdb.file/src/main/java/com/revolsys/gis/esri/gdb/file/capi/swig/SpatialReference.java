/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.5
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class SpatialReference {
  protected static long getCPtr(final SpatialReference obj) {
    return obj == null ? 0 : obj.swigCPtr;
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public SpatialReference() {
    this(EsriFileGdbJNI.new_SpatialReference(), true);
  }

  protected SpatialReference(final long cPtr, final boolean cMemoryOwn) {
    this.swigCMemOwn = cMemoryOwn;
    this.swigCPtr = cPtr;
  }

  public synchronized void delete() {
    if (this.swigCPtr != 0) {
      if (this.swigCMemOwn) {
        this.swigCMemOwn = false;
        EsriFileGdbJNI.delete_SpatialReference(this.swigCPtr);
      }
      this.swigCPtr = 0;
    }
  }

  @Override
  protected void finalize() {
    delete();
  }

  public int getId() {
    return EsriFileGdbJNI.SpatialReference_getId(this.swigCPtr, this);
  }

  public double getMFalseOrigin() {
    return EsriFileGdbJNI.SpatialReference_getMFalseOrigin(this.swigCPtr, this);
  }

  public double getMTolerance() {
    return EsriFileGdbJNI.SpatialReference_getMTolerance(this.swigCPtr, this);
  }

  public double getMUnits() {
    return EsriFileGdbJNI.SpatialReference_getMUnits(this.swigCPtr, this);
  }

  public String getText() {
    return EsriFileGdbJNI.SpatialReference_getText(this.swigCPtr, this);
  }

  public double getXFalseOrigin() {
    return EsriFileGdbJNI.SpatialReference_getXFalseOrigin(this.swigCPtr, this);
  }

  public double getXUnits() {
    return EsriFileGdbJNI.SpatialReference_getXUnits(this.swigCPtr, this);
  }

  public double getXYTolerance() {
    return EsriFileGdbJNI.SpatialReference_getXYTolerance(this.swigCPtr, this);
  }

  public double getXYUnits() {
    return EsriFileGdbJNI.SpatialReference_getXYUnits(this.swigCPtr, this);
  }

  public double getYFalseOrigin() {
    return EsriFileGdbJNI.SpatialReference_getYFalseOrigin(this.swigCPtr, this);
  }

  public double getZTolerance() {
    return EsriFileGdbJNI.SpatialReference_getZTolerance(this.swigCPtr, this);
  }

  public int SetFalseOriginAndUnits(final double falseX, final double falseY,
    final double xyUnits) {
    return EsriFileGdbJNI.SpatialReference_SetFalseOriginAndUnits(
      this.swigCPtr, this, falseX, falseY, xyUnits);
  }

  public int SetMFalseOriginAndUnits(final double falseM, final double mUnits) {
    return EsriFileGdbJNI.SpatialReference_SetMFalseOriginAndUnits(
      this.swigCPtr, this, falseM, mUnits);
  }

  public int SetMTolerance(final double mTolerance) {
    return EsriFileGdbJNI.SpatialReference_SetMTolerance(this.swigCPtr, this,
      mTolerance);
  }

  public int SetSpatialReferenceID(final int wkid) {
    return EsriFileGdbJNI.SpatialReference_SetSpatialReferenceID(this.swigCPtr,
      this, wkid);
  }

  public int SetSpatialReferenceText(final String spatialReference) {
    return EsriFileGdbJNI.SpatialReference_SetSpatialReferenceText(
      this.swigCPtr, this, spatialReference);
  }

  public int SetXYTolerance(final double xyTolerance) {
    return EsriFileGdbJNI.SpatialReference_SetXYTolerance(this.swigCPtr, this,
      xyTolerance);
  }

  public int SetZFalseOriginAndUnits(final double falseZ, final double zUnits) {
    return EsriFileGdbJNI.SpatialReference_SetZFalseOriginAndUnits(
      this.swigCPtr, this, falseZ, zUnits);
  }

  public int SetZTolerance(final double zTolerance) {
    return EsriFileGdbJNI.SpatialReference_SetZTolerance(this.swigCPtr, this,
      zTolerance);
  }

}
