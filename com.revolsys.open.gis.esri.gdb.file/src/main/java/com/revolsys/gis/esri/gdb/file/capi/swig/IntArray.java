/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.3
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class IntArray {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  public IntArray(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(IntArray obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_IntArray(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public IntArray() {
    this(EsriFileGdbJNI.new_IntArray(), true);
  }

  public int get(int i) {
    return EsriFileGdbJNI.IntArray_get(swigCPtr, this, i);
  }

  public void set(int i, int value) {
    EsriFileGdbJNI.IntArray_set(swigCPtr, this, i, value);
  }

}
