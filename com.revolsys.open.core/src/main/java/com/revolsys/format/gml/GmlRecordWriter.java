package com.revolsys.format.gml;

import java.io.Writer;

import javax.xml.namespace.QName;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordWriter;
import com.revolsys.data.record.property.RecordProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.format.gml.type.GmlFieldType;
import com.revolsys.format.gml.type.GmlFieldTypeRegistry;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Path;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class GmlRecordWriter extends AbstractWriter<Record>implements GmlConstants, RecordWriter {
  public static final void srsName(final XmlWriter out, final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final int csId = coordinateSystem.getId();
    out.attribute(SRS_NAME, "EPSG:" + csId);
  }

  private final GmlFieldTypeRegistry fieldTypes = GmlFieldTypeRegistry.INSTANCE;

  private GeometryFactory geometryFactory;

  private final String namespaceUri;

  private boolean opened;

  private final XmlWriter out;

  private QName qualifiedName;

  public GmlRecordWriter(final RecordDefinition metaData, final Writer out) {
    this.out = new XmlWriter(out);
    this.qualifiedName = metaData.getProperty(RecordProperties.QUALIFIED_NAME);
    if (this.qualifiedName == null) {
      this.qualifiedName = new QName(metaData.getName());
    }
    this.namespaceUri = this.qualifiedName.getNamespaceURI();
    this.out.setPrefix(this.qualifiedName);
  }

  private void box(final GeometryFactory geometryFactory, final BoundingBox areaBoundingBox) {
    this.out.startTag(BOX);
    srsName(this.out, geometryFactory);
    this.out.startTag(COORDINATES);
    this.out.text(areaBoundingBox.getMinX());
    this.out.text(",");
    this.out.text(areaBoundingBox.getMinY());
    this.out.text(" ");
    this.out.text(areaBoundingBox.getMaxX());
    this.out.text(",");
    this.out.text(areaBoundingBox.getMaxY());
    this.out.endTag(COORDINATES);
    this.out.endTag(BOX);
  }

  @Override
  public void close() {
    if (!this.opened) {
      writeHeader();
    }

    writeFooter();
    this.out.close();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (name.equals(IoConstants.GEOMETRY_FACTORY)) {
      this.geometryFactory = (GeometryFactory)value;
    }
    super.setProperty(name, value);
  }

  @Override
  public void write(final Record object) {
    if (!this.opened) {
      writeHeader();
    }
    this.out.startTag(FEATURE_MEMBER);
    final RecordDefinition metaData = object.getRecordDefinition();
    QName qualifiedName = metaData.getProperty(RecordProperties.QUALIFIED_NAME);
    if (qualifiedName == null) {
      final String typeName = metaData.getPath();
      final String path = Path.getPath(typeName);
      final String name = Path.getName(typeName);
      qualifiedName = new QName(path, name);
      metaData.setProperty(RecordProperties.QUALIFIED_NAME, qualifiedName);
    }
    this.out.startTag(qualifiedName);

    for (final FieldDefinition attribute : metaData.getFields()) {
      final String attributeName = attribute.getName();
      this.out.startTag(this.namespaceUri, attributeName);
      final Object value = object.getValue(attributeName);
      final DataType type = attribute.getType();
      final GmlFieldType fieldType = this.fieldTypes.getFieldType(type);
      if (fieldType != null) {
        fieldType.writeValue(this.out, value);
      }
      this.out.endTag();
    }

    this.out.endTag(qualifiedName);
    this.out.endTag(FEATURE_MEMBER);
  }

  public void writeFooter() {
    this.out.endTag(FEATURE_COLLECTION);
    this.out.endDocument();
  }

  private void writeHeader() {
    this.opened = true;
    this.out.startDocument("UTF-8", "1.0");

    this.out.startTag(FEATURE_COLLECTION);
    if (this.geometryFactory != null) {
      this.out.startTag(BOUNDED_BY);
      box(this.geometryFactory, this.geometryFactory.getCoordinateSystem().getAreaBoundingBox());
      this.out.endTag(BOUNDED_BY);
    }
  }

}
