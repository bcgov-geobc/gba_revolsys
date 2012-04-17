/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.3
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class Table {
  public static long getCPtr(final Table obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public Table() {
    this(EsriFileGdbJNI.new_Table(), true);
  }

  public Table(final long cPtr, final boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public int AddField(final FieldDef fieldDef) {
    return EsriFileGdbJNI.Table_AddField__SWIG_1(swigCPtr, this,
      FieldDef.getCPtr(fieldDef), fieldDef);
  }

  public int AddField(final String fieldDef) {
    return EsriFileGdbJNI.Table_AddField__SWIG_0(swigCPtr, this, fieldDef);
  }

  public int AddIndex(final IndexDef indexDef) {
    return EsriFileGdbJNI.Table_AddIndex__SWIG_1(swigCPtr, this,
      IndexDef.getCPtr(indexDef), indexDef);
  }

  public int AddIndex(final String indexDef) {
    return EsriFileGdbJNI.Table_AddIndex__SWIG_0(swigCPtr, this, indexDef);
  }

  public int AlterField(final String fieldDef) {
    return EsriFileGdbJNI.Table_AlterField(swigCPtr, this, fieldDef);
  }

  public int AlterSubtype(final String subtypeDef) {
    return EsriFileGdbJNI.Table_AlterSubtype(swigCPtr, this, subtypeDef);
  }

  public Row createRowObject() {
    final long cPtr = EsriFileGdbJNI.Table_createRowObject(swigCPtr, this);
    return (cPtr == 0) ? null : new Row(cPtr, true);
  }

  public int CreateSubtype(final String subtypeDef) {
    return EsriFileGdbJNI.Table_CreateSubtype(swigCPtr, this, subtypeDef);
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_Table(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public int DeleteField(final String fieldName) {
    return EsriFileGdbJNI.Table_DeleteField(swigCPtr, this, fieldName);
  }

  public int DeleteIndex(final String indexName) {
    return EsriFileGdbJNI.Table_DeleteIndex(swigCPtr, this, indexName);
  }

  public void deleteRow(final Row row) {
    EsriFileGdbJNI.Table_deleteRow(swigCPtr, this, Row.getCPtr(row), row);
  }

  public int DeleteSubtype(final String subtypeName) {
    return EsriFileGdbJNI.Table_DeleteSubtype(swigCPtr, this, subtypeName);
  }

  public int DisableSubtypes() {
    return EsriFileGdbJNI.Table_DisableSubtypes(swigCPtr, this);
  }

  public int EnableSubtypes(
    final String subtypeFieldName,
    final String subtypeDef) {
    return EsriFileGdbJNI.Table_EnableSubtypes(swigCPtr, this,
      subtypeFieldName, subtypeDef);
  }

  @Override
  protected void finalize() {
    delete();
  }

  public void freeWriteLock() {
    EsriFileGdbJNI.Table_freeWriteLock(swigCPtr, this);
  }

  public int getDefaultSubtypeCode() {
    return EsriFileGdbJNI.Table_getDefaultSubtypeCode(swigCPtr, this);
  }

  public String getDefinition() {
    return EsriFileGdbJNI.Table_getDefinition(swigCPtr, this);
  }

  public String getDocumentation() {
    return EsriFileGdbJNI.Table_getDocumentation(swigCPtr, this);
  }

  public int GetExtent(final Envelope extent) {
    return EsriFileGdbJNI.Table_GetExtent(swigCPtr, this,
      Envelope.getCPtr(extent), extent);
  }

  public int GetFieldInformation(final FieldInfo fieldInfo) {
    return EsriFileGdbJNI.Table_GetFieldInformation(swigCPtr, this,
      FieldInfo.getCPtr(fieldInfo), fieldInfo);
  }

  public VectorOfFieldDef getFields() {
    return new VectorOfFieldDef(EsriFileGdbJNI.Table_getFields(swigCPtr, this),
      true);
  }

  public VectorOfString getIndexes() {
    return new VectorOfString(EsriFileGdbJNI.Table_getIndexes(swigCPtr, this),
      true);
  }

  public int getRowCount() {
    return EsriFileGdbJNI.Table_getRowCount(swigCPtr, this);
  }

  public void insertRow(final Row row) {
    EsriFileGdbJNI.Table_insertRow(swigCPtr, this, Row.getCPtr(row), row);
  }

  public boolean isEditable() {
    return EsriFileGdbJNI.Table_isEditable(swigCPtr, this);
  }

  public EnumRows search(
    final String subfields,
    final String whereClause,
    final boolean recycling) {
    final long cPtr = EsriFileGdbJNI.Table_search__SWIG_1(swigCPtr, this,
      subfields, whereClause, recycling);
    return (cPtr == 0) ? null : new EnumRows(cPtr, true);
  }

  public EnumRows search(
    final String subfields,
    final String whereClause,
    final Envelope envelope,
    final boolean recycling) {
    final long cPtr = EsriFileGdbJNI.Table_search__SWIG_0(swigCPtr, this,
      subfields, whereClause, Envelope.getCPtr(envelope), envelope, recycling);
    return (cPtr == 0) ? null : new EnumRows(cPtr, true);
  }

  public int SetDefaultSubtypeCode(final int defaultCode) {
    return EsriFileGdbJNI.Table_SetDefaultSubtypeCode(swigCPtr, this,
      defaultCode);
  }

  public int SetDocumentation(final String documentation) {
    return EsriFileGdbJNI.Table_SetDocumentation(swigCPtr, this, documentation);
  }

  public void setLoadOnlyMode(final boolean loadOnly) {
    EsriFileGdbJNI.Table_setLoadOnlyMode(swigCPtr, this, loadOnly);
  }

  public void setWriteLock() {
    EsriFileGdbJNI.Table_setWriteLock(swigCPtr, this);
  }

  public void updateRow(final Row row) {
    EsriFileGdbJNI.Table_updateRow(swigCPtr, this, Row.getCPtr(row), row);
  }

}
