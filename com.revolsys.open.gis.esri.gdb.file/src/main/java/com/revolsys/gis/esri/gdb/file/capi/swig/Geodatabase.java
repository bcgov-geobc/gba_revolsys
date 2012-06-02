/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.3
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class Geodatabase {
  public static long getCPtr(final Geodatabase obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  private long swigCPtr;

  protected boolean swigCMemOwn;

  public Geodatabase() {
    this(EsriFileGdbJNI.new_Geodatabase(), true);
  }

  public Geodatabase(final long cPtr, final boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public void alterDomain(final String domainDefinition) {
    EsriFileGdbJNI.Geodatabase_alterDomain(swigCPtr, this, domainDefinition);
  }

  public void closeTable(final Table table) {
    EsriFileGdbJNI.Geodatabase_closeTable(swigCPtr, this, Table.getCPtr(table),
      table);
  }

  public void createDomain(final String domainDefinition) {
    EsriFileGdbJNI.Geodatabase_createDomain(swigCPtr, this, domainDefinition);
  }

  public void createFeatureDataset(final String featureDatasetDef) {
    EsriFileGdbJNI.Geodatabase_createFeatureDataset(swigCPtr, this,
      featureDatasetDef);
  }

  public Table createTable(final String tableDefinition, final String parent) {
    final long cPtr = EsriFileGdbJNI.Geodatabase_createTable(swigCPtr, this,
      tableDefinition, parent);
    return (cPtr == 0) ? null : new Table(cPtr, true);
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_Geodatabase(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public int Delete(final String path, final String datasetType) {
    return EsriFileGdbJNI.Geodatabase_Delete(swigCPtr, this, path, datasetType);
  }

  public void deleteDomain(final String domainName) {
    EsriFileGdbJNI.Geodatabase_deleteDomain(swigCPtr, this, domainName);
  }

  public int ExecuteSQL(
    final String sqlStmt,
    final boolean recycling,
    final EnumRows rows) {
    return EsriFileGdbJNI.Geodatabase_ExecuteSQL(swigCPtr, this, sqlStmt,
      recycling, EnumRows.getCPtr(rows), rows);
  }

  @Override
  protected void finalize() {
    delete();
  }

  public int GetChildDatasetDefinitions(
    final String parentPath,
    final String datasetType,
    final VectorOfString childDatasetDefs) {
    return EsriFileGdbJNI.Geodatabase_GetChildDatasetDefinitions(swigCPtr,
      this, parentPath, datasetType, VectorOfString.getCPtr(childDatasetDefs),
      childDatasetDefs);
  }

  public VectorOfWString getChildDatasets(
    final String parentPath,
    final String datasetType) {
    return new VectorOfWString(EsriFileGdbJNI.Geodatabase_getChildDatasets(
      swigCPtr, this, parentPath, datasetType), true);
  }

  public String getDatasetDefinition(final String path, final String datasetType) {
    return EsriFileGdbJNI.Geodatabase_getDatasetDefinition(swigCPtr, this,
      path, datasetType);
  }

  public String getDatasetDocumentation(
    final String path,
    final String datasetType) {
    return EsriFileGdbJNI.Geodatabase_getDatasetDocumentation(swigCPtr, this,
      path, datasetType);
  }

  public int GetDatasetRelationshipTypes(final VectorOfWString relationshipTypes) {
    return EsriFileGdbJNI.Geodatabase_GetDatasetRelationshipTypes(swigCPtr,
      this, VectorOfWString.getCPtr(relationshipTypes), relationshipTypes);
  }

  public int GetDatasetTypes(final VectorOfWString datasetTypes) {
    return EsriFileGdbJNI.Geodatabase_GetDatasetTypes(swigCPtr, this,
      VectorOfWString.getCPtr(datasetTypes), datasetTypes);
  }

  public String getDomainDefinition(final String domainName) {
    return EsriFileGdbJNI.Geodatabase_getDomainDefinition(swigCPtr, this,
      domainName);
  }

  public VectorOfWString getDomains() {
    return new VectorOfWString(EsriFileGdbJNI.Geodatabase_getDomains(swigCPtr,
      this), true);
  }

  public String getQueryName(final String path) {
    return EsriFileGdbJNI.Geodatabase_getQueryName(swigCPtr, this, path);
  }

  public int GetRelatedDatasetDefinitions(
    final String path,
    final String relType,
    final String datasetType,
    final VectorOfString relatedDatasetDefs) {
    return EsriFileGdbJNI.Geodatabase_GetRelatedDatasetDefinitions(swigCPtr,
      this, path, relType, datasetType,
      VectorOfString.getCPtr(relatedDatasetDefs), relatedDatasetDefs);
  }

  public int GetRelatedDatasets(
    final String path,
    final String relType,
    final String datasetType,
    final VectorOfWString relatedDatasets) {
    return EsriFileGdbJNI.Geodatabase_GetRelatedDatasets(swigCPtr, this, path,
      relType, datasetType, VectorOfWString.getCPtr(relatedDatasets),
      relatedDatasets);
  }

  public String getTableDefinition(final String path) {
    return EsriFileGdbJNI.Geodatabase_getTableDefinition(swigCPtr, this, path);
  }

  public int Move(final String path, final String newParentPath) {
    return EsriFileGdbJNI.Geodatabase_Move(swigCPtr, this, path, newParentPath);
  }

  public Table openTable(final String path) {
    final long cPtr = EsriFileGdbJNI.Geodatabase_openTable(swigCPtr, this, path);
    return (cPtr == 0) ? null : new Table(cPtr, true);
  }

  public int Rename(
    final String path,
    final String datasetType,
    final String newName) {
    return EsriFileGdbJNI.Geodatabase_Rename(swigCPtr, this, path, datasetType,
      newName);
  }

}
