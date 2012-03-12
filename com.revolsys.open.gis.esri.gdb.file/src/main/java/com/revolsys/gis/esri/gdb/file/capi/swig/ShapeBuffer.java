/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.3
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class ShapeBuffer {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  public ShapeBuffer(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(ShapeBuffer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_ShapeBuffer(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public boolean Allocate(long length) {
    return EsriFileGdbJNI.ShapeBuffer_Allocate(swigCPtr, this, length);
  }

  public ShapeBuffer(long length) {
    this(EsriFileGdbJNI.new_ShapeBuffer__SWIG_0(length), true);
  }

  public ShapeBuffer() {
    this(EsriFileGdbJNI.new_ShapeBuffer__SWIG_1(), true);
  }

  public void setAllocatedLength(long value) {
    EsriFileGdbJNI.ShapeBuffer_allocatedLength_set(swigCPtr, this, value);
  }

  public long getAllocatedLength() {
    return EsriFileGdbJNI.ShapeBuffer_allocatedLength_get(swigCPtr, this);
  }

  public void setInUseLength(long value) {
    EsriFileGdbJNI.ShapeBuffer_inUseLength_set(swigCPtr, this, value);
  }

  public long getInUseLength() {
    return EsriFileGdbJNI.ShapeBuffer_inUseLength_get(swigCPtr, this);
  }

  public boolean IsEmpty() {
    return EsriFileGdbJNI.ShapeBuffer_IsEmpty(swigCPtr, this);
  }

  public void SetEmpty() {
    EsriFileGdbJNI.ShapeBuffer_SetEmpty(swigCPtr, this);
  }

  public static boolean HasZs(ShapeType shapeType) {
    return EsriFileGdbJNI.ShapeBuffer_HasZs(shapeType.swigValue());
  }

  public static boolean HasMs(ShapeType shapeType) {
    return EsriFileGdbJNI.ShapeBuffer_HasMs(shapeType.swigValue());
  }

  public static boolean HasIDs(ShapeType shapeType) {
    return EsriFileGdbJNI.ShapeBuffer_HasIDs(shapeType.swigValue());
  }

  public static boolean HasCurves(ShapeType shapeType) {
    return EsriFileGdbJNI.ShapeBuffer_HasCurves(shapeType.swigValue());
  }

  public static boolean HasNormals(ShapeType shapeType) {
    return EsriFileGdbJNI.ShapeBuffer_HasNormals(shapeType.swigValue());
  }

  public static boolean HasTextures(ShapeType shapeType) {
    return EsriFileGdbJNI.ShapeBuffer_HasTextures(shapeType.swigValue());
  }

  public static boolean HasMaterials(ShapeType shapeType) {
    return EsriFileGdbJNI.ShapeBuffer_HasMaterials(shapeType.swigValue());
  }

  public short get(int i) {
    return EsriFileGdbJNI.ShapeBuffer_get(swigCPtr, this, i);
  }

  public void set(int i, short c) {
    EsriFileGdbJNI.ShapeBuffer_set(swigCPtr, this, i, c);
  }

  public UnsignedCharArray getShapeBuffer() {
  return new UnsignedCharArray(EsriFileGdbJNI.ShapeBuffer_getShapeBuffer(swigCPtr, this), false);
}

  public ShapeType getShapeType() {
    return ShapeType.swigToEnum(EsriFileGdbJNI.ShapeBuffer_getShapeType(swigCPtr, this));
  }

  public GeometryType getGeometryType() {
    return GeometryType.swigToEnum(EsriFileGdbJNI.ShapeBuffer_getGeometryType(swigCPtr, this));
  }

}
