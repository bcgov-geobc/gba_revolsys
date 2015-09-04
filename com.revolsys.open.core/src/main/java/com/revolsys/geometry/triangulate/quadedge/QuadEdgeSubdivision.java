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

package com.revolsys.geometry.triangulate.quadedge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.CoordinateList;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * A class that contains the {@link QuadEdge}s representing a planar
 * subdivision that models a triangulation.
 * The subdivision is constructed using the
 * quadedge algebra defined in the classs {@link QuadEdge}.
 * All metric calculations
 * are done in the {@link Vertex} class.
 * In addition to a triangulation, subdivisions
 * support extraction of Voronoi diagrams.
 * This is easily accomplished, since the Voronoi diagram is the dual
 * of the Delaunay triangulation.
 * <p>
 * Subdivisions can be provided with a tolerance value. Inserted vertices which
 * are closer than this value to vertices already in the subdivision will be
 * ignored. Using a suitable tolerance value can prevent robustness failures
 * from happening during Delaunay triangulation.
 * <p>
 * Subdivisions maintain a <b>frame</b> triangle around the client-created
 * edges. The frame is used to provide a bounded "container" for all edges
 * within a TIN. Normally the frame edges, frame connecting edges, and frame
 * triangles are not included in client processing.
 *
 * @author David Skea
 * @author Martin Davis
 */
public class QuadEdgeSubdivision {
  /**
   * A TriangleVisitor which computes and sets the
   * circumcentre as the origin of the dual
   * edges originating in each triangle.
   *
   * @author mbdavis
   *
   */
  private static class TriangleCircumcentreVisitor implements TriangleVisitor {
    public TriangleCircumcentreVisitor() {
    }

    @Override
    public void visit(final QuadEdge[] triEdges) {
      final Point a = triEdges[0].orig().getCoordinate();
      final Point b = triEdges[1].orig().getCoordinate();
      final Point c = triEdges[2].orig().getCoordinate();

      // TODO: choose the most accurate circumcentre based on the edges
      final Point cc = Triangle.circumcentre(a, b, c);
      final Vertex ccVertex = new Vertex(cc);
      // save the circumcentre as the origin for the dual edges originating in
      // this triangle
      for (int i = 0; i < 3; i++) {
        triEdges[i].rot().setOrig(ccVertex);
      }
    }
  }

  private static class TriangleCoordinatesVisitor implements TriangleVisitor {
    private final CoordinateList coordList = new CoordinateList();

    private final List triCoords = new ArrayList();

    public TriangleCoordinatesVisitor() {
    }

    public List getTriangles() {
      return this.triCoords;
    }

    @Override
    public void visit(final QuadEdge[] triEdges) {
      this.coordList.clear();
      for (int i = 0; i < 3; i++) {
        final Vertex v = triEdges[i].orig();
        this.coordList.add(v.getCoordinate());
      }
      if (this.coordList.size() > 0) {
        this.coordList.closeRing();
        final Point[] pts = this.coordList.toCoordinateArray();
        if (pts.length != 4) {
          // checkTriangleSize(pts);
          return;
        }

        this.triCoords.add(pts);
      }
    }
  }

  // debugging only - preserve current subdiv statically
  // private static QuadEdgeSubdivision currentSubdiv;

  private static class TriangleEdgesListVisitor implements TriangleVisitor {
    private final List triList = new ArrayList();

    public List getTriangleEdges() {
      return this.triList;
    }

    @Override
    public void visit(final QuadEdge[] triEdges) {
      this.triList.add(triEdges.clone());
    }
  }

  private static class TriangleVertexListVisitor implements TriangleVisitor {
    private final List triList = new ArrayList();

    public List getTriangleVertices() {
      return this.triList;
    }

    @Override
    public void visit(final QuadEdge[] triEdges) {
      this.triList.add(new Vertex[] {
        triEdges[0].orig(), triEdges[1].orig(), triEdges[2].orig()
      });
    }
  }

  private final static double EDGE_COINCIDENCE_TOL_FACTOR = 1000;

  /**
   * Gets the edges for the triangle to the left of the given {@link QuadEdge}.
   *
   * @param startQE
   * @param triEdge
   *
   * @throws IllegalArgumentException
   *           if the edges do not form a triangle
   */
  public static void getTriangleEdges(final QuadEdge startQE, final QuadEdge[] triEdge) {
    triEdge[0] = startQE;
    triEdge[1] = triEdge[0].lNext();
    triEdge[2] = triEdge[1].lNext();
    if (triEdge[2].lNext() != triEdge[0]) {
      throw new IllegalArgumentException("Edges do not form a triangle");
    }
  }

  private final double edgeCoincidenceTolerance;

  private BoundingBoxDoubleGf frameEnv;

  private final Vertex[] frameVertex = new Vertex[3];

  private QuadEdgeLocator locator = null;

  // private Set quadEdges = new HashSet();
  private final List quadEdges = new ArrayList();

  private final QuadEdge startingEdge;

  private final double tolerance;

  /**
   * The quadedges forming a single triangle.
   * Only one visitor is allowed to be active at a
   * time, so this is safe.
   */
  private final QuadEdge[] triEdges = new QuadEdge[3];

  /**
   * Creates a new instance of a quad-edge subdivision based on a frame triangle
   * that encloses a supplied bounding box. A new super-bounding box that
   * contains the triangle is computed and stored.
   *
   * @param env
   *          the bouding box to surround
   * @param tolerance
   *          the tolerance value for determining if two sites are equal
   */
  public QuadEdgeSubdivision(final BoundingBox env, final double tolerance) {
    // currentSubdiv = this;
    this.tolerance = tolerance;
    this.edgeCoincidenceTolerance = tolerance / EDGE_COINCIDENCE_TOL_FACTOR;

    createFrame(env);

    this.startingEdge = initSubdiv();
    this.locator = new LastFoundQuadEdgeLocator(this);
  }

  /**
   * Creates a new QuadEdge connecting the destination of a to the origin of b,
   * in such a way that all three have the same left face after the connection
   * is complete. The quadedge is recorded in the edges list.
   *
   * @param a
   * @param b
   * @return a quadedge
   */
  public QuadEdge connect(final QuadEdge a, final QuadEdge b) {
    final QuadEdge q = QuadEdge.connect(a, b);
    this.quadEdges.add(q);
    return q;
  }

  private void createFrame(final BoundingBox env) {
    final double deltaX = env.getWidth();
    final double deltaY = env.getHeight();
    double offset = 0.0;
    if (deltaX > deltaY) {
      offset = deltaX * 10.0;
    } else {
      offset = deltaY * 10.0;
    }

    this.frameVertex[0] = new Vertex((env.getMaxX() + env.getMinX()) / 2.0, env.getMaxY() + offset);
    this.frameVertex[1] = new Vertex(env.getMinX() - offset, env.getMinY() - offset);
    this.frameVertex[2] = new Vertex(env.getMaxX() + offset, env.getMinY() - offset);

    this.frameEnv = new BoundingBoxDoubleGf(this.frameVertex[0].getCoordinate(),
      this.frameVertex[1].getCoordinate(), this.frameVertex[2].getCoordinate());
  }

  /**
   * Deletes a quadedge from the subdivision. Linked quadedges are updated to
   * reflect the deletion.
   *
   * @param e
   *          the quadedge to delete
   */
  public void delete(final QuadEdge e) {
    QuadEdge.splice(e, e.oPrev());
    QuadEdge.splice(e.sym(), e.sym().oPrev());

    final QuadEdge eSym = e.sym();
    final QuadEdge eRot = e.rot();
    final QuadEdge eRotSym = e.rot().sym();

    // this is inefficient on an ArrayList, but this method should be called
    // infrequently
    this.quadEdges.remove(e);
    this.quadEdges.remove(eSym);
    this.quadEdges.remove(eRot);
    this.quadEdges.remove(eRotSym);

    e.delete();
    eSym.delete();
    eRot.delete();
    eRotSym.delete();
  }

  /**
   * Stores the edges for a visited triangle. Also pushes sym (neighbour) edges
   * on stack to visit later.
   *
   * @param edge
   * @param edgeStack
   * @param includeFrame
   * @return the visited triangle edges
   * or null if the triangle should not be visited (for instance, if it is
   *         outer)
   */
  private QuadEdge[] fetchTriangleToVisit(final QuadEdge edge, final Stack edgeStack,
    final boolean includeFrame, final Set visitedEdges) {
    QuadEdge curr = edge;
    int edgeCount = 0;
    boolean isFrame = false;
    do {
      this.triEdges[edgeCount] = curr;

      if (isFrameEdge(curr)) {
        isFrame = true;
      }

      // push sym edges to visit next
      final QuadEdge sym = curr.sym();
      if (!visitedEdges.contains(sym)) {
        edgeStack.push(sym);
      }

      // mark this edge as visited
      visitedEdges.add(curr);

      edgeCount++;
      curr = curr.lNext();
    } while (curr != edge);

    if (isFrame && !includeFrame) {
      return null;
    }
    return this.triEdges;
  }

  /**
   * Gets the collection of base {@link QuadEdge}s (one for every pair of
   * vertices which is connected).
   *
   * @return a collection of QuadEdges
   */
  public Collection getEdges() {
    return this.quadEdges;
  }

  /**
   * Gets the geometry for the edges in the subdivision as a {@link MultiLineString}
   * containing 2-point lines.
   *
   * @param geomFact the GeometryFactory to use
   * @return a MultiLineString
   */
  public Geometry getEdges(final GeometryFactory geomFact) {
    final List quadEdges = getPrimaryEdges(false);
    final LineString[] edges = new LineString[quadEdges.size()];
    int i = 0;
    for (final Iterator it = quadEdges.iterator(); it.hasNext();) {
      final QuadEdge qe = (QuadEdge)it.next();
      edges[i++] = geomFact.lineString(new Point[] {
        qe.orig().getCoordinate(), qe.dest().getCoordinate()
      });
    }
    return geomFact.multiLineString(edges);
  }

  /**
   * Gets the envelope of the Subdivision (including the frame).
   *
   * @return the envelope
   */
  public BoundingBox getEnvelope() {
    return this.frameEnv;
  }

  /**
   * Gets all primary quadedges in the subdivision.
   * A primary edge is a {@link QuadEdge}
   * which occupies the 0'th position in its array of associated quadedges.
   * These provide the unique geometric edges of the triangulation.
   *
   * @param includeFrame true if the frame edges are to be included
   * @return a List of QuadEdges
   */
  public List getPrimaryEdges(final boolean includeFrame) {
    final List edges = new ArrayList();
    final Stack edgeStack = new Stack();
    edgeStack.push(this.startingEdge);

    final Set visitedEdges = new HashSet();

    while (!edgeStack.empty()) {
      final QuadEdge edge = (QuadEdge)edgeStack.pop();
      if (!visitedEdges.contains(edge)) {
        final QuadEdge priQE = edge.getPrimary();

        if (includeFrame || !isFrameEdge(priQE)) {
          edges.add(priQE);
        }

        edgeStack.push(edge.oNext());
        edgeStack.push(edge.sym().oNext());

        visitedEdges.add(edge);
        visitedEdges.add(edge.sym());
      }
    }
    return edges;
  }

  /**
   * Gets the vertex-equality tolerance value
   * used in this subdivision
   *
   * @return the tolerance value
   */
  public double getTolerance() {
    return this.tolerance;
  }

  /**
   * Gets the coordinates for each triangle in the subdivision as an array.
   *
   * @param includeFrame
   *          true if the frame triangles should be included
   * @return a list of Coordinate[4] representing each triangle
   */
  public List getTriangleCoordinates(final boolean includeFrame) {
    final TriangleCoordinatesVisitor visitor = new TriangleCoordinatesVisitor();
    visitTriangles(visitor, includeFrame);
    return visitor.getTriangles();
  }

  /**
   * Gets a list of the triangles
   * in the subdivision, specified as
   * an array of the primary quadedges around the triangle.
   *
   * @param includeFrame
   *          true if the frame triangles should be included
   * @return a List of QuadEdge[3] arrays
   */
  public List getTriangleEdges(final boolean includeFrame) {
    final TriangleEdgesListVisitor visitor = new TriangleEdgesListVisitor();
    visitTriangles(visitor, includeFrame);
    return visitor.getTriangleEdges();
  }

  /**
   * Gets the geometry for the triangles in a triangulated subdivision as a {@link GeometryCollection}
   * of triangular {@link Polygon}s.
   *
   * @param geomFact the GeometryFactory to use
   * @return a GeometryCollection of triangular Polygons
   */
  public Geometry getTriangles(final GeometryFactory geomFact) {
    final List triPtsList = getTriangleCoordinates(false);
    final Polygon[] tris = new Polygon[triPtsList.size()];
    int i = 0;
    for (final Iterator it = triPtsList.iterator(); it.hasNext();) {
      final Point[] triPt = (Point[])it.next();
      tris[i++] = geomFact.polygon(geomFact.linearRing(triPt));
    }
    return geomFact.geometryCollection(tris);
  }

  /**
   * Gets a list of the triangles in the subdivision,
   * specified as an array of the triangle {@link Vertex}es.
   *
   * @param includeFrame
   *          true if the frame triangles should be included
   * @return a List of Vertex[3] arrays
   */
  public List getTriangleVertices(final boolean includeFrame) {
    final TriangleVertexListVisitor visitor = new TriangleVertexListVisitor();
    visitTriangles(visitor, includeFrame);
    return visitor.getTriangleVertices();
  }

  /**
   * Gets a collection of {@link QuadEdge}s whose origin
   * vertices are a unique set which includes
   * all vertices in the subdivision.
   * The frame vertices can be included if required.
   * <p>
   * This is useful for algorithms which require traversing the
   * subdivision starting at all vertices.
   * Returning a quadedge for each vertex
   * is more efficient than
   * the alternative of finding the actual vertices
   * using {@link #getVertices} and then locating
   * quadedges attached to them.
   *
   * @param includeFrame true if the frame vertices should be included
   * @return a collection of QuadEdge with the vertices of the subdivision as their origins
   */
  public List getVertexUniqueEdges(final boolean includeFrame) {
    final List edges = new ArrayList();
    final Set visitedVertices = new HashSet();
    for (final Iterator i = this.quadEdges.iterator(); i.hasNext();) {
      final QuadEdge qe = (QuadEdge)i.next();
      final Vertex v = qe.orig();
      // System.out.println(v);
      if (!visitedVertices.contains(v)) {
        visitedVertices.add(v);
        if (includeFrame || !isFrameVertex(v)) {
          edges.add(qe);
        }
      }

      /**
       * Inspect the sym edge as well, since it is
       * possible that a vertex is only at the
       * dest of all tracked quadedges.
       */
      final QuadEdge qd = qe.sym();
      final Vertex vd = qd.orig();
      // System.out.println(vd);
      if (!visitedVertices.contains(vd)) {
        visitedVertices.add(vd);
        if (includeFrame || !isFrameVertex(vd)) {
          edges.add(qd);
        }
      }
    }
    return edges;
  }

  /**
   * Gets the unique {@link Vertex}es in the subdivision,
   * including the frame vertices if desired.
   *
   * @param includeFrame
   *          true if the frame vertices should be included
   * @return a collection of the subdivision vertices
   *
   * @see #getVertexUniqueEdges
   */
  public Collection getVertices(final boolean includeFrame) {
    final Set vertices = new HashSet();
    for (final Iterator i = this.quadEdges.iterator(); i.hasNext();) {
      final QuadEdge qe = (QuadEdge)i.next();
      final Vertex v = qe.orig();
      // System.out.println(v);
      if (includeFrame || !isFrameVertex(v)) {
        vertices.add(v);
      }

      /**
       * Inspect the sym edge as well, since it is
       * possible that a vertex is only at the
       * dest of all tracked quadedges.
       */
      final Vertex vd = qe.dest();
      // System.out.println(vd);
      if (includeFrame || !isFrameVertex(vd)) {
        vertices.add(vd);
      }
    }
    return vertices;
  }

  /**
   * Gets the Voronoi cell around a site specified
   * by the origin of a QuadEdge.
   * <p>
   * The userData of the polygon is set to be the {@link Coordinates}
   * of the site.  This allows attaching external
   * data associated with the site to this cell polygon.
   *
   * @param qe a quadedge originating at the cell site
   * @param geomFact a factory for building the polygon
   * @return a polygon indicating the cell extent
   */
  public Polygon getVoronoiCellPolygon(QuadEdge qe, final GeometryFactory geomFact) {
    final List<Point> cellPts = new ArrayList<>();
    final QuadEdge startQE = qe;
    do {
      // Point cc = circumcentre(qe);
      // use previously computed circumcentre
      final Point cc = qe.rot().orig().getCoordinate();
      cellPts.add(cc);

      // move to next triangle CW around vertex
      qe = qe.oPrev();
    } while (qe != startQE);

    final CoordinateList coordList = new CoordinateList();
    coordList.addAll(cellPts, false);
    coordList.closeRing();

    if (coordList.size() < 4) {
      System.out.println(coordList);
      coordList.add(coordList.get(coordList.size() - 1), true);
    }

    final Polygon cellPoly = geomFact.polygon(coordList);

    final Vertex v = startQE.orig();
    cellPoly.setUserData(v.getCoordinate());
    return cellPoly;
  }

  /**
   * Gets a List of {@link Polygon}s for the Voronoi cells
   * of this triangulation.
   * <p>
   * The userData of each polygon is set to be the {@link Coordinates}
   * of the cell site.  This allows easily associating external
   * data associated with the sites to the cells.
   *
   * @param geomFact a geometry factory
   * @return a List of Polygons
   */
  public List getVoronoiCellPolygons(final GeometryFactory geomFact) {
    /*
     * Compute circumcentres of triangles as vertices for dual edges.
     * Precomputing the circumcentres is more efficient, and more importantly
     * ensures that the computed centres are consistent across the Voronoi
     * cells.
     */
    visitTriangles(new TriangleCircumcentreVisitor(), true);

    final List cells = new ArrayList();
    final Collection edges = getVertexUniqueEdges(false);
    for (final Iterator i = edges.iterator(); i.hasNext();) {
      final QuadEdge qe = (QuadEdge)i.next();
      cells.add(getVoronoiCellPolygon(qe, geomFact));
    }
    return cells;
  }

  /**
   * Gets the cells in the Voronoi diagram for this triangulation.
   * The cells are returned as a {@link GeometryCollection} of {@link Polygon}s
   * <p>
   * The userData of each polygon is set to be the {@link Coordinates}
   * of the cell site.  This allows easily associating external
   * data associated with the sites to the cells.
   *
   * @param geomFact a geometry factory
   * @return a GeometryCollection of Polygons
   */
  public Geometry getVoronoiDiagram(final GeometryFactory geomFact) {
    final List<Geometry> vorCells = getVoronoiCellPolygons(geomFact);
    return geomFact.geometryCollection(vorCells);
  }

  private QuadEdge initSubdiv() {
    // build initial subdivision from frame
    final QuadEdge ea = makeEdge(this.frameVertex[0], this.frameVertex[1]);
    final QuadEdge eb = makeEdge(this.frameVertex[1], this.frameVertex[2]);
    QuadEdge.splice(ea.sym(), eb);
    final QuadEdge ec = makeEdge(this.frameVertex[2], this.frameVertex[0]);
    QuadEdge.splice(eb.sym(), ec);
    QuadEdge.splice(ec.sym(), ea);
    return ea;
  }

  /**
   * Inserts a new site into the Subdivision, connecting it to the vertices of
   * the containing triangle (or quadrilateral, if the split point falls on an
   * existing edge).
   * <p>
   * This method does NOT maintain the Delaunay condition. If desired, this must
   * be checked and enforced by the caller.
   * <p>
   * This method does NOT check if the inserted vertex falls on an edge. This
   * must be checked by the caller, since this situation may cause erroneous
   * triangulation
   *
   * @param v
   *          the vertex to insert
   * @return a new quad edge terminating in v
   */
  public QuadEdge insertSite(final Vertex v) {
    QuadEdge e = locate(v);

    if (v.equals(e.orig(), this.tolerance) || v.equals(e.dest(), this.tolerance)) {
      return e; // point already in subdivision.
    }

    // Connect the new point to the vertices of the containing
    // triangle (or quadrilateral, if the new point fell on an
    // existing edge.)
    QuadEdge base = makeEdge(e.orig(), v);
    QuadEdge.splice(base, e);
    final QuadEdge startEdge = base;
    do {
      base = connect(e, base.sym());
      e = base.oPrev();
    } while (e.lNext() != startEdge);

    return startEdge;
  }

  /**
   * Tests whether a QuadEdge is an edge on the border of the frame facets and
   * the internal facets. E.g. an edge which does not itself touch a frame
   * vertex, but which touches an edge which does.
   *
   * @param e
   *          the edge to test
   * @return true if the edge is on the border of the frame
   */
  public boolean isFrameBorderEdge(final QuadEdge e) {
    // MD debugging
    final QuadEdge[] leftTri = new QuadEdge[3];
    getTriangleEdges(e, leftTri);
    // System.out.println(new QuadEdgeTriangle(leftTri).toString());
    final QuadEdge[] rightTri = new QuadEdge[3];
    getTriangleEdges(e.sym(), rightTri);
    // System.out.println(new QuadEdgeTriangle(rightTri).toString());

    // check other vertex of triangle to left of edge
    final Vertex vLeftTriOther = e.lNext().dest();
    if (isFrameVertex(vLeftTriOther)) {
      return true;
    }
    // check other vertex of triangle to right of edge
    final Vertex vRightTriOther = e.sym().lNext().dest();
    if (isFrameVertex(vRightTriOther)) {
      return true;
    }

    return false;
  }

  /**
   * Tests whether a QuadEdge is an edge incident on a frame triangle vertex.
   *
   * @param e
   *          the edge to test
   * @return true if the edge is connected to the frame triangle
   */
  public boolean isFrameEdge(final QuadEdge e) {
    if (isFrameVertex(e.orig()) || isFrameVertex(e.dest())) {
      return true;
    }
    return false;
  }

  /**
   * Tests whether a vertex is a vertex of the outer triangle.
   *
   * @param v
   *          the vertex to test
   * @return true if the vertex is an outer triangle vertex
   */
  public boolean isFrameVertex(final Vertex v) {
    if (v.equals(this.frameVertex[0])) {
      return true;
    }
    if (v.equals(this.frameVertex[1])) {
      return true;
    }
    if (v.equals(this.frameVertex[2])) {
      return true;
    }
    return false;
  }

  /**
   * Tests whether a {@link Coordinates} lies on a {@link QuadEdge}, up to a
   * tolerance determined by the subdivision tolerance.
   *
   * @param e
   *          a QuadEdge
   * @param point
   *          a point
   * @return true if the vertex lies on the edge
   */
  public boolean isOnEdge(final QuadEdge e, final Point point) {
    final Point p1 = e.orig().getCoordinate();
    final Point p2 = e.dest().getCoordinate();
    final double dist = LineSegmentUtil.distanceLinePoint(p1, p2, point);
    // heuristic (hack?)
    return dist < this.edgeCoincidenceTolerance;
  }

  /**
   * Tests whether a {@link Vertex} is the start or end vertex of a
   * {@link QuadEdge}, up to the subdivision tolerance distance.
   *
   * @param e
   * @param v
   * @return true if the vertex is a endpoint of the edge
   */
  public boolean isVertexOfEdge(final QuadEdge e, final Vertex v) {
    if (v.equals(e.orig(), this.tolerance) || v.equals(e.dest(), this.tolerance)) {
      return true;
    }
    return false;
  }

  /**
   * Finds a quadedge of a triangle containing a location
   * specified by a {@link Coordinates}, if one exists.
   *
   * @param p the Point to locate
   * @return a quadedge on the edge of a triangle which touches or contains the location
   * or null if no such triangle exists
   */
  public QuadEdge locate(final Point p) {
    return this.locator.locate(new Vertex(p));
  }

  /**
   * Locates the edge between the given vertices, if it exists in the
   * subdivision.
   *
   * @param p0 a coordinate
   * @param p1 another coordinate
   * @return the edge joining the coordinates, if present
   * or null if no such edge exists
   */
  public QuadEdge locate(final Point p0, final Point p1) {
    // find an edge containing one of the points
    final QuadEdge e = this.locator.locate(new Vertex(p0));
    if (e == null) {
      return null;
    }

    // normalize so that p0 is origin of base edge
    QuadEdge base = e;
    if (e.dest().getCoordinate().equals(2, p0)) {
      base = e.sym();
    }
    // check all edges around origin of base edge
    QuadEdge locEdge = base;
    do {
      if (locEdge.dest().getCoordinate().equals(2, p1)) {
        return locEdge;
      }
      locEdge = locEdge.oNext();
    } while (locEdge != base);
    return null;
  }

  /**
   * Finds a quadedge of a triangle containing a location
   * specified by a {@link Vertex}, if one exists.
   *
   * @param v the vertex to locate
   * @return a quadedge on the edge of a triangle which touches or contains the location
   * or null if no such triangle exists
   */
  public QuadEdge locate(final Vertex v) {
    return this.locator.locate(v);
  }

  /**
   * Locates an edge of a triangle which contains a location
   * specified by a Vertex v.
   * The edge returned has the
   * property that either v is on e, or e is an edge of a triangle containing v.
   * The search starts from startEdge amd proceeds on the general direction of v.
   * <p>
   * This locate algorithm relies on the subdivision being Delaunay. For
   * non-Delaunay subdivisions, this may loop for ever.
   *
   * @param v the location to search for
   * @param startEdge an edge of the subdivision to start searching at
   * @returns a QuadEdge which contains v, or is on the edge of a triangle containing v
   * @throws LocateFailureException
   *           if the location algorithm fails to converge in a reasonable
   *           number of iterations
   */
  public QuadEdge locateFromEdge(final Vertex v, final QuadEdge startEdge) {
    int iter = 0;
    final int maxIter = this.quadEdges.size();

    QuadEdge e = startEdge;

    while (true) {
      iter++;

      /**
       * So far it has always been the case that failure to locate indicates an
       * invalid subdivision. So just fail completely. (An alternative would be
       * to perform an exhaustive search for the containing triangle, but this
       * would mask errors in the subdivision topology)
       *
       * This can also happen if two vertices are located very close together,
       * since the orientation predicates may experience precision failures.
       */
      if (iter > maxIter) {
        throw new LocateFailureException(e.toLineSegment());
        // String msg = "Locate failed to converge (at edge: " + e + ").
        // Possible causes include invalid Subdivision topology or very close
        // sites";
        // System.err.println(msg);
        // dumpTriangles();
      }

      if (v.equals(e.orig()) || v.equals(e.dest())) {
        break;
      } else if (v.rightOf(e)) {
        e = e.sym();
      } else if (!v.rightOf(e.oNext())) {
        e = e.oNext();
      } else if (!v.rightOf(e.dPrev())) {
        e = e.dPrev();
      } else {
        // on edge or in triangle containing edge
        break;
      }
    }
    // System.out.println("Locate count: " + iter);
    return e;
  }

  /**
   * Creates a new quadedge, recording it in the edges list.
   *
   * @param o
   * @param d
   * @return a new quadedge
   */
  public QuadEdge makeEdge(final Vertex o, final Vertex d) {
    final QuadEdge q = QuadEdge.makeEdge(o, d);
    this.quadEdges.add(q);
    return q;
  }

  /**
   * Sets the {@link QuadEdgeLocator} to use for locating containing triangles
   * in this subdivision.
   *
   * @param locator
   *          a QuadEdgeLocator
   */
  public void setLocator(final QuadEdgeLocator locator) {
    this.locator = locator;
  }

  /*****************************************************************************
   * Visitors
   ****************************************************************************/

  public void visitTriangles(final TriangleVisitor triVisitor, final boolean includeFrame) {
    // visited flag is used to record visited edges of triangles
    // setVisitedAll(false);
    final Stack edgeStack = new Stack();
    edgeStack.push(this.startingEdge);

    final Set visitedEdges = new HashSet();

    while (!edgeStack.empty()) {
      final QuadEdge edge = (QuadEdge)edgeStack.pop();
      if (!visitedEdges.contains(edge)) {
        final QuadEdge[] triEdges = fetchTriangleToVisit(edge, edgeStack, includeFrame,
          visitedEdges);
        if (triEdges != null) {
          triVisitor.visit(triEdges);
        }
      }
    }
  }

}
