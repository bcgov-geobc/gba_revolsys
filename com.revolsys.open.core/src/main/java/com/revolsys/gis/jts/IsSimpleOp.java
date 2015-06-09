package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeIntersection;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.geomgraph.index.EdgeSetIntersector;
import com.vividsolutions.jts.geomgraph.index.SimpleMCSweepLineIntersector;

public class IsSimpleOp {
  private static class EndpointInfo {
    Coordinate pt;

    boolean isClosed;

    int degree;

    public EndpointInfo(final Coordinate pt) {
      this.pt = pt;
      this.isClosed = false;
      this.degree = 0;
    }

    public void addEndpoint(final boolean isClosed) {
      this.degree++;
      this.isClosed |= isClosed;
    }

    public Coordinate getCoordinate() {
      return this.pt;
    }
  }

  public static SegmentIntersector computeIntersections(final GeometryGraph graph,
    final LineIntersector li, final boolean ringBased) {
    final SegmentIntersector si = new SegmentIntersector(li, true, false);
    final EdgeSetIntersector esi = new SimpleMCSweepLineIntersector();
    final List<Edge> edges = new ArrayList<Edge>();
    final Iterator<Edge> edgeIter = graph.getEdgeIterator();
    while (edgeIter.hasNext()) {
      final Edge edge = edgeIter.next();
      edges.add(edge);
    }
    // optimized test for Polygons and Rings
    if (ringBased) {
      esi.computeIntersections(edges, si, false);
    } else {
      esi.computeIntersections(edges, si, true);
    }
    return si;
  }

  private final Geometry geometry;

  private final List<Coordinates> nonSimplePoints = new ArrayList<Coordinates>();

  public IsSimpleOp(final Geometry geometry) {
    this.geometry = geometry;
  }

  /**
   * Add an endpoint to the map, creating an entry for it if none exists
   */
  private void addEndpoint(final Map<Coordinate, EndpointInfo> endPoints, final Coordinate p,
    final boolean isClosed) {
    EndpointInfo eiInfo = endPoints.get(p);
    if (eiInfo == null) {
      eiInfo = new EndpointInfo(p);
      endPoints.put(p, eiInfo);
    }
    eiInfo.addEndpoint(isClosed);
  }

  private void addNonSimplePoint(final Coordinate coordinate) {
    this.nonSimplePoints.add(new DoubleCoordinates(CoordinatesUtil.get(coordinate), 2));
  }

  public List<Coordinates> getNonSimplePoints() {
    return this.nonSimplePoints;
  }

  /**
   * Tests that no edge intersection is the endpoint of a closed line.
   * This ensures that closed lines are not touched at their endpoint,
   * which is an interior point according to the Mod-2 rule
   * To check this we compute the degree of each endpoint.
   * The degree of endpoints of closed lines
   * must be exactly 2.
   */
  private boolean hasClosedEndpointIntersection(final GeometryGraph graph) {
    boolean hasIntersection = false;
    final Map<Coordinate, EndpointInfo> endPoints = new TreeMap<Coordinate, EndpointInfo>();
    for (final Iterator i = graph.getEdgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      final int maxSegmentIndex = e.getMaximumSegmentIndex();
      final boolean isClosed = e.isClosed();
      final Coordinate p0 = e.getCoordinate(0);
      addEndpoint(endPoints, p0, isClosed);
      final Coordinate p1 = e.getCoordinate(e.getNumPoints() - 1);
      addEndpoint(endPoints, p1, isClosed);
    }

    for (final Object element : endPoints.values()) {
      final EndpointInfo eiInfo = (EndpointInfo)element;
      if (eiInfo.isClosed && eiInfo.degree != 2) {
        addNonSimplePoint(eiInfo.getCoordinate());
        hasIntersection = true;
      }
    }
    return hasIntersection;
  }

  /**
   * For all edges, check if there are any intersections which are NOT at an endpoint.
   * The Geometry is not simple if there are intersections not at endpoints.
   */
  private boolean hasNonEndpointIntersection(final GeometryGraph graph) {
    boolean hasIntersection = false;
    for (final Iterator i = graph.getEdgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      final int maxSegmentIndex = e.getMaximumSegmentIndex();
      for (final Iterator eiIt = e.getEdgeIntersectionList().iterator(); eiIt.hasNext();) {
        final EdgeIntersection ei = (EdgeIntersection)eiIt.next();
        if (!ei.isEndPoint(maxSegmentIndex)) {
          final Coordinate coordinate = ei.getCoordinate();
          addNonSimplePoint(coordinate);
          hasIntersection = true;
        }
      }
    }
    return hasIntersection;
  }

  /**
   * Tests whether the geometry is simple.
   *
   * @return true if the geometry is simple
   */
  public boolean isSimple() {
    if (this.geometry.isEmpty()) {
      return true;
    } else if (this.geometry instanceof LineString) {
      return isSimpleLinearGeometry(this.geometry);
    } else if (this.geometry instanceof MultiLineString) {
      return isSimpleLinearGeometry(this.geometry);
    } else if (this.geometry instanceof MultiPoint) {
      return isSimple((MultiPoint)this.geometry);
    }
    // all other geometry types are simple by definition
    return true;
  }

  private boolean isSimple(final MultiPoint multiPoint) {
    if (multiPoint.isEmpty()) {
      return true;
    } else {
      boolean simple = true;
      final Set<Coordinates> points = new TreeSet<Coordinates>();
      for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
        final Point point = (Point)multiPoint.getGeometryN(i);
        final Coordinates coordinates = new DoubleCoordinates(CoordinatesUtil.get(point), 2);
        if (points.contains(coordinates)) {
          this.nonSimplePoints.add(coordinates);
          simple = false;
        }
        points.add(coordinates);
      }
      return simple;
    }
  }

  private boolean isSimpleLinearGeometry(final Geometry geom) {
    final GeometryGraph graph = new GeometryGraph(0, geom);
    final LineIntersector li = new RobustLineIntersector();
    final boolean ringBased = geom instanceof LinearRing || geom instanceof Polygon
        || geom instanceof MultiPolygon;
    final SegmentIntersector si = computeIntersections(graph, li, ringBased);

    // if no self-intersection, must be simple
    if (si.hasIntersection()) {
      final List<Coordinates> properIntersections = si.getProperIntersections();
      if (properIntersections.isEmpty()) {
        if (si.hasProperIntersection()) {
          addNonSimplePoint(si.getProperIntersectionPoint());
          return false;
        } else if (hasNonEndpointIntersection(graph)) {
          return false;
        } else if (hasClosedEndpointIntersection(graph)) {
          return false;
        }
      } else {
        this.nonSimplePoints.addAll(properIntersections);
        return false;
      }
    } else {
      return true;
    }
    return true;
  }

}
