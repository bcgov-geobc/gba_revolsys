package com.revolsys.io.gml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GmlGeometryIterator extends AbstractIterator<Geometry> implements GmlConstants {

  private GeometryFactory geometryFactory;

  private XMLStreamReader in;

  public GmlGeometryIterator(final Resource resource) {
    try {
      this.in = StaxUtils.createXmlReader(resource);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Unable to open resource " + resource);
    }
  }

  @Override
  protected void doClose() {
    StaxUtils.closeSilent(this.in);
    this.geometryFactory = null;
    this.in = null;
  }

  @Override
  protected void doInit() {
    this.geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (this.geometryFactory == null) {
      this.geometryFactory = GeometryFactory.getFactory();
    }
  }

  private GeometryFactory getGeometryFactory(final GeometryFactory geometryFactory) {
    final String srsName = this.in.getAttributeValue(SRS_NAME.getNamespaceURI(),
      SRS_NAME.getLocalPart());
    if (srsName == null) {
      return geometryFactory;
    } else {
      if (srsName.startsWith("urn:ogc:def:crs:EPSG:6.6:")) {
        final int srid = Integer.parseInt(srsName.substring("urn:ogc:def:crs:EPSG:6.6:".length()));
        final GeometryFactory factory = GeometryFactory.floating3(srid);
        return factory;
      } else if (srsName.startsWith("EPSG:")) {
        final int srid = Integer.parseInt(srsName.substring("EPSG:".length()));
        final GeometryFactory factory = GeometryFactory.floating3(srid);
        return factory;
      } else {
        return geometryFactory;
      }
    }
  }

  @Override
  protected Geometry getNext() {
    try {
      while (StaxUtils.skipToStartElements(this.in, ENVELOPE_AND_GEOMETRY_TYPE_NAMES)) {
        if (this.in.getName().equals(ENVELOPE)) {
          this.geometryFactory = getGeometryFactory(this.geometryFactory);
          StaxUtils.skipToEndElement(this.in, ENVELOPE);
        } else {
          return readGeometry(this.geometryFactory);
        }
      }
      throw new NoSuchElementException();
    } catch (final XMLStreamException e) {
      throw new RuntimeException("Error reading next geometry", e);
    }

  }

  private CoordinatesList readCoordinates() throws XMLStreamException {
    String decimal = this.in.getAttributeValue(null, "decimal");
    if (decimal == null) {
      decimal = ".";
    }
    String coordSeperator = this.in.getAttributeValue(null, "coordSeperator");
    if (coordSeperator == null) {
      coordSeperator = ",";
    }
    String toupleSeperator = this.in.getAttributeValue(null, "toupleSeperator");
    if (toupleSeperator == null) {
      toupleSeperator = " ";
    }
    final String value = this.in.getElementText();

    final CoordinatesList points = CoordinatesListUtil.parse(value, decimal, coordSeperator,
      toupleSeperator);
    StaxUtils.skipToEndElement(this.in);
    return points;
  }

  private Geometry readGeometry(final GeometryFactory geometryFactory) throws XMLStreamException {
    final QName typeName = this.in.getName();
    if (typeName.equals(POINT)) {
      return readPoint(geometryFactory);
    } else if (typeName.equals(LINE_STRING)) {
      return readLineString(geometryFactory);
    } else if (typeName.equals(POLYGON)) {
      return readPolygon(geometryFactory);
    } else if (typeName.equals(MULTI_POINT)) {
      return readMultiPoint(geometryFactory);
    } else if (typeName.equals(MULTI_LINE_STRING)) {
      return readMultiLineString(geometryFactory);
    } else if (typeName.equals(MULTI_POLYGON)) {
      return readMultiPolygon(geometryFactory);
    } else if (typeName.equals(MULTI_GEOMETRY)) {
      return readMultiGeometry(geometryFactory);
    } else {
      throw new IllegalStateException("Unexpected geometry type " + typeName);
    }
  }

  private LinearRing readLinearRing(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(this.in, POS_LIST, COORDINATES)) {
      final QName elementName = this.in.getName();
      if (elementName.equals(POS_LIST)) {
        points = readPosList();
      } else if (elementName.equals(COORDINATES)) {
        points = readCoordinates();
      }
      StaxUtils.skipToEndElement(this.in, LINEAR_RING);
    } else {
      StaxUtils.skipToEndElement(this.in, LINEAR_RING);
    }
    return factory.createLinearRing(points);
  }

  private LineString readLineString(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(this.in, POS_LIST)) {
      points = readPosList();
      StaxUtils.skipToEndElement(this.in, LINE_STRING);
    } else if (StaxUtils.skipToChildStartElements(this.in, COORDINATES)) {
      points = readCoordinates();
      StaxUtils.skipToEndElement(this.in, LINE_STRING);
    } else {
      StaxUtils.skipToEndElement(this.in, LINE_STRING);
    }
    return factory.createLineString(points);
  }

  private Geometry readMultiGeometry(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Geometry> geometries = new ArrayList<Geometry>();
    StaxUtils.skipSubTree(this.in);
    return factory.createGeometry(geometries);
  }

  private MultiLineString readMultiLineString(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<LineString> lines = new ArrayList<LineString>();
    while (StaxUtils.skipToChildStartElements(this.in, LINE_STRING)) {
      final LineString line = readLineString(factory);
      if (line != null) {
        lines.add(line);
      }
    }
    StaxUtils.skipToEndElement(this.in, MULTI_LINE_STRING);
    return factory.createMultiLineString(lines);
  }

  private MultiPoint readMultiPoint(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final List<Point> points = new ArrayList<Point>();
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    while (StaxUtils.skipToChildStartElements(this.in, POINT)) {
      final Point point = readPoint(factory);
      if (point != null) {
        points.add(point);
      }
    }
    StaxUtils.skipToEndElement(this.in, MULTI_POINT);
    return factory.createMultiPoint(points);
  }

  private MultiPolygon readMultiPolygon(final GeometryFactory geometryFactory)
    throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<Polygon> polygons = new ArrayList<Polygon>();
    while (StaxUtils.skipToChildStartElements(this.in, POLYGON)) {
      final Polygon polygon = readPolygon(factory);
      if (polygon != null) {
        polygons.add(polygon);
      }
    }
    StaxUtils.skipToEndElement(this.in, MULTI_POLYGON);
    return factory.createMultiPolygon(polygons);
  }

  private Point readPoint(final GeometryFactory geometryFactory) throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    CoordinatesList points = null;
    if (StaxUtils.skipToChildStartElements(this.in, POS)) {
      points = readPosList();
      StaxUtils.skipToEndElement(this.in, POINT);
    } else if (StaxUtils.skipToChildStartElements(this.in, COORDINATES)) {
      points = readCoordinates();
      StaxUtils.skipToEndElement(this.in, POINT);
    } else {
      StaxUtils.skipToEndElement(this.in, POINT);
    }
    return factory.createPoint(points);
  }

  private Polygon readPolygon(final GeometryFactory geometryFactory) throws XMLStreamException {
    final GeometryFactory factory = getGeometryFactory(geometryFactory);
    final List<LinearRing> rings = new ArrayList<LinearRing>();
    if (StaxUtils.skipToChildStartElements(this.in, OUTER_BOUNDARY_IS)) {
      final LinearRing exteriorRing = readLinearRing(factory);
      rings.add(exteriorRing);
      StaxUtils.skipToEndElement(this.in, OUTER_BOUNDARY_IS);
      while (StaxUtils.skipToChildStartElements(this.in, INNER_BOUNDARY_IS)) {
        final LinearRing interiorRing = readLinearRing(factory);
        rings.add(interiorRing);
        StaxUtils.skipToEndElement(this.in, INNER_BOUNDARY_IS);
      }
      StaxUtils.skipToEndElement(this.in, POLYGON);
    } else {
      StaxUtils.skipSubTree(this.in);
    }
    final Polygon polygon = factory.createPolygon(rings);
    return polygon;
  }

  private CoordinatesList readPosList() throws XMLStreamException {
    final String dimension = this.in.getAttributeValue(null, "dimension");
    if (dimension == null) {
      StaxUtils.skipSubTree(this.in);
      return null;
    } else {
      final int numAxis = Integer.parseInt(dimension);
      final String value = this.in.getElementText();
      final CoordinatesList points = CoordinatesListUtil.parse(value, "\\s+", numAxis);
      StaxUtils.skipToEndElement(this.in);
      return points;
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
