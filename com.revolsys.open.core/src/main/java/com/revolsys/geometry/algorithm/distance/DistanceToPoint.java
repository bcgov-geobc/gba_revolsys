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
package com.revolsys.geometry.algorithm.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * Computes the Euclidean distance (L2 metric) from a {@link Coordinates} to a {@link Geometry}.
 * Also computes two points on the geometry which are separated by the distance found.
 */
public class DistanceToPoint {

  public static void computeDistance(final Geometry geom, final Point pt,
    final PointPairDistance ptDist) {
    if (geom instanceof LineString) {
      final LineString line = (LineString)geom;
      computeDistance(line, pt, ptDist);
    } else if (geom instanceof Polygon) {
      final Polygon polygon = (Polygon)geom;
      computeDistance(polygon, pt, ptDist);
    } else if (geom instanceof GeometryCollection) {
      final GeometryCollection gc = (GeometryCollection)geom;
      for (int i = 0; i < gc.getGeometryCount(); i++) {
        final Geometry g = gc.getGeometry(i);
        computeDistance(g, pt, ptDist);
      }
    } else { // assume geom is Point
      ptDist.setMinimum(geom.getPoint(), pt);
    }
  }

  public static void computeDistance(final LineSegment segment, final Point pt,
    final PointPairDistance ptDist) {
    final Point closestPt = segment.closestPoint(pt);
    ptDist.setMinimum(closestPt, pt);
  }

  public static void computeDistance(final LineString line, final Point pt,
    final PointPairDistance ptDist) {
    for (final Segment segment : line.segments()) {
      final Point closestPt = segment.closestPoint(pt);
      ptDist.setMinimum(closestPt, pt);
    }
  }

  public static void computeDistance(final Polygon poly, final Point pt,
    final PointPairDistance ptDist) {
    computeDistance(poly.getShell(), pt, ptDist);
    for (int i = 0; i < poly.getHoleCount(); i++) {
      computeDistance(poly.getHole(i), pt, ptDist);
    }
  }

  public DistanceToPoint() {
  }
}
