package com.revolsys.gis.oracle.esri;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.oracle.io.OracleDataObjectStore;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ArcSdeBinaryGeometryDataStoreUtil {

  private final RecordStoreIteratorFactory iteratorFactory = new RecordStoreIteratorFactory(this,
      "createIterator");

  private Map<String, Object> connectionProperties = new HashMap<String, Object>();

  private JdbcRecordStore dataStore;

  public ArcSdeBinaryGeometryDataStoreUtil() {
  }

  public ArcSdeBinaryGeometryDataStoreUtil(final RecordStore dataStore,
    final Map<String, Object> connectionProperties) {
    this.dataStore = (JdbcRecordStore)dataStore;
    this.connectionProperties = connectionProperties;
  }

  public SeConnection close(final SeConnection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (final SeException e) {
      }
    }
    return null;
  }

  public SeQuery close(final SeQuery query) {
    if (query != null) {
      try {
        query.close();
      } catch (final SeException e) {
      }
    }
    return null;
  }

  public void createGeometryColumn(final AbstractJdbcRecordStore dataStore,
    final RecordStoreSchema schema, final RecordDefinition metaData, final String typePath,
    final String dbName, final String columnName, final Map<String, Object> columnProperties) {
    final FieldDefinition attribute = metaData.getField(columnName);

    DataType dataType = JdbcFieldAdder.getColumnProperty(schema, typePath, columnName,
      JdbcFieldAdder.GEOMETRY_TYPE);
    if (dataType == null) {
      dataType = DataTypes.GEOMETRY;
    }

    GeometryFactory geometryFactory = JdbcFieldAdder.getColumnProperty(schema, typePath,
      columnName, JdbcFieldAdder.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = schema.getGeometryFactory();
    }

    final ArcSdeBinaryGeometryAttribute sdeAttribute = new ArcSdeBinaryGeometryAttribute(this,
      dbName, columnName, dataType, attribute.isRequired(), "The GEOMETRY reference",
      attribute.getProperties(), geometryFactory);
    ((RecordDefinitionImpl)metaData).replaceField(attribute, sdeAttribute);
    sdeAttribute.setRecordDefinition(metaData);

    metaData.setProperty("dataStoreIteratorFactory", this.iteratorFactory);

    ((RecordDefinitionImpl)metaData).setGeometryFieldName(columnName);
  }

  public AbstractIterator<Record> createIterator(final OracleDataObjectStore dataStore,
    final Query query, final Map<String, Object> properties) {
    final BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox == null) {
      return null;
    } else {
      return new ArcSdeBinaryGeometryQueryIterator(this, dataStore, query, properties);
    }
  }

  public SeConnection createSeConnection() {
    final String server = (String)this.connectionProperties.get("sdeServer");

    if (!Property.hasValue(server)) {
      throw new IllegalArgumentException(
          "The connection properties must include a sdeServer to support ESRI ArcSDE SDEBINARY columns");
    }
    String instance = (String)this.connectionProperties.get("sdeInstance");
    if (!Property.hasValue(instance)) {
      instance = "5151";
    }
    String database = (String)this.connectionProperties.get("sdeDatabase");
    if (!Property.hasValue(database)) {
      database = "none";
    }
    final String username = (String)this.connectionProperties.get("username");
    String password = (String)this.connectionProperties.get("password");
    password = PasswordUtil.decrypt(password);

    try {
      return new SeConnection(server, instance, database, username, password);
    } catch (final SeException e) {
      throw new RuntimeException("Unabel to create connection", e);
    }
  }

  public CoordinatesList getCoordinates(final SeShape shape, final double[][][] allCoordinates,
    final int partIndex, final int ringIndex, final int numAxis) {
    try {
      final int numCoords = shape.getNumPoints(partIndex + 1, ringIndex + 1);
      final CoordinatesList coordinates = new DoubleCoordinatesList(numCoords, numAxis);
      for (int coordinateIndex = 0; coordinateIndex < numCoords; coordinateIndex++) {

        final double x = allCoordinates[partIndex][ringIndex][coordinateIndex * numAxis];
        final double y = allCoordinates[partIndex][ringIndex][coordinateIndex * numAxis + 1];
        coordinates.setX(coordinateIndex, x);
        coordinates.setY(coordinateIndex, y);
      }
      return coordinates;
    } catch (final SeException e) {
      throw new RuntimeException("Unable to get coordinates", e);
    }
  }

  public String getTableName(final RecordDefinition metaData) {
    final String typePath = metaData.getPath();
    return this.dataStore.getDatabaseQualifiedTableName(typePath);
  }

  public void setValueFromRow(final Record object, final SeRow row, final int columnIndex) {
    if (object != null && row != null) {
      try {
        final SeColumnDefinition columnDefinition = row.getColumnDef(columnIndex);
        final int type = columnDefinition.getType();
        if (row.getIndicator(columnIndex) != SeRow.SE_IS_NULL_VALUE) {

          final String name = columnDefinition.getName();
          Object value = null;
          switch (type) {
            case SeColumnDefinition.TYPE_INT16:
              value = row.getShort(columnIndex);
              break;

            case SeColumnDefinition.TYPE_INT32:
              value = row.getInteger(columnIndex);
              break;

            case SeColumnDefinition.TYPE_INT64:
              value = row.getLong(columnIndex);
              break;

            case SeColumnDefinition.TYPE_FLOAT32:
              value = row.getFloat(columnIndex);
              break;

            case SeColumnDefinition.TYPE_FLOAT64:
              value = row.getDouble(columnIndex);
              break;

            case SeColumnDefinition.TYPE_STRING:
              value = row.getString(columnIndex);
              break;

            case SeColumnDefinition.TYPE_NSTRING:
              value = row.getNString(columnIndex);
              break;

            case SeColumnDefinition.TYPE_CLOB:
              final ByteArrayInputStream clob = row.getClob(columnIndex);
              value = FileUtil.getString(clob);
              break;
            case SeColumnDefinition.TYPE_NCLOB:
              final ByteArrayInputStream nClob = row.getNClob(columnIndex);
              value = FileUtil.getString(nClob);
              break;

            case SeColumnDefinition.TYPE_XML:
              value = row.getXml(columnIndex).getText();
              break;

            case SeColumnDefinition.TYPE_UUID:
              value = row.getUuid(columnIndex);
              break;

            case SeColumnDefinition.TYPE_DATE:
              value = row.getTime(columnIndex);
              break;

            case SeColumnDefinition.TYPE_SHAPE:
              final SeShape shape = row.getShape(columnIndex);
              value = toGeometry(shape);
              break;

            default:
              LoggerFactory.getLogger(ArcSdeBinaryGeometryDataStoreUtil.class).error(
                "Unsupported column type: " + object.getRecordDefinition() + "." + name);
              break;
          }
          object.setValue(name, value);
        }
      } catch (final SeException e) {
        throw new RuntimeException("Unable to get value " + columnIndex + " from result set", e);
      }
    }
  }

  public Geometry toGeometry(final SeShape shape) {
    try {
      final int type = shape.getType();
      final SeCoordinateReference coordRef = shape.getCoordRef();
      final int srid = (int)coordRef.getSrid().longValue();
      final double scaleXy = coordRef.getXYUnits();
      final double scaleZ = coordRef.getZUnits();
      int numAxis = 2;
      if (shape.is3D()) {
        numAxis = 3;
      }
      if (shape.isMeasured()) {
        numAxis = 4;
      }
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(srid, numAxis, scaleXy,
        scaleZ);

      final int numParts = shape.getNumParts();
      final double[][][] allCoordinates = shape.getAllCoords();
      switch (type) {

        case SeShape.TYPE_NIL:
          return geometryFactory.createEmptyGeometry();
        case SeShape.TYPE_POINT:
        case SeShape.TYPE_MULTI_POINT:
          final List<Point> points = new ArrayList<Point>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final CoordinatesList coordinates = getCoordinates(shape, allCoordinates, partIndex,
                ringIndex, numAxis);
              final Point point = geometryFactory.createPoint(coordinates);
              if (!point.isEmpty()) {
                points.add(point);
              }
            }
          }
          if (points.size() == 1) {
            return points.get(0);
          } else {
            return geometryFactory.createMultiPoint(points);
          }
        case SeShape.TYPE_MULTI_LINE:
        case SeShape.TYPE_LINE:
          final List<LineString> lines = new ArrayList<LineString>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final CoordinatesList coordinates = getCoordinates(shape, allCoordinates, partIndex,
                ringIndex, numAxis);
              final LineString line = geometryFactory.createLineString(coordinates);
              if (!line.isEmpty()) {
                lines.add(line);
              }
            }
          }
          if (lines.size() == 1) {
            return lines.get(0);
          } else {
            return geometryFactory.createMultiLineString(lines);
          }
        case SeShape.TYPE_POLYGON:
        case SeShape.TYPE_MULTI_POLYGON:
          final List<Polygon> polygons = new ArrayList<Polygon>();
          for (int partIndex = 0; partIndex < numParts; partIndex++) {
            final int numRings = shape.getNumSubParts(partIndex + 1);
            final List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
            for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
              final CoordinatesList coordinates = getCoordinates(shape, allCoordinates, partIndex,
                ringIndex, numAxis);
              rings.add(coordinates);
            }
            if (!rings.isEmpty()) {
              final Polygon polygon = geometryFactory.createPolygon(rings);
              polygons.add(polygon);
            }
          }
          if (polygons.size() == 1) {
            return polygons.get(0);
          } else {
            return geometryFactory.createMultiPolygon(polygons);
          }

        default:
          throw new IllegalArgumentException("Shape not supported:" + shape.asText(1000));
      }

    } catch (final SeException e) {
      throw new RuntimeException("Unable to read shape", e);
    }
  }
}
