package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.Node;

/**
 * Computes the intersection of line segments,
 * and adds the intersection to the edges containing the segments.
 *
 * @version 1.7
 */
public class SegmentIntersector extends com.vividsolutions.jts.geomgraph.index.SegmentIntersector {

  public static boolean isAdjacentSegments(final int i1, final int i2) {
    return Math.abs(i1 - i2) == 1;
  }

  /**
   * These variables keep track of what types of intersections were
   * found during ALL edges that have been intersected.
   */
  private boolean hasIntersection = false;

  private boolean hasProper = false;

  private boolean hasProperInterior = false;

  // the proper intersection point found
  private Coordinate properIntersectionPoint = null;

  private final LineIntersector li;

  private final boolean includeProper;

  private final boolean recordIsolated;

  // testing only
  public int numTests = 0;

  private Collection[] bdyNodes;

  private final List<Coordinates> properIntersections = new ArrayList<Coordinates>();

  /*
   * public SegmentIntersector() { }
   */
  public SegmentIntersector(final LineIntersector li, final boolean includeProper,
    final boolean recordIsolated) {
    super(li, includeProper, recordIsolated);
    this.li = li;
    this.includeProper = includeProper;
    this.recordIsolated = recordIsolated;
  }

  /**
   * This method is called by clients of the EdgeIntersector class to test for and add
   * intersections for two segments of the edges being intersected.
   * Note that clients (such as MonotoneChainEdges) may choose not to intersect
   * certain pairs of segments for efficiency reasons.
   */
  @Override
  public void addIntersections(final Edge e0, final int segIndex0, final Edge e1,
    final int segIndex1) {
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }
    this.numTests++;
    final Coordinate p00 = e0.getCoordinates()[segIndex0];
    final Coordinate p01 = e0.getCoordinates()[segIndex0 + 1];
    final Coordinate p10 = e1.getCoordinates()[segIndex1];
    final Coordinate p11 = e1.getCoordinates()[segIndex1 + 1];

    this.li.computeIntersection(p00, p01, p10, p11);
    // if (li.hasIntersection() && li.isProper()) Debug.println(li);
    /**
     *  Always record any non-proper intersections.
     *  If includeProper is true, record any proper intersections as well.
     */
    if (this.li.hasIntersection()) {
      if (this.recordIsolated) {
        e0.setIsolated(false);
        e1.setIsolated(false);
      }
      // if the segments are adjacent they have at least one trivial
      // intersection,
      // the shared endpoint. Don't bother adding it if it is the
      // only intersection.
      if (!isTrivialIntersection(e0, segIndex0, e1, segIndex1)) {
        this.hasIntersection = true;
        if (this.includeProper || !this.li.isProper()) {
          // Debug.println(li);
          e0.addIntersections(this.li, segIndex0, 0);
          e1.addIntersections(this.li, segIndex1, 1);
        }
        if (this.li.isProper()) {
          final Coordinate intersection = this.li.getIntersection(0);
          addProperIntersection(intersection);
          this.properIntersectionPoint = (Coordinate)intersection.clone();
          this.hasProper = true;
          if (!isBoundaryPoint(this.li, this.bdyNodes)) {
            this.hasProperInterior = true;
          }
        }
        // if (li.isCollinear())
        // hasCollinear = true;
      }
    }
  }

  private void addProperIntersection(final Coordinate coordinate) {
    this.properIntersections.add(new DoubleCoordinates(CoordinatesUtil.get(coordinate), 2));
  }

  /**
   * @return the proper intersection point, or <code>null</code> if none was found
   */
  @Override
  public Coordinate getProperIntersectionPoint() {
    return this.properIntersectionPoint;
  }

  public List<Coordinates> getProperIntersections() {
    return this.properIntersections;
  }

  @Override
  public boolean hasIntersection() {
    return this.hasIntersection;
  }

  /**
   * A proper interior intersection is a proper intersection which is <b>not</b>
   * contained in the set of boundary nodes set for this SegmentIntersector.
   */
  @Override
  public boolean hasProperInteriorIntersection() {
    return this.hasProperInterior;
  }

  /**
   * A proper intersection is an intersection which is interior to at least two
   * line segments.  Note that a proper intersection is not necessarily
   * in the interior of the entire Geometry, since another edge may have
   * an endpoint equal to the intersection, which according to SFS semantics
   * can result in the point being on the Boundary of the Geometry.
   */
  @Override
  public boolean hasProperIntersection() {
    return this.hasProper;
  }

  private boolean isBoundaryPoint(final LineIntersector li, final Collection bdyNodes) {
    for (final Iterator i = bdyNodes.iterator(); i.hasNext();) {
      final Node node = (Node)i.next();
      final Coordinate pt = node.getCoordinate();
      if (li.isIntersection(pt)) {
        return true;
      }
    }
    return false;
  }

  private boolean isBoundaryPoint(final LineIntersector li, final Collection[] bdyNodes) {
    if (bdyNodes == null) {
      return false;
    }
    if (isBoundaryPoint(li, bdyNodes[0])) {
      return true;
    }
    if (isBoundaryPoint(li, bdyNodes[1])) {
      return true;
    }
    return false;
  }

  /**
   * A trivial intersection is an apparent self-intersection which in fact
   * is simply the point shared by adjacent line segments.
   * Note that closed edges require a special check for the point shared by the beginning
   * and end segments.
   */
  private boolean isTrivialIntersection(final Edge e0, final int segIndex0, final Edge e1,
    final int segIndex1) {
    if (e0 == e1) {
      if (this.li.getIntersectionNum() == 1) {
        if (isAdjacentSegments(segIndex0, segIndex1)) {
          return true;
        }
        if (e0.isClosed()) {
          final int maxSegIndex = e0.getNumPoints() - 1;
          if (segIndex0 == 0 && segIndex1 == maxSegIndex || segIndex1 == 0
              && segIndex0 == maxSegIndex) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public void setBoundaryNodes(final Collection bdyNodes0, final Collection bdyNodes1) {
    this.bdyNodes = new Collection[2];
    this.bdyNodes[0] = bdyNodes0;
    this.bdyNodes[1] = bdyNodes1;
  }

}
