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

package com.revolsys.geometry.operation.overlay.snap;

import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.util.GeometryTransformer;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Snaps the vertices and segments of a {@link Geometry}
 * to another Geometry's vertices.
 * A snap distance tolerance is used to control where snapping is performed.
 * Snapping one geometry to another can improve
 * robustness for overlay operations by eliminating
 * nearly-coincident edges
 * (which cause problems during noding and intersection calculation).
 * It can also be used to eliminate artifacts such as narrow slivers, spikes and gores.
 * <p>
 * Too much snapping can result in invalid topology
 * being created, so the number and location of snapped vertices
 * is decided using heuristics to determine when it
 * is safe to snap.
 * This can result in some potential snaps being omitted, however.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class GeometrySnapper {
  private static final double SNAP_PRECISION_FACTOR = 1e-9;

  /**
   * Estimates the snap tolerance for a Geometry, taking into account its precision model.
   *
   * @param g a Geometry
   * @return the estimated snap tolerance
   */
  public static double computeOverlaySnapTolerance(final Geometry g) {
    double snapTolerance = computeSizeBasedSnapTolerance(g);

    /**
     * Overlay is carried out in the precision model
     * of the two inputs.
     * If this precision model is of type FIXED, then the snap tolerance
     * must reflect the precision grid size.
     * Specifically, the snap tolerance should be at least
     * the distance from a corner of a precision grid cell
     * to the centre point of the cell.
     */
    final GeometryFactory geometryFactory = g.getGeometryFactory();
    if (!geometryFactory.isFloating()) {
      final double fixedSnapTol = 1 / geometryFactory.getScale(0) * 2 / 1.415;
      if (fixedSnapTol > snapTolerance) {
        snapTolerance = fixedSnapTol;
      }
    }
    return snapTolerance;
  }

  public static double computeOverlaySnapTolerance(final Geometry g0, final Geometry g1) {
    return Math.min(computeOverlaySnapTolerance(g0), computeOverlaySnapTolerance(g1));
  }

  public static double computeSizeBasedSnapTolerance(final Geometry g) {
    final BoundingBox env = g.getBoundingBox();
    final double minDimension = Math.min(env.getHeight(), env.getWidth());
    final double snapTol = minDimension * SNAP_PRECISION_FACTOR;
    return snapTol;
  }

  /**
   * Snaps two geometries together with a given tolerance.
   *
   * @param g0 a geometry to snap
   * @param g1 a geometry to snap
   * @param snapTolerance the tolerance to use
   * @return the snapped geometries
   */
  public static Geometry[] snap(final Geometry g0, final Geometry g1, final double snapTolerance) {
    final Geometry[] snapGeom = new Geometry[2];
    final GeometrySnapper snapper0 = new GeometrySnapper(g0);
    snapGeom[0] = snapper0.snapTo(g1, snapTolerance);

    /**
     * Snap the second geometry to the snapped first geometry
     * (this strategy minimizes the number of possible different points in the result)
     */
    final GeometrySnapper snapper1 = new GeometrySnapper(g1);
    snapGeom[1] = snapper1.snapTo(snapGeom[0], snapTolerance);

    // System.out.println(snap[0]);
    // System.out.println(snap[1]);
    return snapGeom;
  }

  /**
   * Snaps a geometry to itself.
   * Allows optionally cleaning the result to ensure it is
   * topologically valid
   * (which fixes issues such as topology collapses in polygonal inputs).
   * <p>
   * Snapping a geometry to itself can remove artifacts such as very narrow slivers, gores and spikes.
   *
   *@param geom the geometry to snap
   *@param snapTolerance the snapping tolerance
   *@param cleanResult whether the result should be made valid
   * @return a new snapped Geometry
   */
  public static Geometry snapToSelf(final Geometry geom, final double snapTolerance,
    final boolean cleanResult) {
    final GeometrySnapper snapper0 = new GeometrySnapper(geom);
    return snapper0.snapToSelf(snapTolerance, cleanResult);
  }

  private final Geometry srcGeom;

  /**
   * Creates a new snapper acting on the given geometry
   *
   * @param srcGeom the geometry to snap
   */
  public GeometrySnapper(final Geometry srcGeom) {
    this.srcGeom = srcGeom;
  }

  private Point[] extractTargetCoordinates(final Geometry geometry) {
    // TODO: should do this more efficiently. Use CoordSeq filter to get points,
    // KDTree for uniqueness & queries
    final Set<Point> points = new TreeSet<Point>();
    for (final Vertex vertex : geometry.vertices()) {
      points.add(vertex.clonePoint());
    }
    return points.toArray(new Point[points.size()]);
  }

  /**
   * Snaps the vertices in the component {@link LineString}s
   * of the source geometry
   * to the vertices of the given snap geometry.
   *
   * @param snapGeom a geometry to snap the source to
   * @return a new snapped Geometry
   */
  public Geometry snapTo(final Geometry snapGeom, final double snapTolerance) {
    final Point[] snapPts = extractTargetCoordinates(snapGeom);

    final SnapTransformer snapTrans = new SnapTransformer(snapTolerance, snapPts);
    return snapTrans.transform(this.srcGeom);
  }

  /**
   * Snaps the vertices in the component {@link LineString}s
   * of the source geometry
   * to the vertices of the same geometry.
   * Allows optionally cleaning the result to ensure it is
   * topologically valid
   * (which fixes issues such as topology collapses in polygonal inputs).
   *
   *@param snapTolerance the snapping tolerance
   *@param cleanResult whether the result should be made valid
   * @return a new snapped Geometry
   */
  public Geometry snapToSelf(final double snapTolerance, final boolean cleanResult) {
    final Point[] snapPts = extractTargetCoordinates(this.srcGeom);

    final SnapTransformer snapTrans = new SnapTransformer(snapTolerance, snapPts, true);
    final Geometry snappedGeom = snapTrans.transform(this.srcGeom);
    Geometry result = snappedGeom;
    if (cleanResult && result instanceof Polygonal) {
      // TODO: use better cleaning approach
      result = snappedGeom.buffer(0);
    }
    return result;
  }

}

class SnapTransformer extends GeometryTransformer {
  private boolean isSelfSnap = false;

  private final Point[] snapPts;

  private final double snapTolerance;

  SnapTransformer(final double snapTolerance, final Point[] snapPts) {
    this.snapTolerance = snapTolerance;
    this.snapPts = snapPts;
  }

  SnapTransformer(final double snapTolerance, final Point[] snapPts, final boolean isSelfSnap) {
    this.snapTolerance = snapTolerance;
    this.snapPts = snapPts;
    this.isSelfSnap = isSelfSnap;
  }

  private Point[] snapLine(final LineString srcPts, final Point[] snapPts) {
    final LineStringSnapper snapper = new LineStringSnapper(srcPts, this.snapTolerance);
    snapper.setAllowSnappingToSourceVertices(this.isSelfSnap);
    return snapper.snapTo(snapPts);
  }

  @Override
  protected LineString transformCoordinates(final LineString coords, final Geometry parent) {
    final Point[] newPts = snapLine(coords, this.snapPts);
    return new LineStringDouble(newPts);
  }

  @Override
  protected Geometry transformPoint(final Point point, final Geometry parent) {
    final Point snapVert = LineStringSnapper.findSnapForVertex(point, this.snapPts,
      this.snapTolerance);
    if (snapVert == null) {
      return point;
    } else {
      return snapVert;
    }
  }
}
