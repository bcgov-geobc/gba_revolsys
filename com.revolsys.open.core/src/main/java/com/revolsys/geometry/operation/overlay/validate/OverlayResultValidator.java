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
package com.revolsys.geometry.operation.overlay.validate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.operation.overlay.OverlayOp;
import com.revolsys.geometry.operation.overlay.snap.GeometrySnapper;

/**
 * Validates that the result of an overlay operation is
 * geometrically correct, within a determined tolerance.
 * Uses fuzzy point location to find points which are
 * definitely in either the interior or exterior of the result
 * geometry, and compares these results with the expected ones.
 * <p>
 * This algorithm is only useful where the inputs are polygonal.
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 *
 * @author Martin Davis
 * @version 1.7
 * @see OverlayOp
 */
public class OverlayResultValidator {
  private static final double TOLERANCE = 0.000001;

  private static double computeBoundaryDistanceTolerance(final Geometry g0, final Geometry g1) {
    return Math.min(GeometrySnapper.computeSizeBasedSnapTolerance(g0),
      GeometrySnapper.computeSizeBasedSnapTolerance(g1));
  }

  private static boolean hasLocation(final Location[] location, final Location loc) {
    for (int i = 0; i < 3; i++) {
      if (location[i] == loc) {
        return true;
      }
    }
    return false;
  }

  public static boolean isValid(final Geometry a, final Geometry b, final int overlayOp,
    final Geometry result) {
    final OverlayResultValidator validator = new OverlayResultValidator(a, b, result);
    return validator.isValid(overlayOp);
  }

  private double boundaryDistanceTolerance = TOLERANCE;

  private final Geometry[] geom;

  private Point invalidLocation = null;

  private final Location[] location = new Location[3];

  private final FuzzyPointLocator[] locFinder;

  private final List<Point> testCoords = new ArrayList<>();

  public OverlayResultValidator(final Geometry a, final Geometry b, final Geometry result) {
    /**
     * The tolerance to use needs to depend on the size of the geometries.
     * It should not be more precise than double-precision can support.
     */
    this.boundaryDistanceTolerance = computeBoundaryDistanceTolerance(a, b);
    this.geom = new Geometry[] {
      a, b, result
    };
    this.locFinder = new FuzzyPointLocator[] {
      new FuzzyPointLocator(this.geom[0], this.boundaryDistanceTolerance),
      new FuzzyPointLocator(this.geom[1], this.boundaryDistanceTolerance),
      new FuzzyPointLocator(this.geom[2], this.boundaryDistanceTolerance)
    };
  }

  private void addTestPts(final Geometry g) {
    final OffsetPointGenerator ptGen = new OffsetPointGenerator(g);
    this.testCoords.addAll(ptGen.getPoints(5 * this.boundaryDistanceTolerance));
  }

  private boolean checkValid(final int overlayOp) {
    for (int i = 0; i < this.testCoords.size(); i++) {
      final Point pt = this.testCoords.get(i);
      if (!checkValid(overlayOp, pt)) {
        this.invalidLocation = pt;
        return false;
      }
    }
    return true;
  }

  private boolean checkValid(final int overlayOp, final Point pt) {
    this.location[0] = this.locFinder[0].getLocation(pt);
    this.location[1] = this.locFinder[1].getLocation(pt);
    this.location[2] = this.locFinder[2].getLocation(pt);

    /**
     * If any location is on the Boundary, can't deduce anything, so just return true
     */
    if (hasLocation(this.location, Location.BOUNDARY)) {
      return true;
    }

    return isValidResult(overlayOp, this.location);
  }

  public Point getInvalidLocation() {
    return this.invalidLocation;
  }

  public boolean isValid(final int overlayOp) {
    addTestPts(this.geom[0]);
    addTestPts(this.geom[1]);
    final boolean isValid = checkValid(overlayOp);

    /*
     * System.out.println("OverlayResultValidator: " + isValid);
     * System.out.println("G0"); System.out.println(geom[0]);
     * System.out.println("G1"); System.out.println(geom[1]);
     * System.out.println("Result"); System.out.println(geom[2]);
     */

    return isValid;
  }

  private boolean isValidResult(final int overlayOp, final Location[] location) {
    final boolean expectedInterior = OverlayOp.isResultOfOp(location[0], location[1], overlayOp);

    final boolean resultInInterior = location[2] == Location.INTERIOR;
    // MD use simpler: boolean isValid = (expectedInterior == resultInInterior);
    final boolean isValid = !(expectedInterior ^ resultInInterior);

    if (!isValid) {
      reportResult(overlayOp, location, expectedInterior);
    }

    return isValid;
  }

  private void reportResult(final int overlayOp, final Location[] location,
    final boolean expectedInterior) {
    System.out.println("Overlay result invalid - A:" + Location.toLocationSymbol(location[0])
      + " B:" + Location.toLocationSymbol(location[1]) + " expected:"
      + (expectedInterior ? 'i' : 'e') + " actual:" + Location.toLocationSymbol(location[2]));
  }
}
