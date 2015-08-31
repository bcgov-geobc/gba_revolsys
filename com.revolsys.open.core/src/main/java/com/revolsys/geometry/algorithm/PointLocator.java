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
package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

/**
 * Computes the topological ({@link Location})
 * of a single point to a {@link Geometry}.
 * A {@link BoundaryNodeRule} may be specified
 * to control the evaluation of whether the point lies on the boundary or not
 * The default rule is to use the the <i>SFS Boundary Determination Rule</i>
 * <p>
 * Notes:
 * <ul>
 * <li>{@link LinearRing}s do not enclose any area - points inside the ring are still in the EXTERIOR of the ring.
 * </ul>
 * Instances of this class are not reentrant.
 *
 * @version 1.7
 */
public class PointLocator {
  private boolean isIn; // true if the point lies in or on any Geometry element

  public PointLocator() {
  }

  private int computeLocation(final Point point, final Geometry geometry) {
    int numBoundaries = 0;
    if (geometry instanceof Point) {
      final Location location = point.locate((Point)geometry);
      return updateLocationInfo(location);
    } else if (geometry instanceof LineString) {
      final Location location = ((LineString)geometry).locate(point);
      return updateLocationInfo(location);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final Location location = polygon.locate(point);
      return updateLocationInfo(location);
    } else {
      for (final Geometry part : geometry.geometries()) {
        if (part != geometry) {
          numBoundaries += computeLocation(point, part);
        }
      }
    }
    return numBoundaries;
  }

  /**
   * Computes the topological relationship ({@link Location}) of a single point
   * to a Geometry.
   * It handles both single-element
   * and multi-element Geometries.
   * The algorithm for multi-part Geometries
   * takes into account the SFS Boundary Determination Rule.
   *
   * @return the {@link Location} of the point relative to the input Geometry
   */
  public Location locate(final Point point, final Geometry geometry) {
    if (geometry.isEmpty()) {
      return Location.EXTERIOR;
    } else if (geometry instanceof LineString) {
      return ((LineString)geometry).locate(point);
    } else if (geometry instanceof Polygon) {
      return ((Polygon)geometry).locate(point);
    }

    this.isIn = false;
    final int boundaryCount = computeLocation(point, geometry);
    if (boundaryCount % 2 == 1) {
      return Location.BOUNDARY;
    } else if (boundaryCount > 0 || this.isIn) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  private int updateLocationInfo(final Location loc) {
    if (loc == Location.INTERIOR) {
      this.isIn = true;
    } else if (loc == Location.BOUNDARY) {
      return 1;
    }
    return 0;
  }

}
