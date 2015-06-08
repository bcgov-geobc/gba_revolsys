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
package com.revolsys.gis.model.geometry.operation.buffer;

import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryFactory;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.gis.model.geometry.operation.chain.MCIndexSnapRounder;
import com.revolsys.gis.model.geometry.operation.chain.Noder;
import com.revolsys.gis.model.geometry.operation.noding.snapround.ScaledNoder;
import com.revolsys.gis.model.geometry.util.TopologyException;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * @version 1.7
 */
//import debug.*;

/**
 * Computes the buffer of a geometry, for both positive and negative buffer distances.
 * <p>
 * In GIS, the positive (or negative) buffer of a geometry is defined as
 * the Minkowski sum (or difference) of the geometry
 * with a circle of radius equal to the absolute value of the buffer distance.
 * In the CAD/CAM world buffers are known as </i>offset curves</i>.
 * In morphological analysis the
 * operation of postive and negative buffering
 * is referred to as <i>erosion</i> and <i>dilation</i>
 * <p>
 * The buffer operation always returns a polygonal result.
 * The negative or zero-distance buffer of lines and points is always an empty {@link Polygon}.
 * <p>
 * Since true buffer curves may contain circular arcs,
 * computed buffer polygons can only be approximations to the true geometry.
 * The user can control the accuracy of the curve approximation by specifying
 * the number of linear segments used to approximate curves.
 * <p>
 * The <b>end cap style</b> of a linear buffer may be specified. The
 * following end cap styles are supported:
 * <ul
 * <li>{@link #CAP_ROUND} - the usual round end caps
 * <li>{@link #CAP_BUTT} - end caps are truncated flat at the line ends
 * <li>{@link #CAP_SQUARE} - end caps are squared off at the buffer distance beyond the line ends
 * </ul>
 * <p>
 *
 * @version 1.7
 */
public class BufferOp {
  /**
   * Specifies a round line buffer end cap style.
   * @deprecated use BufferParameters
   */
  @Deprecated
  public static final int CAP_ROUND = BufferParameters.CAP_ROUND;

  /**
   * Specifies a butt (or flat) line buffer end cap style.
   * @deprecated use BufferParameters
   */
  @Deprecated
  public static final int CAP_BUTT = BufferParameters.CAP_FLAT;

  /**
   * Specifies a butt (or flat) line buffer end cap style.
   * @deprecated use BufferParameters
   */
  @Deprecated
  public static final int CAP_FLAT = BufferParameters.CAP_FLAT;

  /**
   * Specifies a square line buffer end cap style.
   * @deprecated use BufferParameters
   */
  @Deprecated
  public static final int CAP_SQUARE = BufferParameters.CAP_SQUARE;

  /**
   * A number of digits of precision which leaves some computational "headroom"
   * for floating point operations.
   *
   * This value should be less than the decimal precision of double-precision values (16).
   */
  private static int MAX_PRECISION_DIGITS = 12;

  /**
   * Computes the buffer of a geometry for a given buffer distance.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @return the buffer of the input geometry
   */
  public static Geometry bufferOp(final Geometry g, final double distance) {
    final BufferOp gBuf = new BufferOp(g);
    final Geometry geomBuf = gBuf.getResultGeometry(distance);
    // BufferDebug.saveBuffer(geomBuf);
    // BufferDebug.runCount++;
    return geomBuf;
  }

  /**
   * Comutes the buffer for a geometry for a given buffer distance
   * and accuracy of approximation.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @param params the buffer parameters to use
   * @return the buffer of the input geometry
   *
   */
  public static Geometry bufferOp(final Geometry g, final double distance,
    final BufferParameters params) {
    final BufferOp bufOp = new BufferOp(g, params);
    final Geometry geomBuf = bufOp.getResultGeometry(distance);
    return geomBuf;
  }

  /**
   * Comutes the buffer for a geometry for a given buffer distance
   * and accuracy of approximation.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @param quadrantSegments the number of segments used to approximate a quarter circle
   * @return the buffer of the input geometry
   *
   */
  public static Geometry bufferOp(final Geometry g, final double distance,
    final int quadrantSegments) {
    final BufferOp bufOp = new BufferOp(g);
    bufOp.setQuadrantSegments(quadrantSegments);
    final Geometry geomBuf = bufOp.getResultGeometry(distance);
    return geomBuf;
  }

  /**
   * Comutes the buffer for a geometry for a given buffer distance
   * and accuracy of approximation.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @param quadrantSegments the number of segments used to approximate a quarter circle
   * @param endCapStyle the end cap style to use
   * @return the buffer of the input geometry
   *
   */
  public static Geometry bufferOp(final Geometry g, final double distance,
    final int quadrantSegments, final int endCapStyle) {
    final BufferOp bufOp = new BufferOp(g);
    bufOp.setQuadrantSegments(quadrantSegments);
    bufOp.setEndCapStyle(endCapStyle);
    final Geometry geomBuf = bufOp.getResultGeometry(distance);
    return geomBuf;
  }

  /**
   * Compute a scale factor to limit the precision of
   * a given combination of Geometry and buffer distance.
   * The scale factor is determined by a combination of
   * the number of digits of precision in the (geometry + buffer distance),
   * limited by the supplied <code>maxPrecisionDigits</code> value.
   *
   * @param g the Geometry being buffered
   * @param distance the buffer distance
   * @param maxPrecisionDigits the max # of digits that should be allowed by
   *          the precision determined by the computed scale factor
   *
   * @return a scale factor for the buffer computation
   */
  private static double precisionScaleFactor(final Geometry g, final double distance,
    final int maxPrecisionDigits) {
    final BoundingBox env = g.getBoundingBox();
    final double envSize = Math.max(env.getHeight(), env.getWidth());
    final double expandByDistance = distance > 0.0 ? distance : 0.0;
    final double bufEnvSize = envSize + 2 * expandByDistance;

    // the smallest power of 10 greater than the buffer envelope
    final int bufEnvLog10 = (int)(Math.log(bufEnvSize) / Math.log(10) + 1.0);
    final int minUnitLog10 = bufEnvLog10 - maxPrecisionDigits;
    // scale factor is inverse of min Unit size, so flip sign of exponent
    final double scaleFactor = Math.pow(10.0, -minUnitLog10);
    return scaleFactor;
  }

  private final Geometry argGeom;

  private double distance;

  private BufferParameters bufParams = new BufferParameters();

  private Geometry resultGeometry = null;

  private RuntimeException saveException; // debugging only

  /**
   * Initializes a buffer computation for the given geometry
   *
   * @param g the geometry to buffer
   */
  public BufferOp(final Geometry g) {
    this.argGeom = g;
  }

  /**
   * Initializes a buffer computation for the given geometry
   * with the given set of parameters
   *
   * @param g the geometry to buffer
   * @param bufParams the buffer parameters to use
   */
  public BufferOp(final Geometry g, final BufferParameters bufParams) {
    this.argGeom = g;
    this.bufParams = bufParams;
  }

  private void bufferFixedPrecision(final CoordinatesPrecisionModel fixedPM) {
    final Noder noder = new ScaledNoder(new MCIndexSnapRounder(new SimpleCoordinatesPrecisionModel(
      1.0)), fixedPM.getScaleXY());

    final BufferBuilder bufBuilder = new BufferBuilder(this.bufParams);
    bufBuilder.setWorkingPrecisionModel(fixedPM);
    bufBuilder.setNoder(noder);
    // this may throw an exception, if robustness errors are encountered
    this.resultGeometry = bufBuilder.buffer(this.argGeom, this.distance);
  }

  private void bufferOriginalPrecision() {
    try {
      // use fast noding by default
      final BufferBuilder bufBuilder = new BufferBuilder(this.bufParams);
      this.resultGeometry = bufBuilder.buffer(this.argGeom, this.distance);
    } catch (final RuntimeException ex) {
      this.saveException = ex;
      // don't propagate the exception - it will be detected by fact that
      // resultGeometry is null

      // testing ONLY - propagate exception
      // throw ex;
    }
  }

  private void bufferReducedPrecision() {
    // try and compute with decreasing precision
    for (int precDigits = MAX_PRECISION_DIGITS; precDigits >= 0; precDigits--) {
      try {
        bufferReducedPrecision(precDigits);
      } catch (final TopologyException ex) {
        // update the saved exception to reflect the new input geometry
        this.saveException = ex;
        // don't propagate the exception - it will be detected by fact that
        // resultGeometry is null
      }
      if (this.resultGeometry != null) {
        return;
      }
    }

    // tried everything - have to bail
    throw this.saveException;
  }

  private void bufferReducedPrecision(final int precisionDigits) {
    final double sizeBasedScaleFactor = precisionScaleFactor(this.argGeom, this.distance,
      precisionDigits);
    // System.out.println("recomputing with precision scale factor = " +
    // sizeBasedScaleFactor);

    final CoordinatesPrecisionModel fixedPM = new SimpleCoordinatesPrecisionModel(
      sizeBasedScaleFactor);
    bufferFixedPrecision(fixedPM);
  }

  private void computeGeometry() {
    bufferOriginalPrecision();
    if (this.resultGeometry != null) {
      return;
    }

    final GeometryFactory geometryFactory = this.argGeom.getGeometryFactory();
    if (geometryFactory.getScaleXY() > 0) {
      bufferFixedPrecision(geometryFactory);
    } else {
      bufferReducedPrecision();
    }
  }

  /**
   * Returns the buffer computed for a geometry for a given buffer distance.
   *
   * @param distance the buffer distance
   * @return the buffer of the input geometry
   */
  public Geometry getResultGeometry(final double distance) {
    this.distance = distance;
    computeGeometry();
    return this.resultGeometry;
  }

  /**
   * Specifies the end cap style of the generated buffer.
   * The styles supported are {@link #CAP_ROUND}, {@link #CAP_BUTT}, and {@link #CAP_SQUARE}.
   * The default is CAP_ROUND.
   *
   * @param endCapStyle the end cap style to specify
   */
  public void setEndCapStyle(final int endCapStyle) {
    this.bufParams.setEndCapStyle(endCapStyle);
  }

  /**
   * Sets the number of segments used to approximate a angle fillet
   *
   * @param quadrantSegments the number of segments in a fillet for a quadrant
   */
  public void setQuadrantSegments(final int quadrantSegments) {
    this.bufParams.setQuadrantSegments(quadrantSegments);
  }
}
