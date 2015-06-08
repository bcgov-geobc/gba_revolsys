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
package com.revolsys.gis.model.geometry.operation.chain;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;

//import com.vividsolutions.jts.util.Debug;

/**
 * Detects and records an intersection between two {@link SegmentString}s,
 * if one exists.  Only a single intersection is recorded.
 * This strategy can be configured to search for proper intersections.
 * In this case, the presence of <i>any</i> kind of intersection will still be recorded,
 * but searching will continue until either a proper intersection has been found
 * or no intersections are detected.
 *
 * @version 1.7
 */
public class SegmentIntersectionDetector implements SegmentIntersector {
  private final LineIntersector li;

  private boolean findProper = false;

  private boolean findAllTypes = false;

  private boolean hasIntersection = false;

  private boolean hasProperIntersection = false;

  private boolean hasNonProperIntersection = false;

  private Coordinates intPt = null;

  private Coordinates[] intSegments = null;

  /**
   * Creates an intersection finder
   *
   * @param li the LineIntersector to use
   */
  public SegmentIntersectionDetector(final LineIntersector li) {
    this.li = li;
  }

  /**
   * Gets the computed location of the intersection.
   * Due to round-off, the location may not be exact.
   *
   * @return the coordinate for the intersection location
   */
  public Coordinates getIntersection() {
    return this.intPt;
  }

  /**
   * Gets the endpoints of the intersecting segments.
   *
   * @return an array of the segment endpoints (p00, p01, p10, p11)
   */
  public Coordinates[] getIntersectionSegments() {
    return this.intSegments;
  }

  /**
   * Tests whether an intersection was found.
   *
   * @return true if an intersection was found
   */
  public boolean hasIntersection() {
    return this.hasIntersection;
  }

  /**
   * Tests whether a non-proper intersection was found.
   *
   * @return true if a non-proper intersection was found
   */
  public boolean hasNonProperIntersection() {
    return this.hasNonProperIntersection;
  }

  /**
   * Tests whether a proper intersection was found.
   *
   * @return true if a proper intersection was found
   */
  public boolean hasProperIntersection() {
    return this.hasProperIntersection;
  }

  @Override
  public boolean isDone() {
    /**
     * If finding all types, we can stop
     * when both possible types have been found.
     */
    if (this.findAllTypes) {
      return this.hasProperIntersection && this.hasNonProperIntersection;
    }

    /**
     * If searching for a proper intersection, only stop if one is found
     */
    if (this.findProper) {
      return this.hasProperIntersection;
    }
    return this.hasIntersection;
  }

  /**
   * This method is called by clients
   * of the {@link SegmentIntersector} class to process
   * intersections for two segments of the {@link SegmentString}s being intersected.
   * Note that some clients (such as {@link MonotoneChain}s) may optimize away
   * this call for segment pairs which they have determined do not intersect
   * (e.g. by an disjoint envelope test).
   */
  @Override
  public void processIntersections(final SegmentString e0, final int segIndex0,
    final SegmentString e1, final int segIndex1) {
    // don't bother intersecting a segment with itself
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }

    final Coordinates p00 = e0.getCoordinate(segIndex0);
    final Coordinates p01 = e0.getCoordinate(segIndex0 + 1);
    final Coordinates p10 = e1.getCoordinate(segIndex1);
    final Coordinates p11 = e1.getCoordinate(segIndex1 + 1);

    this.li.computeIntersection(p00, p01, p10, p11);
    // if (li.hasIntersection() && li.isProper()) Debug.println(li);

    if (this.li.hasIntersection()) {
      // System.out.println(li);

      // record intersection info
      this.hasIntersection = true;

      final boolean isProper = this.li.isProper();
      if (isProper) {
        this.hasProperIntersection = true;
      }
      if (!isProper) {
        this.hasNonProperIntersection = true;
      }

      /**
       * If this is the kind of intersection we are searching for
       * OR no location has yet been recorded
       * save the location data
       */
      boolean saveLocation = true;
      if (this.findProper && !isProper) {
        saveLocation = false;
      }

      if (this.intPt == null || saveLocation) {

        // record intersection location (approximate)
        this.intPt = this.li.getIntersection(0);

        // record intersecting segments
        this.intSegments = new Coordinates[4];
        this.intSegments[0] = p00;
        this.intSegments[1] = p01;
        this.intSegments[2] = p10;
        this.intSegments[3] = p11;
      }
    }
  }

  public void setFindAllIntersectionTypes(final boolean findAllTypes) {
    this.findAllTypes = findAllTypes;
  }

  public void setFindProper(final boolean findProper) {
    this.findProper = findProper;
  }
}
