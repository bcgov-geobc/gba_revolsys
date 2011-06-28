/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class VectorOfString {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected VectorOfString(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(VectorOfString obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_VectorOfString(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public VectorOfString() {
    this(EsriFileGdbJNI.new_VectorOfString__SWIG_0(), true);
  }

  public VectorOfString(long n) {
    this(EsriFileGdbJNI.new_VectorOfString__SWIG_1(n), true);
  }

  public long size() {
    return EsriFileGdbJNI.VectorOfString_size(swigCPtr, this);
  }

  public long capacity() {
    return EsriFileGdbJNI.VectorOfString_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    EsriFileGdbJNI.VectorOfString_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return EsriFileGdbJNI.VectorOfString_isEmpty(swigCPtr, this);
  }

  public void clear() {
    EsriFileGdbJNI.VectorOfString_clear(swigCPtr, this);
  }

  public void add(String x) {
    EsriFileGdbJNI.VectorOfString_add(swigCPtr, this, x);
  }

  public String get(int i) {
    return EsriFileGdbJNI.VectorOfString_get(swigCPtr, this, i);
  }

  public void set(int i, String val) {
    EsriFileGdbJNI.VectorOfString_set(swigCPtr, this, i, val);
  }

}
