/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class FieldInfo {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected FieldInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(FieldInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_FieldInfo(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public int GetFieldCount(IntValue fieldCount) {
    return EsriFileGdbJNI.FieldInfo_GetFieldCount(swigCPtr, this, fieldCount);
  }

  public int GetFieldName(int fieldNumber, WStringValue fieldName) {
    return EsriFileGdbJNI.FieldInfo_GetFieldName(swigCPtr, this, fieldNumber, fieldName);
  }

  public int GetFieldType(int fieldNumber, FieldTypeValue fieldType) {
    return EsriFileGdbJNI.FieldInfo_GetFieldType(swigCPtr, this, fieldNumber, fieldType);
  }

  public int GetFieldLength(int fieldNumber, IntValue fieldLength) {
    return EsriFileGdbJNI.FieldInfo_GetFieldLength(swigCPtr, this, fieldNumber, fieldLength);
  }

  public int GetFieldIsNullable(int fieldNumber, BoolValue isNullable) {
    return EsriFileGdbJNI.FieldInfo_GetFieldIsNullable(swigCPtr, this, fieldNumber, isNullable);
  }

  public FieldInfo() {
    this(EsriFileGdbJNI.new_FieldInfo(), true);
  }

}
