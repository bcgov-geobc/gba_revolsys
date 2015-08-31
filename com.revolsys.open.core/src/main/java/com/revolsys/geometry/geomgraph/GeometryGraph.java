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
package com.revolsys.geometry.geomgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.algorithm.BoundaryNodeRule;
import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.geometry.geomgraph.index.EdgeSetIntersector;
import com.revolsys.geometry.geomgraph.index.SegmentIntersector;
import com.revolsys.geometry.geomgraph.index.SimpleMCSweepLineIntersector;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.util.CleanDuplicatePoints;

/**
 * A GeometryGraph is a graph that models a given Geometry
 * @version 1.7
 */
public class GeometryGraph extends PlanarGraph {
  /**
   * This method implements the Boundary Determination Rule
   * for determining whether
   * a component (node or edge) that appears multiple times in elements
   * of a MultiGeometry is in the boundary or the interior of the Geometry
   * <br>
   * The SFS uses the "Mod-2 Rule", which this function implements
   * <br>
   * An alternative (and possibly more intuitive) rule would be
   * the "At Most One Rule":
   *    isInBoundary = (componentCount == 1)
   */
  /*
   * public static boolean isInBoundary(int boundaryCount) { // the "Mod-2 Rule"
   * return boundaryCount % 2 == 1; } public static int determineBoundary(int
   * boundaryCount) { return isInBoundary(boundaryCount) ? Location.BOUNDARY :
   * Location.INTERIOR; }
   */

  public static Location determineBoundary(final BoundaryNodeRule boundaryNodeRule,
    final int boundaryCount) {
    return boundaryNodeRule.isInBoundary(boundaryCount) ? Location.BOUNDARY : Location.INTERIOR;
  }

  private PointOnGeometryLocator areaPtLocator = null;

  private final int argIndex; // the index of this geometry as an argument to a

  private BoundaryNodeRule boundaryNodeRule = null;

  private Collection<Node> boundaryNodes;

  private final Geometry geometry;

  // spatial function (used for labelling)

  private boolean hasTooFewPoints = false;

  private Point invalidPoint = null;

  /**
   * The lineEdgeMap is a map of the linestring components of the
   * parentGeometry to the edges which are derived from them.
   * This is used to efficiently perform findEdge queries
   */
  private final Map<LineString, Edge> lineEdgeMap = new HashMap<>();

  // for use if geometry is not Polygonal
  private final PointLocator ptLocator = new PointLocator();

  /**
   * If this flag is true, the Boundary Determination Rule will used when deciding
   * whether nodes are in the boundary or not
   */
  private boolean useBoundaryDeterminationRule = true;

  public GeometryGraph(final int argIndex, final Geometry geometry) {
    this(argIndex, geometry, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE);
  }

  public GeometryGraph(final int argIndex, final Geometry geometry,
    final BoundaryNodeRule boundaryNodeRule) {
    this.argIndex = argIndex;
    this.geometry = geometry;
    this.boundaryNodeRule = boundaryNodeRule;
    if (geometry != null) {
      add(geometry);
    }
  }

  private void add(final Geometry geometry) {
    if (!geometry.isEmpty()) {
      if (geometry instanceof Polygon) {
        addPolygon((Polygon)geometry);
      } else if (geometry instanceof LineString) {
        addLineString((LineString)geometry);
      } else if (geometry instanceof Point) {
        addPoint((Point)geometry);
      } else if (geometry instanceof MultiPoint) {
        addCollection((MultiPoint)geometry);
      } else if (geometry instanceof MultiLineString) {
        addCollection((MultiLineString)geometry);
      } else if (geometry instanceof MultiPolygon) {
        this.useBoundaryDeterminationRule = false;
        addCollection((MultiPolygon)geometry);
      } else if (geometry instanceof GeometryCollection) {
        addCollection((GeometryCollection)geometry);
      } else {
        throw new UnsupportedOperationException(geometry.getClass().getName());
      }
    }
  }

  private void addCollection(final GeometryCollection gc) {
    for (int i = 0; i < gc.getGeometryCount(); i++) {
      final Geometry geometry = gc.getGeometry(i);
      add(geometry);
    }
  }

  /**
   * Add an Edge computed externally.  The label on the Edge is assumed
   * to be correct.
   */
  public void addEdge(final Edge edge) {
    insertEdge(edge);
    // insert the endpoint as a node, to mark that it is on the boundary
    insertPoint(this.argIndex, edge.getCoordinate(0), Location.BOUNDARY);
    insertPoint(this.argIndex, edge.getCoordinate(edge.getNumPoints() - 1), Location.BOUNDARY);
  }

  private void addLineString(final LineString line) {
    final LineString cleanLine = CleanDuplicatePoints.clean(line);

    if (cleanLine.getVertexCount() < 2 || cleanLine.isEmpty()) {
      this.hasTooFewPoints = true;
      this.invalidPoint = cleanLine.getPoint(0);
      return;
    } else {
      // add the edge for the LineString
      // line edges do not have locations for their left and right sides
      final Edge e = new Edge(cleanLine, new Label(this.argIndex, Location.INTERIOR));
      this.lineEdgeMap.put(line, e);
      insertEdge(e);
      /**
       * Add the boundary points of the LineString, if any.
       * Even if the LineString is closed, add both points as if they were endpoints.
       * This allows for the case that the node already exists and is a boundary point.
       */
      insertBoundaryPoint(this.argIndex, cleanLine.getPoint(0));
      insertBoundaryPoint(this.argIndex, cleanLine.getPoint(cleanLine.getVertexCount() - 1));
    }
  }

  /**
   * Add a point computed externally.  The point is assumed to be a
   * Point Geometry part, which has a location of INTERIOR.
   */
  public void addPoint(final Point pt) {
    insertPoint(this.argIndex, pt, Location.INTERIOR);
  }

  private void addPolygon(final Polygon p) {
    addPolygonRing(p.getShell(), Location.EXTERIOR, Location.INTERIOR);

    for (int i = 0; i < p.getHoleCount(); i++) {
      final LinearRing hole = p.getHole(i);

      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CW)
      addPolygonRing(hole, Location.INTERIOR, Location.EXTERIOR);
    }
  }

  /**
   * Adds a polygon ring to the graph.
   * Empty rings are ignored.
   *
   * The left and right topological location arguments assume that the ring is oriented CW.
   * If the ring is in the opposite orientation,
   * the left and right locations must be interchanged.
   */
  private void addPolygonRing(final LinearRing ring, final Location cwLeft,
    final Location cwRight) {
    // don't bother adding empty holes
    if (ring.isEmpty()) {
      return;
    }
    final LineString coordinatesList = CleanDuplicatePoints.clean(ring);

    if (coordinatesList.getVertexCount() < 4) {
      this.hasTooFewPoints = true;
      this.invalidPoint = coordinatesList.getPoint(0);
      return;
    }

    Location left = cwLeft;
    Location right = cwRight;
    if (ring.isCounterClockwise()) {
      left = cwRight;
      right = cwLeft;
    }
    final Edge e = new Edge(coordinatesList,
      new Label(this.argIndex, Location.BOUNDARY, left, right));
    this.lineEdgeMap.put(ring, e);

    insertEdge(e);
    // insert the endpoint as a node, to mark that it is on the boundary
    insertPoint(this.argIndex, coordinatesList.getPoint(0), Location.BOUNDARY);
  }

  /**
   * Add a node for a self-intersection.
   * If the node is a potential boundary node (e.g. came from an edge which
   * is a boundary) then insert it as a potential boundary node.
   * Otherwise, just add it as a regular node.
   */
  private void addSelfIntersectionNode(final int argIndex, final Point coord, final Location loc) {
    // if this node is already a boundary node, don't change it
    if (isBoundaryNode(argIndex, coord)) {
      return;
    }
    if (loc == Location.BOUNDARY && this.useBoundaryDeterminationRule) {
      insertBoundaryPoint(argIndex, coord);
    } else {
      insertPoint(argIndex, coord, loc);
    }
  }

  private void addSelfIntersectionNodes(final int argIndex) {
    for (final Edge e : this.edges) {
      final Location eLoc = e.getLabel().getLocation(argIndex);
      for (final EdgeIntersection ei : e.getEdgeIntersectionList()) {
        addSelfIntersectionNode(argIndex, ei.coord, eLoc);
      }
    }
  }

  public SegmentIntersector computeEdgeIntersections(final GeometryGraph g,
    final LineIntersector li, final boolean includeProper) {
    final SegmentIntersector si = new SegmentIntersector(li, includeProper, true);
    si.setBoundaryNodes(this.getBoundaryNodes(), g.getBoundaryNodes());

    final EdgeSetIntersector esi = createEdgeSetIntersector();
    esi.computeIntersections(this.edges, g.edges, si);
    /*
     * for (Iterator i = g.edges.iterator(); i.hasNext();) { Edge e = (Edge)
     * i.next(); Debug.print(e.getEdgeIntersectionList()); }
     */
    return si;
  }

  /**
   * Compute self-nodes, taking advantage of the Geometry type to
   * minimize the number of intersection tests.  (E.g. rings are
   * not tested for self-intersection, since they are assumed to be valid).
   * @param li the LineIntersector to use
   * @param computeRingSelfNodes if <false>, intersection checks are optimized to not test rings for self-intersection
   * @return the SegmentIntersector used, containing information about the intersections found
   */
  public SegmentIntersector computeSelfNodes(final LineIntersector li,
    final boolean computeRingSelfNodes) {
    final SegmentIntersector si = new SegmentIntersector(li, true, false);
    final EdgeSetIntersector esi = createEdgeSetIntersector();
    // optimized test for Polygons and Rings
    if (!computeRingSelfNodes && (this.geometry instanceof LinearRing
      || this.geometry instanceof Polygon || this.geometry instanceof MultiPolygon)) {
      esi.computeIntersections(this.edges, si, false);
    } else {
      esi.computeIntersections(this.edges, si, true);
    }
    // System.out.println("SegmentIntersector # tests = " + si.numTests);
    addSelfIntersectionNodes(this.argIndex);
    return si;
  }

  public void computeSplitEdges(final List<Edge> edgelist) {
    for (final Edge edge : this.edges) {
      final EdgeIntersectionList edgeIntersectionList = edge.getEdgeIntersectionList();
      edgeIntersectionList.addSplitEdges(edgelist);
    }
  }

  private EdgeSetIntersector createEdgeSetIntersector() {
    // various options for computing intersections, from slowest to fastest

    // private EdgeSetIntersector esi = new SimpleEdgeSetIntersector();
    // private EdgeSetIntersector esi = new MonotoneChainIntersector();
    // private EdgeSetIntersector esi = new NonReversingChainIntersector();
    // private EdgeSetIntersector esi = new SimpleSweepLineIntersector();
    // private EdgeSetIntersector esi = new MCSweepLineIntersector();

    // return new SimpleEdgeSetIntersector();
    return new SimpleMCSweepLineIntersector();
  }

  public Edge findEdge(final LineString line) {
    return this.lineEdgeMap.get(line);
  }

  public BoundaryNodeRule getBoundaryNodeRule() {
    return this.boundaryNodeRule;
  }

  public Collection<Node> getBoundaryNodes() {
    if (this.boundaryNodes == null) {
      final NodeMap nodes = getNodeMap();
      this.boundaryNodes = nodes.getBoundaryNodes(this.argIndex);
    }
    return this.boundaryNodes;
  }

  public Point[] getBoundaryPoints() {
    final Collection<Node> nodes = getBoundaryNodes();
    final Point[] points = new Point[nodes.size()];
    int i = 0;
    for (final Node node : nodes) {
      points[i++] = node.getCoordinate().clonePoint();
    }
    return points;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public Point getInvalidPoint() {
    return this.invalidPoint;
  }

  /**
   * This constructor is used by clients that wish to add Edges explicitly,
   * rather than adding a Geometry.  (An example is Buffer).
   */
  // no longer used
  // public GeometryGraph(int argIndex, PrecisionModel precisionModel, int SRID)
  // {
  // this(argIndex, null);
  // this.precisionModel = precisionModel;
  // this.SRID = SRID;
  // }
  // public PrecisionModel getPrecisionModel()
  // {
  // return precisionModel;
  // }
  // public int getSRID() { return SRID; }

  public boolean hasTooFewPoints() {
    return this.hasTooFewPoints;
  }

  /**
   * Adds candidate boundary points using the current {@link BoundaryNodeRule}.
   * This is used to add the boundary
   * points of dim-1 geometries (Curves/MultiCurves).
   */
  private void insertBoundaryPoint(final int argIndex, final Point coord) {
    final NodeMap nodes = getNodeMap();
    final Node n = nodes.addNode(coord);
    // nodes always have labels
    final Label lbl = n.getLabel();
    // the new point to insert is on a boundary
    int boundaryCount = 1;
    // determine the current location for the point (if any)
    Location loc = Location.NONE;
    loc = lbl.getLocation(argIndex, Position.ON);
    if (loc == Location.BOUNDARY) {
      boundaryCount++;
    }

    // determine the boundary status of the point according to the Boundary
    // Determination Rule
    final Location newLoc = determineBoundary(this.boundaryNodeRule, boundaryCount);
    lbl.setLocation(argIndex, newLoc);
  }

  private void insertPoint(final int argIndex, final Point coord, final Location onLocation) {
    final NodeMap nodes = getNodeMap();
    final Node n = nodes.addNode(coord);
    final Label lbl = n.getLabel();
    if (lbl == null) {
      n.label = new Label(argIndex, onLocation);
    } else {
      lbl.setLocation(argIndex, onLocation);
    }
  }

  // MD - experimental for now
  /**
   * Determines the {@link Location} of the given {@link Coordinates}
   * in this geometry.
   *
   * @param p the point to test
   * @return the location of the point in the geometry
   */
  public Location locate(final Point pt) {
    if (this.geometry instanceof Polygonal && this.geometry.getGeometryCount() > 50) {
      // lazily init point locator
      if (this.areaPtLocator == null) {
        this.areaPtLocator = new IndexedPointInAreaLocator(this.geometry);
      }
      return this.areaPtLocator.locate(pt);
    }
    return this.ptLocator.locate(pt, this.geometry);
  }
}
