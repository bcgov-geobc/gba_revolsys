/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.wkb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.LineStringDouble;

/**
 * Reads a {@link Geometry}from a byte stream in Well-Known Binary format.
 * Supports use of an {@link InStream}, which allows easy use
 * with arbitrary byte stream sources.
 * <p>
 * This class reads the format describe in {@link WKBWriter}.
 * It also partially handles
 * the <b>Extended WKB</b> format used by PostGIS,
 * by parsing and storing SRID values.
 * The reader repairs structurally-invalid input
 * (specifically, LineStrings and LinearRings which contain
 * too few points have vertices added,
 * and non-closed rings are closed).
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 *
 * @see WKBWriter for a formal format specification
 */
public class WKBReader {
  private static final String INVALID_GEOM_TYPE_MSG = "Invalid geometry type encountered in ";

  public static LineString createClosedRing(final LineString seq, final int size) {
    final int axisCount = seq.getAxisCount();
    final double[] coordinates = new double[size * axisCount];
    final int n = seq.getVertexCount();
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, seq, 0, n);
    // fill remaining coordinates with start point
    for (int i = n; i < size; i++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, i, seq, 0, 1);
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  /**
   * Ensures that a LineString forms a valid ring,
   * returning a new closed sequence of the correct length if required.
   * If the input sequence is already a valid ring, it is returned
   * without modification.
   * If the input sequence is too short or is not closed,
   * it is extended with one or more copies of the start point.
   * @param seq the sequence to test
   * @param geometryFactory the CoordinateSequenceFactory to use to create the new sequence
   *
   * @return the original sequence, if it was a valid ring, or a new sequence which is valid.
   */
  public static LineString ensureValidRing(final LineString seq) {
    final int n = seq.getVertexCount();
    // empty sequence is valid
    if (n == 0) {
      return seq;
    }
    // too short - make a new one
    if (n <= 3) {
      return createClosedRing(seq, 4);
    }

    final boolean isClosed = seq.getCoordinate(0, Geometry.X) == seq.getCoordinate(n - 1,
      Geometry.X) && seq.getCoordinate(0, Geometry.Y) == seq.getCoordinate(n - 1, Geometry.Y);
    if (isClosed) {
      return seq;
    }
    // make a new closed ring
    return createClosedRing(seq, n + 1);
  }

  public static LineString extend(final LineString seq, final int size) {
    final int axisCount = seq.getAxisCount();
    final double[] coordinates = new double[size * axisCount];
    final int n = seq.getVertexCount();
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, seq, 0, n);

    // fill remaining coordinates with end point, if it exists
    if (n > 0) {
      for (int i = n; i < size; i++) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, i, seq, n - 1, 1);
      }
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  /**
   * Converts a hexadecimal string to a byte array.
   * The hexadecimal digit symbols are case-insensitive.
   *
   * @param hex a string containing hex digits
   * @return an array of bytes with the value of the hex string
   */
  public static byte[] hexToBytes(final String hex) {
    final int byteLen = hex.length() / 2;
    final byte[] bytes = new byte[byteLen];

    for (int i = 0; i < hex.length() / 2; i++) {
      final int i2 = 2 * i;
      if (i2 + 1 > hex.length()) {
        throw new IllegalArgumentException("Hex string has odd length");
      }

      final int nib1 = hexToInt(hex.charAt(i2));
      final int nib0 = hexToInt(hex.charAt(i2 + 1));
      final byte b = (byte)((nib1 << 4) + (byte)nib0);
      bytes[i] = b;
    }
    return bytes;
  }

  private static int hexToInt(final char hex) {
    final int nib = Character.digit(hex, 16);
    if (nib < 0) {
      throw new IllegalArgumentException("Invalid hex digit: '" + hex + "'");
    }
    return nib;
  }

  /**
   * Tests whether two {@link LineString}s are equal.
   * To be equal, the sequences must be the same length.
   * They do not need to be of the same dimension,
   * but the ordinate values for the smallest dimension of the two
   * must be equal.
   * Two <code>NaN</code> ordinates values are considered to be equal.
   *
   * @param cs1 a LineString
   * @param cs2 a LineString
   * @return true if the sequences are equal in the common dimensions
   */
  public static boolean isEqual(final LineString cs1, final LineString cs2) {
    final int cs1Size = cs1.getVertexCount();
    final int cs2Size = cs2.getVertexCount();
    if (cs1Size != cs2Size) {
      return false;
    }
    final int dim = Math.min(cs1.getAxisCount(), cs2.getAxisCount());
    for (int i = 0; i < cs1Size; i++) {
      for (int d = 0; d < dim; d++) {
        final double v1 = cs1.getCoordinate(i, d);
        final double v2 = cs2.getCoordinate(i, d);
        if (cs1.getCoordinate(i, d) == cs2.getCoordinate(i, d)) {
          continue;
        }
        // special check for NaNs
        if (Double.isNaN(v1) && Double.isNaN(v2)) {
          continue;
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Tests whether a {@link LineString} forms a valid {@link LinearRing},
   * by checking the sequence length and closure
   * (whether the first and last points are identical in 2D).
   * Self-intersection is not checked.
   *
   * @param seq the sequence to test
   * @return true if the sequence is a ring
   * @see LinearRing
   */
  public static boolean isRing(final LineString seq) {
    final int n = seq.getVertexCount();
    if (n == 0) {
      return true;
    }
    // too few points
    if (n <= 3) {
      return false;
    }
    // test if closed
    return seq.getCoordinate(0, Geometry.X) == seq.getCoordinate(n - 1, Geometry.X)
      && seq.getCoordinate(0, Geometry.Y) == seq.getCoordinate(n - 1, Geometry.Y);
  }

  private final ByteOrderDataInStream dis = new ByteOrderDataInStream();

  // private final PrecisionModel precisionModel;

  private final GeometryFactory factory;

  private boolean hasSRID = false;

  // default dimension - will be set on read
  private int inputDimension = 2;

  /**
   * true if structurally invalid input should be reported rather than repaired.
   * At some point this could be made client-controllable.
   */
  private final boolean isStrict = false;

  private double[] ordValues;

  public WKBReader() {
    this(GeometryFactory.floating3());
  }

  public WKBReader(final GeometryFactory geometryFactory) {
    this.factory = geometryFactory;
  }

  /**
   * Reads a single {@link Geometry} in WKB format from a byte array.
   *
   * @param bytes the byte array to read from
   * @return the geometry read
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(final byte[] bytes) throws ParseException {
    // possibly reuse the ByteArrayInStream?
    // don't throw IOExceptions, since we are not doing any I/O
    try {
      return read(new ByteArrayInStream(bytes));
    } catch (final IOException ex) {
      throw new RuntimeException("Unexpected IOException caught: " + ex.getMessage());
    }
  }

  /**
   * Reads a {@link Geometry} in binary WKB format from an {@link InStream}.
   *
   * @param is the stream to read from
   * @return the Geometry read
   * @throws IOException if the underlying stream creates an error
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(final InStream is) throws IOException, ParseException {
    this.dis.setInStream(is);
    final Geometry g = readGeometry();
    return g;
  }

  /**
   * Reads a coordinate value with the specified dimensionality.
   * Makes the X and Y ordinates precise according to the precision model
   * in use.
   */
  private void readCoordinate() throws IOException {
    for (int i = 0; i < this.inputDimension; i++) {
      this.ordValues[i] = this.factory.makePrecise(i, this.dis.readDouble());
    }
  }

  private LineString readCoordinateSequence(final int size) throws IOException {
    final double[] coordinates = new double[size * this.inputDimension];

    for (int i = 0; i < size; i++) {
      readCoordinate();
      for (int j = 0; j < this.inputDimension; j++) {
        coordinates[i * this.inputDimension + j] = this.ordValues[j];
      }
    }
    return new LineStringDouble(this.inputDimension, coordinates);
  }

  private LineString readCoordinateSequenceLineString(final int size) throws IOException {
    final LineString seq = readCoordinateSequence(size);
    if (this.isStrict) {
      return seq;
    }
    if (seq.getVertexCount() == 0 || seq.getVertexCount() >= 2) {
      return seq;
    }
    return WKBReader.extend(seq, 2);
  }

  private LineString readCoordinateSequenceRing(final int size) throws IOException {
    final LineString seq = readCoordinateSequence(size);
    if (this.isStrict) {
      return seq;
    }
    if (isRing(seq)) {
      return seq;
    }
    return WKBReader.ensureValidRing(seq);
  }

  private Geometry readGeometry() throws IOException, ParseException {

    // determine byte order
    final byte byteOrderWKB = this.dis.readByte();

    // always set byte order, since it may change from geometry to geometry
    if (byteOrderWKB == WKBConstants.wkbNDR) {
      this.dis.setOrder(ByteOrderValues.LITTLE_ENDIAN);
    } else if (byteOrderWKB == WKBConstants.wkbXDR) {
      this.dis.setOrder(ByteOrderValues.BIG_ENDIAN);
    } else if (this.isStrict) {
      throw new ParseException("Unknown geometry byte order (not NDR or XDR): " + byteOrderWKB);
    }
    // if not strict and not XDR or NDR, then we just use the dis default set at
    // the
    // start of the geometry (if a multi-geometry). This allows WBKReader to
    // work
    // with Spatialite native BLOB WKB, as well as other WKB variants that might
    // just
    // specify endian-ness at the start of the multigeometry.

    final int typeInt = this.dis.readInt();
    final int geometryType = typeInt & 0xff;
    // determine if Z values are present
    final boolean hasZ = (typeInt & 0x80000000) != 0;
    this.inputDimension = hasZ ? 3 : 2;
    // determine if SRIDs are present
    this.hasSRID = (typeInt & 0x20000000) != 0;

    int SRID = 0;
    if (this.hasSRID) {
      SRID = this.dis.readInt();
    }

    // only allocate ordValues buffer if necessary
    if (this.ordValues == null || this.ordValues.length < this.inputDimension) {
      this.ordValues = new double[this.inputDimension];
    }

    Geometry geom = null;
    switch (geometryType) {
      case WKBConstants.wkbPoint:
        geom = readPoint();
      break;
      case WKBConstants.wkbLineString:
        geom = readLineString();
      break;
      case WKBConstants.wkbPolygon:
        geom = readPolygon();
      break;
      case WKBConstants.wkbMultiPoint:
        geom = readMultiPoint();
      break;
      case WKBConstants.wkbMultiLineString:
        geom = readMultiLineString();
      break;
      case WKBConstants.wkbMultiPolygon:
        geom = readMultiPolygon();
      break;
      case WKBConstants.wkbGeometryCollection:
        geom = readGeometryCollection();
      break;
      default:
        throw new ParseException("Unknown WKB type " + geometryType);
    }
    setSRID(geom, SRID);
    return geom;
  }

  private GeometryCollection readGeometryCollection() throws IOException, ParseException {
    final int numGeom = this.dis.readInt();
    final List<Geometry> geoms = new ArrayList<Geometry>();
    for (int i = 0; i < numGeom; i++) {
      final Geometry geometry = readGeometry();
      geoms.add(geometry);
    }
    return this.factory.geometryCollection(geoms);
  }

  private LinearRing readLinearRing() throws IOException {
    final int size = this.dis.readInt();
    final LineString pts = readCoordinateSequenceRing(size);
    return this.factory.linearRing(pts);
  }

  private LineString readLineString() throws IOException {
    final int size = this.dis.readInt();
    final LineString pts = readCoordinateSequenceLineString(size);
    return this.factory.lineString(pts);
  }

  private MultiLineString readMultiLineString() throws IOException, ParseException {
    final int numGeom = this.dis.readInt();
    final LineString[] geoms = new LineString[numGeom];
    for (int i = 0; i < numGeom; i++) {
      final Geometry g = readGeometry();
      if (!(g instanceof LineString)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiLineString");
      }
      geoms[i] = (LineString)g;
    }
    return this.factory.multiLineString(geoms);
  }

  private MultiPoint readMultiPoint() throws IOException, ParseException {
    final int numGeom = this.dis.readInt();
    final Point[] geoms = new Point[numGeom];
    for (int i = 0; i < numGeom; i++) {
      final Geometry g = readGeometry();
      if (!(g instanceof Point)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPoint");
      }
      geoms[i] = (Point)g;
    }
    return this.factory.multiPoint(geoms);
  }

  private MultiPolygon readMultiPolygon() throws IOException, ParseException {
    final int numGeom = this.dis.readInt();
    final Polygon[] geoms = new Polygon[numGeom];

    for (int i = 0; i < numGeom; i++) {
      final Geometry g = readGeometry();
      if (!(g instanceof Polygon)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPolygon");
      }
      geoms[i] = (Polygon)g;
    }
    return this.factory.multiPolygon(geoms);
  }

  private Point readPoint() throws IOException {
    final LineString pts = readCoordinateSequence(1);
    return this.factory.point(pts);
  }

  private Polygon readPolygon() throws IOException {
    final int numRings = this.dis.readInt();
    final List<LinearRing> rings = new ArrayList<>();

    for (int i = 0; i < numRings; i++) {
      final LinearRing ring = readLinearRing();
      rings.add(ring);
    }
    return this.factory.polygon(rings);
  }

  /**
   * Sets the SRID, if it was specified in the WKB
   *
   * @param g the geometry to update
   * @return the geometry with an updated SRID value, if required
   */
  private Geometry setSRID(final Geometry g, final int SRID) {
    // if (SRID != 0)
    // g.setSRID(SRID);
    return g;
  }

}
