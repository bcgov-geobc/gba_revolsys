/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class EsriFileGdb {
  public static int CreateGeodatabase(String path, Geodatabase geodatabase) {
    return EsriFileGdbJNI.CreateGeodatabase(path, Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int OpenGeodatabase(String path, Geodatabase geodatabase) {
    return EsriFileGdbJNI.OpenGeodatabase(path, Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int CloseGeodatabase(Geodatabase geodatabase) {
    return EsriFileGdbJNI.CloseGeodatabase(Geodatabase.getCPtr(geodatabase), geodatabase);
  }

  public static int DeleteGeodatabase(String path) {
    return EsriFileGdbJNI.DeleteGeodatabase(path);
  }

  public static int GetErrorDescription(int fgdbError, WstringValue errorDescription) {
    return EsriFileGdbJNI.GetErrorDescription(fgdbError, errorDescription);
  }

  public static void GetErrorRecordCount(int[] recordCount) {
    EsriFileGdbJNI.GetErrorRecordCount(recordCount);
  }

  public static int GetErrorRecord(int recordNum, int[] fgdbError, WstringValue errorDescription) {
    return EsriFileGdbJNI.GetErrorRecord(recordNum, fgdbError, errorDescription);
  }

  public static void ClearErrors() {
    EsriFileGdbJNI.ClearErrors();
  }

  public static boolean FindSpatialReferenceBySRID(int auth_srid, SpatialReferenceInfo spatialRef) {
    return EsriFileGdbJNI.FindSpatialReferenceBySRID(auth_srid, SpatialReferenceInfo.getCPtr(spatialRef), spatialRef);
  }

  public static boolean FindSpatialReferenceByName(String srname, SpatialReferenceInfo spatialRef) {
    return EsriFileGdbJNI.FindSpatialReferenceByName(srname, SpatialReferenceInfo.getCPtr(spatialRef), spatialRef);
  }

  public static String getErrorDescription(int hr) {
    return EsriFileGdbJNI.getErrorDescription(hr);
  }

  public static SWIGTYPE_p_std__vectorT_std__wstring_t getVectorWstring(SWIGTYPE_p_std__vectorT_std__wstring_t vector) {
    return new SWIGTYPE_p_std__vectorT_std__wstring_t(EsriFileGdbJNI.getVectorWstring(SWIGTYPE_p_std__vectorT_std__wstring_t.getCPtr(vector)), true);
  }

  public static SWIGTYPE_p_std__vectorT_std__string_t getVectorString(SWIGTYPE_p_std__vectorT_std__string_t vector) {
    return new SWIGTYPE_p_std__vectorT_std__string_t(EsriFileGdbJNI.getVectorString(SWIGTYPE_p_std__vectorT_std__string_t.getCPtr(vector)), true);
  }

}
