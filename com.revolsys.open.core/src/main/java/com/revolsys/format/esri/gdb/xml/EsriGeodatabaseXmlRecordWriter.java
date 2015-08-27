package com.revolsys.format.esri.gdb.xml;

import java.io.Writer;
import java.sql.Timestamp;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordWriter;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;
import com.revolsys.format.xml.XmlConstants;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.format.xml.XsiConstants;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Path;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.DateUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class EsriGeodatabaseXmlRecordWriter extends AbstractWriter<Record>
  implements RecordWriter, EsriGeodatabaseXmlConstants {
  private static final Logger LOG = LoggerFactory.getLogger(EsriGeodatabaseXmlRecordWriter.class);

  private int datasetId = 1;

  private String datasetType;

  private final EsriGeodatabaseXmlFieldTypeRegistry fieldTypes = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  private String geometryType;

  private final RecordDefinition metaData;

  private int objectId = 1;

  private boolean opened;

  private final XmlWriter out;

  public EsriGeodatabaseXmlRecordWriter(final RecordDefinition metaData, final Writer out) {
    this.metaData = metaData;
    this.out = new XmlWriter(out);
  }

  @Override
  public void close() {
    if (!this.opened) {
      writeHeader(null);
    }

    writeFooter();
    this.out.close();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void write(final Record object) {
    if (!this.opened) {
      writeHeader(object.getGeometry());
      writeWorkspaceDataHeader();
    }
    this.out.startTag(RECORD);
    this.out.attribute(XsiConstants.TYPE, RECORD_TYPE);

    this.out.startTag(VALUES);
    this.out.attribute(XsiConstants.TYPE, VALUES_TYPE);

    for (final FieldDefinition attribute : this.metaData.getFields()) {
      final String attributeName = attribute.getName();
      final Object value = object.getValue(attributeName);
      final DataType type = attribute.getType();
      final EsriGeodatabaseXmlFieldType fieldType = this.fieldTypes.getFieldType(type);
      if (fieldType != null) {
        fieldType.writeValue(this.out, value);
      }
    }
    if (this.metaData.getField("OBJECTID") == null) {
      final EsriGeodatabaseXmlFieldType fieldType = this.fieldTypes.getFieldType(DataTypes.INTEGER);
      fieldType.writeValue(this.out, this.objectId++);
    }

    this.out.endTag(VALUES);

    this.out.endTag(RECORD);
  }

  private void writeDataElement(final RecordDefinition metaData, final Geometry geometry) {
    final String dataElementType;
    final FieldDefinition geometryAttribute = metaData.getGeometryField();
    boolean hasGeometry = false;
    DataType geometryDataType = null;
    if (geometryAttribute != null) {
      geometryDataType = geometryAttribute.getType();
      if (this.fieldTypes.getFieldType(geometryDataType) != null) {
        hasGeometry = true;

        if (geometryDataType.equals(DataTypes.POINT)) {
          this.geometryType = GEOMETRY_TYPE_POINT;
        } else if (geometryDataType.equals(DataTypes.MULTI_POINT)) {
          this.geometryType = GEOMETRY_TYPE_MULTI_POINT;
        } else if (geometryDataType.equals(DataTypes.LINE_STRING)) {
          this.geometryType = GEOMETRY_TYPE_POLYLINE;
        } else if (geometryDataType.equals(DataTypes.MULTI_LINE_STRING)) {
          this.geometryType = GEOMETRY_TYPE_POLYLINE;
        } else if (geometryDataType.equals(DataTypes.POLYGON)) {
          this.geometryType = GEOMETRY_TYPE_POLYGON;
        } else {
          if (geometry instanceof Point) {
            this.geometryType = GEOMETRY_TYPE_POINT;
          } else if (geometry instanceof MultiPoint) {
            this.geometryType = GEOMETRY_TYPE_MULTI_POINT;
          } else if (geometry instanceof LineString) {
            this.geometryType = GEOMETRY_TYPE_POLYLINE;
          } else if (geometry instanceof MultiLineString) {
            this.geometryType = GEOMETRY_TYPE_POLYLINE;
          } else if (geometry instanceof Polygon) {
            this.geometryType = GEOMETRY_TYPE_POLYGON;
          } else {
            hasGeometry = false;
          }
        }
      }
    }

    if (hasGeometry) {
      dataElementType = DATA_ELEMENT_FEATURE_CLASS;
      this.datasetType = DATASET_TYPE_FEATURE_CLASS;
    } else {
      dataElementType = DATA_ELEMENT_TABLE;
      this.datasetType = DATASET_TYPE_TABLE;
    }

    this.out.startTag(DATA_ELEMENT);
    this.out.attribute(XsiConstants.TYPE, dataElementType);

    final String path = metaData.getPath();
    final String localName = Path.getName(path);
    this.out.element(CATALOG_PATH, "/FC=" + localName);
    this.out.element(NAME, localName);
    this.out.element(METADATA_RETRIEVED, true);

    this.out.startTag(METADATA);
    this.out.attribute(XsiConstants.TYPE, XML_PROPERTY_SET_TYPE);
    this.out.startTag(XML_DOC);
    this.out.text("<?xml version=\"1.0\"?>");
    this.out.text("<metadata xml:lang=\"en\">");
    this.out.text("<Esri>");
    this.out.text("<MetaID>{");
    this.out.text(UUID.randomUUID().toString().toUpperCase());
    this.out.text("}</MetaID>");
    this.out.text("<CreaDate>");
    final Timestamp date = new Timestamp(System.currentTimeMillis());
    this.out.text(DateUtil.format("yyyyMMdd", date));
    this.out.text("</CreaDate>");
    this.out.text("<CreaTime>");
    this.out.text(DateUtil.format("HHmmssSS", date));
    this.out.text("</CreaTime>");
    this.out.text("<SyncOnce>TRUE</SyncOnce>");
    this.out.text("</Esri>");
    this.out.text("</metadata>");
    this.out.endTag(XML_DOC);
    this.out.endTag(METADATA);

    this.out.element(DATASET_TYPE, this.datasetType);
    this.out.element(DSID, this.datasetId++);
    this.out.element(VERSIONED, false);
    this.out.element(CAN_VERSION, false);
    this.out.element(HAS_OID, true);
    this.out.element(OBJECT_ID_FIELD_NAME, "OBJECTID");
    writeFields(metaData);

    this.out.element(CLSID, "{52353152-891A-11D0-BEC6-00805F7C4268}");
    this.out.emptyTag(EXTCLSID);
    this.out.startTag(RELATIONSHIP_CLASS_NAMES);
    this.out.attribute(XsiConstants.TYPE, NAMES_TYPE);
    this.out.endTag(RELATIONSHIP_CLASS_NAMES);
    this.out.element(ALIAS_NAME, localName);
    this.out.emptyTag(MODEL_NAME);
    this.out.element(HAS_GLOBAL_ID, false);
    this.out.emptyTag(GLOBAL_ID_FIELD_NAME);
    this.out.emptyTag(RASTER_FIELD_NAME);
    this.out.startTag(EXTENSION_PROPERTIES);
    this.out.attribute(XsiConstants.TYPE, PROPERTY_SET_TYPE);
    this.out.startTag(PROPERTY_ARRAY);
    this.out.attribute(XsiConstants.TYPE, PROPERTY_ARRAY_TYPE);
    this.out.endTag(PROPERTY_ARRAY);
    this.out.endTag(EXTENSION_PROPERTIES);
    this.out.startTag(CONTROLLER_MEMBERSHIPS);
    this.out.attribute(XsiConstants.TYPE, CONTROLLER_MEMBERSHIPS_TYPE);
    this.out.endTag(CONTROLLER_MEMBERSHIPS);
    if (hasGeometry) {
      this.out.element(FEATURE_TYPE, FEATURE_TYPE_SIMPLE);
      this.out.element(SHAPE_TYPE, this.geometryType);
      this.out.element(SHAPE_FIELD_NAME, geometryAttribute.getName());
      final GeometryFactory geometryFactory = geometryAttribute
        .getProperty(FieldProperties.GEOMETRY_FACTORY);
      this.out.element(HAS_M, false);
      this.out.element(HAS_Z, geometryFactory.hasZ());
      this.out.element(HAS_SPATIAL_INDEX, false);
      this.out.emptyTag(AREA_FIELD_NAME);
      this.out.emptyTag(LENGTH_FIELD_NAME);

      writeExtent(geometryFactory);
      writeSpatialReference(geometryFactory);
    }

    this.out.endTag(DATA_ELEMENT);
  }

  public void writeExtent(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final BoundingBox boundingBox = coordinateSystem.getAreaBoundingBox();
        this.out.startTag(EXTENT);
        this.out.attribute(XsiConstants.TYPE, ENVELOPE_N_TYPE);
        this.out.element(X_MIN, boundingBox.getMinX());
        this.out.element(Y_MIN, boundingBox.getMinY());
        this.out.element(X_MAX, boundingBox.getMaxX());
        this.out.element(Y_MAX, boundingBox.getMaxY());
        this.out.element(Z_MIN, boundingBox.getMinZ());
        this.out.element(Z_MAX, boundingBox.getMaxZ());
        writeSpatialReference(geometryFactory);
        this.out.endTag(EXTENT);
      }
    }
  }

  private void writeField(final FieldDefinition attribute) {
    final String fieldName = attribute.getName();
    if (fieldName.equals("OBJECTID")) {
      writeOidField();
    } else {
      final DataType dataType = attribute.getType();
      final EsriGeodatabaseXmlFieldType fieldType = this.fieldTypes.getFieldType(dataType);
      if (fieldType == null) {
        LOG.error("Data type not supported " + dataType);
      } else {
        this.out.startTag(FIELD);
        this.out.attribute(XsiConstants.TYPE, FIELD_TYPE);
        this.out.element(NAME, fieldName);
        this.out.element(TYPE, fieldType.getEsriFieldType());
        this.out.element(IS_NULLABLE, !attribute.isRequired());
        int length = fieldType.getFixedLength();
        if (length < 0) {
          length = attribute.getLength();
        }
        this.out.element(LENGTH, length);
        final int precision;
        if (fieldType.isUsePrecision()) {
          precision = attribute.getLength();
        } else {
          precision = 0;
        }
        this.out.element(PRECISION, precision);
        this.out.element(SCALE, attribute.getScale());

        final GeometryFactory geometryFactory = attribute
          .getProperty(FieldProperties.GEOMETRY_FACTORY);
        if (geometryFactory != null) {
          this.out.startTag(GEOMETRY_DEF);
          this.out.attribute(XsiConstants.TYPE, GEOMETRY_DEF_TYPE);

          this.out.element(AVG_NUM_POINTS, 0);

          this.out.element(GEOMETRY_TYPE, this.geometryType);
          this.out.element(HAS_M, false);
          this.out.element(HAS_Z, geometryFactory.hasZ());

          writeSpatialReference(geometryFactory);

          this.out.endTag();
        }
        this.out.endTag(FIELD);
      }
    }
  }

  private void writeFields(final RecordDefinition metaData) {
    this.out.startTag(FIELDS);
    this.out.attribute(XsiConstants.TYPE, FIELDS_TYPE);

    this.out.startTag(FIELD_ARRAY);
    this.out.attribute(XsiConstants.TYPE, FIELD_ARRAY_TYPE);

    for (final FieldDefinition attribute : metaData.getFields()) {
      writeField(attribute);
    }
    if (metaData.getField("OBJECTID") == null) {
      writeOidField();
    }
    this.out.endTag(FIELD_ARRAY);

    this.out.endTag(FIELDS);
  }

  public void writeFooter() {
    this.out.endDocument();
  }

  private void writeHeader(final Geometry geometry) {
    this.opened = true;
    this.out.startDocument("UTF-8", "1.0");
    this.out.startTag(WORKSPACE);
    this.out.setPrefix(XsiConstants.PREFIX, XsiConstants.NAMESPACE_URI);
    this.out.setPrefix(XmlConstants.XML_SCHEMA_NAMESPACE_PREFIX,
      XmlConstants.XML_SCHEMA_NAMESPACE_URI);

    this.out.startTag(WORKSPACE_DEFINITION);
    this.out.attribute(XsiConstants.TYPE, WORKSPACE_DEFINITION_TYPE);

    this.out.element(WORKSPACE_TYPE, "esriLocalDatabaseWorkspace");
    this.out.element(VERSION, "");

    this.out.startTag(DOMAINS);
    this.out.attribute(XsiConstants.TYPE, DOMAINS_TYPE);
    this.out.endTag(DOMAINS);

    this.out.startTag(DATASET_DEFINITIONS);
    this.out.attribute(XsiConstants.TYPE, DATASET_DEFINITIONS_TYPE);
    writeDataElement(this.metaData, geometry);
    this.out.endTag(DATASET_DEFINITIONS);

    this.out.endTag(WORKSPACE_DEFINITION);
  }

  private void writeOidField() {
    this.out.startTag(FIELD);
    this.out.attribute(XsiConstants.TYPE, FIELD_TYPE);
    this.out.element(NAME, "OBJECTID");
    this.out.element(TYPE, FIELD_TYPE_OBJECT_ID);
    this.out.element(IS_NULLABLE, false);
    this.out.element(LENGTH, 4);
    this.out.element(PRECISION, 10);
    this.out.element(SCALE, 0);

    this.out.element(REQUIRED, true);
    this.out.element(EDIATBLE, false);
    this.out.endTag(FIELD);
  }

  public void writeSpatialReference(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems
          .getCoordinateSystem(coordinateSystem);
        if (esriCoordinateSystem != null) {
          this.out.startTag(SPATIAL_REFERENCE);
          if (esriCoordinateSystem instanceof ProjectedCoordinateSystem) {
            this.out.attribute(XsiConstants.TYPE, PROJECTED_COORDINATE_SYSTEM_TYPE);
          } else {
            this.out.attribute(XsiConstants.TYPE, GEOGRAPHIC_COORDINATE_SYSTEM_TYPE);
          }
          this.out.element(WKT, EsriCsWktWriter.toWkt(esriCoordinateSystem));
          this.out.element(X_ORIGIN, 0);
          this.out.element(Y_ORIGIN, 0);
          final double scaleXy = geometryFactory.getScaleXY();
          this.out.element(XY_SCALE, (int)scaleXy);
          this.out.element(Z_ORIGIN, 0);
          final double scaleZ = geometryFactory.getScaleZ();
          this.out.element(Z_SCALE, (int)scaleZ);
          this.out.element(M_ORIGIN, 0);
          this.out.element(M_SCALE, 1);
          this.out.element(XY_TOLERANCE, 1.0 / scaleXy * 2.0);
          this.out.element(Z_TOLERANCE, 1.0 / scaleZ * 2.0);
          this.out.element(M_TOLERANCE, 1);
          this.out.element(HIGH_PRECISION, true);
          this.out.element(WKID, coordinateSystem.getId());
          this.out.endTag(SPATIAL_REFERENCE);
        }
      }
    }
  }

  public void writeWorkspaceDataHeader() {
    this.out.startTag(WORKSPACE_DATA);
    this.out.attribute(XsiConstants.TYPE, WORKSPACE_DATA_TYPE);

    this.out.startTag(DATASET_DATA);
    this.out.attribute(XsiConstants.TYPE, DATASET_DATA_TABLE_DATA);

    this.out.element(DATASET_NAME, this.metaData.getName());
    this.out.element(DATASET_TYPE, this.datasetType);

    this.out.startTag(DATA);
    this.out.attribute(XsiConstants.TYPE, DATA_RECORD_SET);

    writeFields(this.metaData);

    this.out.startTag(RECORDS);
    this.out.attribute(XsiConstants.TYPE, RECORDS_TYPE);

  }

}
