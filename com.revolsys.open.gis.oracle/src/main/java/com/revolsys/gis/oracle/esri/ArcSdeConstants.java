package com.revolsys.gis.oracle.esri;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import oracle.sql.SQLName;

import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.data.io.RecordStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.oracle.io.OracleDataObjectStore;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class ArcSdeConstants {

  public static final int COLLECTION = 6;

  public static final int CURVE = 2;

  public static final Map<Integer, DataType> DATA_TYPE_MAP = new HashMap<Integer, DataType>();

  public static final String ESRI_SRID_PROPERTY = "esriSrid";

  public static final int GEOMETRY = 0;

  public static final Map<Class<?>, Integer> GEOMETRY_CLASS_ST_TYPE = new HashMap<Class<?>, Integer>();

  public static String GEOMETRY_COLUMN_TYPE = "geometryColumnType";

  public static final int LINESTRING = 3;

  public static final int MULT_SURFACE = 10;

  public static final int MULTI_CURVE = 8;

  public static final int MULTI_LINESTRING = 9;

  public static final int MULTI_POINT = 7;

  public static final int MULTI_POLYGON = 11;

  public static final int POINT = 1;

  public static final int POLYGON = 5;

  public static final String REGISTRATION_ID = "REGISTRATION_ID";

  public static final String ROWID_COLUMN = "ROWID_COLUMN";

  public static final String SDEBINARY = "SDEBINARY";

  public static final String SPATIAL_REFERENCE = "spatialReference";

  public static final int ST_GEOMETRY_LINESTRING = 4;

  public static final int ST_GEOMETRY_MULTI_LINESTRING = 260;

  public static final int ST_GEOMETRY_MULTI_POINT = 257;

  public static final int ST_GEOMETRY_MULTI_POLYGON = 264;

  public static final int ST_GEOMETRY_POINT = 1;

  public static final int ST_GEOMETRY_POLYGON = 8;

  public static final SQLName ST_GEOMETRY_SQL_NAME;

  public static final int SURFACE = 4;

  static {
    try {
      ST_GEOMETRY_SQL_NAME = new SQLName("SDE", "ST_GEOMETRY", null);
    } catch (final SQLException e) {
      throw new RuntimeException(
        "Unable to create SQL name for SDE.ST_GEOMETRY");
    }
    DATA_TYPE_MAP.put(POINT, DataTypes.POINT);
    DATA_TYPE_MAP.put(LINESTRING, DataTypes.LINE_STRING);
    DATA_TYPE_MAP.put(POLYGON, DataTypes.POLYGON);
    DATA_TYPE_MAP.put(MULTI_POINT, DataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put(MULTI_LINESTRING, DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put(MULTI_POLYGON, DataTypes.MULTI_POLYGON);

    GEOMETRY_CLASS_ST_TYPE.put(Point.class, ST_GEOMETRY_POINT);
    GEOMETRY_CLASS_ST_TYPE.put(MultiPoint.class, ST_GEOMETRY_MULTI_POINT);
    GEOMETRY_CLASS_ST_TYPE.put(LineString.class, ST_GEOMETRY_LINESTRING);
    GEOMETRY_CLASS_ST_TYPE.put(MultiLineString.class,
      ST_GEOMETRY_MULTI_LINESTRING);
    GEOMETRY_CLASS_ST_TYPE.put(Polygon.class, ST_GEOMETRY_POLYGON);
    GEOMETRY_CLASS_ST_TYPE.put(MultiPolygon.class, ST_GEOMETRY_MULTI_POLYGON);
  }

  public static DataType getGeometryDataType(final int geometryType) {
    final DataType dataType = DATA_TYPE_MAP.get(geometryType);
    if (dataType == null) {
      return DataTypes.GEOMETRY;
    } else {
      return dataType;
    }
  }

  public static Integer getStGeometryType(final Geometry geometry) {
    final Class<? extends Geometry> geometryClass = geometry.getClass();
    final Integer type = GEOMETRY_CLASS_ST_TYPE.get(geometryClass);
    if (type == null) {
      throw new IllegalArgumentException("Unsupported geometry type "
        + geometryClass);
    } else {
      return type;
    }
  }

  public static boolean isSdeAvailable(final RecordStore dataStore) {
    if (dataStore instanceof OracleDataObjectStore) {
      final OracleDataObjectStore oracleDataStore = (OracleDataObjectStore)dataStore;
      final Set<String> allSchemaNames = oracleDataStore.getAllSchemaNames();
      return allSchemaNames.contains("SDE");
    }
    return false;
  }

  public static boolean isSdeAvailable(final DataObjectStoreSchema schema) {
    final RecordStore dataStore = schema.getDataStore();

    return isSdeAvailable(dataStore);
  }
}
