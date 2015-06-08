package com.revolsys.gis.model.geometry.operation.relate;

/**
 * @version 1.7
 */

import com.revolsys.gis.model.geometry.Geometry;
import com.vividsolutions.jts.algorithm.BoundaryNodeRule;
import com.vividsolutions.jts.geom.IntersectionMatrix;

/**
 * Implements the SFS <tt>relate()</tt> operation on two {@link Geometry}s.
 * This class supports specifying a custom {@link BoundaryNodeRule}
 * to be used during the relate computation.
 * <p>
 * <b>Note:</b> custom Boundary Node Rules do not (currently)
 * affect the results of other Geometry methods (such
 * as {@link Geometry#getBoundary}.  The results of
 * these methods may not be consistent with the relationship computed by
 * a custom Boundary Node Rule.
 *
 * @version 1.7
 */
public class RelateOp extends GeometryGraphOperation {
  /**
   * Computes the {@link IntersectionMatrix} for the spatial relationship
   * between two {@link Geometry}s, using the default (OGC SFS) Boundary Node Rule
   *
   * @param a a Geometry to test
   * @param b a Geometry to test
   * @return the IntersectonMatrix for the spatial relationship between the geometries
   */
  public static IntersectionMatrix relate(final Geometry a, final Geometry b) {
    final RelateOp relOp = new RelateOp(a, b);
    final IntersectionMatrix im = relOp.getIntersectionMatrix();
    return im;
  }

  /**
   * Computes the {@link IntersectionMatrix} for the spatial relationship
   * between two {@link Geometry}s using a specified Boundary Node Rule.
   *
   * @param a a Geometry to test
   * @param b a Geometry to test
   * @param boundaryNodeRule the Boundary Node Rule to use
   * @return the IntersectonMatrix for the spatial relationship between the input geometries
   */
  public static IntersectionMatrix relate(final Geometry a, final Geometry b,
    final BoundaryNodeRule boundaryNodeRule) {
    final RelateOp relOp = new RelateOp(a, b, boundaryNodeRule);
    final IntersectionMatrix im = relOp.getIntersectionMatrix();
    return im;
  }

  private final RelateComputer relate;

  /**
   * Creates a new Relate operation, using the default (OGC SFS) Boundary Node Rule.
   *
   * @param g0 a Geometry to relate
   * @param g1 another Geometry to relate
   */
  public RelateOp(final Geometry g0, final Geometry g1) {
    super(g0, g1);
    this.relate = new RelateComputer(this.arg);
  }

  /**
   * Creates a new Relate operation with a specified Boundary Node Rule.
   *
   * @param g0 a Geometry to relate
   * @param g1 another Geometry to relate
   * @param boundaryNodeRule the Boundary Node Rule to use
   */
  public RelateOp(final Geometry g0, final Geometry g1, final BoundaryNodeRule boundaryNodeRule) {
    super(g0, g1, boundaryNodeRule);
    this.relate = new RelateComputer(this.arg);
  }

  /**
   * Gets the IntersectionMatrix for the spatial relationship
   * between the input geometries.
   *
   * @return the IntersectonMatrix for the spatial relationship between the input geometries
   */
  public IntersectionMatrix getIntersectionMatrix() {
    return this.relate.computeIM();
  }

}
