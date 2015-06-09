package com.revolsys.gis.model.geometry.operation.geomgraph.index;

/**
 *@version 1.7
 */

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.vividsolutions.jts.algorithm.NotRepresentableException;
import com.vividsolutions.jts.algorithm.RobustDeterminant;

/**
 * A robust version of {@link LineIntersector}.
 *
 * @version 1.7
 * @see RobustDeterminant
 */
public class RobustLineIntersector extends LineIntersector {

  public RobustLineIntersector() {
  }

  private int computeCollinearIntersection(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    final boolean p1q1p2 = BoundingBox.intersects(p1, p2, q1);
    final boolean p1q2p2 = BoundingBox.intersects(p1, p2, q2);
    final boolean q1p1q2 = BoundingBox.intersects(q1, q2, p1);
    final boolean q1p2q2 = BoundingBox.intersects(q1, q2, p2);

    if (p1q1p2 && p1q2p2) {
      this.intPt[0] = q1;
      this.intPt[1] = q2;
      return COLLINEAR_INTERSECTION;
    }
    if (q1p1q2 && q1p2q2) {
      this.intPt[0] = p1;
      this.intPt[1] = p2;
      return COLLINEAR_INTERSECTION;
    }
    if (p1q1p2 && q1p1q2) {
      this.intPt[0] = q1;
      this.intPt[1] = p1;
      return q1.equals(p1) && !p1q2p2 && !q1p2q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    if (p1q1p2 && q1p2q2) {
      this.intPt[0] = q1;
      this.intPt[1] = p2;
      return q1.equals(p2) && !p1q2p2 && !q1p1q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    if (p1q2p2 && q1p1q2) {
      this.intPt[0] = q2;
      this.intPt[1] = p1;
      return q2.equals(p1) && !p1q1p2 && !q1p2q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    if (p1q2p2 && q1p2q2) {
      this.intPt[0] = q2;
      this.intPt[1] = p2;
      return q2.equals(p2) && !p1q1p2 && !q1p1q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    return NO_INTERSECTION;
  }

  @Override
  protected int computeIntersect(final Coordinates p1, final Coordinates p2, final Coordinates q1,
    final Coordinates q2) {
    this.isProper = false;

    // first try a fast test to see if the envelopes of the lines intersect
    if (!BoundingBox.intersects(p1, p2, q1, q2)) {
      return NO_INTERSECTION;
    }

    // for each endpoint, compute which side of the other segment it lies
    // if both endpoints lie on the same side of the other segment,
    // the segments do not intersect
    final int Pq1 = CoordinatesUtil.orientationIndex(p1, p2, q1);
    final int Pq2 = CoordinatesUtil.orientationIndex(p1, p2, q2);

    if (Pq1 > 0 && Pq2 > 0 || Pq1 < 0 && Pq2 < 0) {
      return NO_INTERSECTION;
    }

    final int Qp1 = CoordinatesUtil.orientationIndex(q1, q2, p1);
    final int Qp2 = CoordinatesUtil.orientationIndex(q1, q2, p2);

    if (Qp1 > 0 && Qp2 > 0 || Qp1 < 0 && Qp2 < 0) {
      return NO_INTERSECTION;
    }

    final boolean collinear = Pq1 == 0 && Pq2 == 0 && Qp1 == 0 && Qp2 == 0;
    if (collinear) {
      return computeCollinearIntersection(p1, p2, q1, q2);
    }

    /**
     * At this point we know that there is a single intersection point (since
     * the lines are not collinear).
     */

    /**
     * Check if the intersection is an endpoint. If it is, copy the endpoint as
     * the intersection point. Copying the point rather than computing it
     * ensures the point has the exact value, which is important for robustness.
     * It is sufficient to simply check for an endpoint which is on the other
     * line, since at this point we know that the inputLines must intersect.
     */
    if (Pq1 == 0 || Pq2 == 0 || Qp1 == 0 || Qp2 == 0) {
      this.isProper = false;

      /**
       * Check for two equal endpoints. This is done explicitly rather than by
       * the orientation tests below in order to improve robustness. [An example
       * where the orientation tests fail to be consistent is the following
       * (where the true intersection is at the shared endpoint POINT
       * (19.850257749638203 46.29709338043669) LINESTRING ( 19.850257749638203
       * 46.29709338043669, 20.31970698357233 46.76654261437082 ) and LINESTRING
       * ( -48.51001596420236 -22.063180333403878, 19.850257749638203
       * 46.29709338043669 ) which used to produce the INCORRECT result:
       * (20.31970698357233, 46.76654261437082, NaN)
       */
      if (p1.equals2d(q1) || p1.equals2d(q2)) {
        this.intPt[0] = p1;
      } else if (p2.equals2d(q1) || p2.equals2d(q2)) {
        this.intPt[0] = p2;
      }

      /**
       * Now check to see if any endpoint lies on the interior of the other
       * segment.
       */
      else if (Pq1 == 0) {
        this.intPt[0] = new DoubleCoordinates(q1);
      } else if (Pq2 == 0) {
        this.intPt[0] = new DoubleCoordinates(q2);
      } else if (Qp1 == 0) {
        this.intPt[0] = new DoubleCoordinates(p1);
      } else if (Qp2 == 0) {
        this.intPt[0] = new DoubleCoordinates(p2);
      }
    } else {
      this.isProper = true;
      this.intPt[0] = intersection(p1, p2, q1, q2);
    }
    return POINT_INTERSECTION;
  }

  @Override
  public void computeIntersection(final Coordinates p, final Coordinates p1, final Coordinates p2) {
    this.isProper = false;
    // do between check first, since it is faster than the orientation test
    if (BoundingBox.intersects(p1, p2, p)) {
      if (CoordinatesUtil.orientationIndex(p1, p2, p) == 0
          && CoordinatesUtil.orientationIndex(p2, p1, p) == 0) {
        this.isProper = true;
        if (p.equals(p1) || p.equals(p2)) {
          this.isProper = false;
        }
        this.result = POINT_INTERSECTION;
        return;
      }
    }
    this.result = NO_INTERSECTION;
  }

  /**
   * This method computes the actual value of the intersection point. To obtain
   * the maximum precision from the intersection calculation, the coordinates
   * are normalized by subtracting the minimum ordinate values (in absolute
   * value). This has the effect of removing common significant digits from the
   * calculation to maintain more bits of precision.
   */
  private Coordinates intersection(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    Coordinates intPt = intersectionWithNormalization(p1, p2, q1, q2);
    // testing only
    // Coordinates intPt = safeHCoordinatesIntersection(p1, p2, q1, q2);

    /**
     * Due to rounding it can happen that the computed intersection is outside
     * the envelopes of the input segments. Clearly this is inconsistent. This
     * code checks this condition and forces a more reasonable answer MD - May 4
     * 2005 - This is still a problem. Here is a failure case: LINESTRING
     * (2089426.5233462777 1180182.3877339689, 2085646.6891757075
     * 1195618.7333999649) LINESTRING (1889281.8148903656 1997547.0560044837,
     * 2259977.3672235999 483675.17050843034) int point =
     * (2097408.2633752143,1144595.8008114607) MD - Dec 14 2006 - This does not
     * seem to be a failure case any longer
     */
    if (!isInSegmentBoundingBoxs(intPt)) {
      // System.out.println("Intersection outside segment envelopes: " + intPt);
      // System.out.println("Segments: " + this);
      // compute a safer result
      intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);
      // System.out.println("Snapped to " + intPt);
    }

    if (this.precisionModel != null) {
      this.precisionModel.makePrecise(intPt);
    }

    return intPt;
  }

  private Coordinates intersectionWithNormalization(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    final Coordinates n1 = new DoubleCoordinates(p1);
    final Coordinates n2 = new DoubleCoordinates(p2);
    final Coordinates n3 = new DoubleCoordinates(q1);
    final Coordinates n4 = new DoubleCoordinates(q2);
    final Coordinates normPt = new DoubleCoordinates(2);
    normalizeToEnvCentre(n1, n2, n3, n4, normPt);

    final Coordinates intPt = safeHCoordinatesIntersection(n1, n2, n3, n4);

    intPt.setX(intPt.getX() + normPt.getX());
    intPt.setY(intPt.getY() + normPt.getY());

    return intPt;
  }

  /**
   * Test whether a point lies in the envelopes of both input segments. A
   * correctly computed intersection point should return <code>true</code> for
   * this test. Since this test is for debugging purposes only, no attempt is
   * made to optimize the envelope test.
   *
   * @return <code>true</code> if the input point lies within both input segment
   *         envelopes
   */
  private boolean isInSegmentBoundingBoxs(final Coordinates intPt) {
    final BoundingBox env0 = new BoundingBox(this.inputLines[0][0], this.inputLines[0][1]);
    final BoundingBox env1 = new BoundingBox(this.inputLines[1][0], this.inputLines[1][1]);
    return env0.contains(intPt) && env1.contains(intPt);
  }

  /**
   * Normalize the supplied coordinates to so that the midpoint of their
   * intersection envelope lies at the origin.
   *
   * @param n00
   * @param n01
   * @param n10
   * @param n11
   * @param normPt
   */
  private void normalizeToEnvCentre(final Coordinates n00, final Coordinates n01,
    final Coordinates n10, final Coordinates n11, final Coordinates normPt) {
    final double minX0 = n00.getX() < n01.getX() ? n00.getX() : n01.getX();
    final double minY0 = n00.getY() < n01.getY() ? n00.getY() : n01.getY();
    final double maxX0 = n00.getX() > n01.getX() ? n00.getX() : n01.getX();
    final double maxY0 = n00.getY() > n01.getY() ? n00.getY() : n01.getY();

    final double minX1 = n10.getX() < n11.getX() ? n10.getX() : n11.getX();
    final double minY1 = n10.getY() < n11.getY() ? n10.getY() : n11.getY();
    final double maxX1 = n10.getX() > n11.getX() ? n10.getX() : n11.getX();
    final double maxY1 = n10.getY() > n11.getY() ? n10.getY() : n11.getY();

    final double intMinX = minX0 > minX1 ? minX0 : minX1;
    final double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
    final double intMinY = minY0 > minY1 ? minY0 : minY1;
    final double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;

    final double intMidX = (intMinX + intMaxX) / 2.0;
    final double intMidY = (intMinY + intMaxY) / 2.0;
    normPt.setX(intMidX);
    normPt.setY(intMidY);

    /*
     * // equilavalent code using more modular but slower method BoundingBox
     * env0 = new BoundingBox(n00, n01); BoundingBox env1 = new BoundingBox(n10,
     * n11); BoundingBox intEnv = env0.intersection(env1); Coordinates intMidPt
     * = intEnv.centre(); normPt.getX() = intMidPt.getX(); normPt.getY() =
     * intMidPt.getY();
     */

    n00.setX(n00.getX() - normPt.getX());
    n00.setY(n00.getY() - normPt.getY());
    n01.setX(n01.getX() - normPt.getX());
    n01.setY(n01.getY() - normPt.getY());
    n10.setX(n10.getX() - normPt.getX());
    n10.setY(n10.getY() - normPt.getY());
    n11.setX(n11.getX() - normPt.getX());
    n11.setY(n11.getY() - normPt.getY());
  }

  /**
   * Computes a segment intersection using homogeneous coordinates. Round-off
   * error can cause the raw computation to fail, (usually due to the segments
   * being approximately parallel). If this happens, a reasonable approximation
   * is computed instead.
   *
   * @param p1 a segment endpoint
   * @param p2 a segment endpoint
   * @param q1 a segment endpoint
   * @param q2 a segment endpoint
   * @return the computed intersection point
   */
  private Coordinates safeHCoordinatesIntersection(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    Coordinates intPt = null;
    try {
      intPt = HCoordinate.intersection(p1, p2, q1, q2);
    } catch (final NotRepresentableException e) {
      // System.out.println("Not calculable: " + this);
      // compute an approximate result
      intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);
      // System.out.println("Snapped to " + intPt);
    }
    return intPt;
  }

}
