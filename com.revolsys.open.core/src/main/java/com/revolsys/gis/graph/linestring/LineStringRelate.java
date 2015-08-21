package com.revolsys.gis.graph.linestring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleListCoordinatesList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class LineStringRelate {
  private final LineString line1;

  private final LineString line2;

  private final LineStringGraph graph1;

  private final Coordinates fromPoint1;

  private final Coordinates fromPoint2;

  private final LineStringGraph graph2;

  private final Coordinates toPoint1;

  private final Coordinates toPoint2;

  public LineStringRelate(final LineString line1, final LineString line2) {
    this(line1, line2, 1);
  }

  public LineStringRelate(final LineString line1, final LineString line2, final double tolerance) {
    this.line1 = line1;
    this.line2 = line2;
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line1);
    this.graph1 = new LineStringGraph(geometryFactory, line1);
    this.graph2 = new LineStringGraph(geometryFactory, line2);

    final Map<Coordinates, Coordinates> movedNodes = new HashMap<Coordinates, Coordinates>();
    final InvokeMethodVisitor<Node<LineSegment>> moveNodesVisitor1 = new InvokeMethodVisitor<Node<LineSegment>>(
      this.graph2, "movePointsWithinTolerance", movedNodes, tolerance);
    this.graph1.visitNodes(moveNodesVisitor1);
    final InvokeMethodVisitor<Node<LineSegment>> moveNodesVisitor2 = new InvokeMethodVisitor<Node<LineSegment>>(
      this.graph1, "movePointsWithinTolerance", movedNodes, tolerance);
    this.graph2.visitNodes(moveNodesVisitor2);

    final int i = 0;
    this.fromPoint1 = getMovedCoordinate(movedNodes, line1, i);
    this.fromPoint2 = getMovedCoordinate(movedNodes, line2, i);
    this.toPoint1 = getMovedCoordinate(movedNodes, line1, line1.getNumPoints() - 1);
    this.toPoint2 = getMovedCoordinate(movedNodes, line2, line2.getNumPoints() - 1);
  }

  public Graph<LineSegment> getGraph1() {
    return this.graph1;
  }

  public Graph<LineSegment> getGraph2() {
    return this.graph2;
  }

  public LineString getLine1() {
    return this.line1;
  }

  public LineString getLine2() {
    return this.line2;
  }

  public Coordinates getMovedCoordinate(final Map<Coordinates, Coordinates> movedNodes,
    final LineString line, final int i) {
    final Coordinates coordinates = CoordinatesListUtil.get(line, i);
    if (movedNodes.containsKey(coordinates)) {
      return movedNodes.get(coordinates);
    } else {
      return coordinates;
    }
  }

  public MultiLineString getOverlap() {
    final List<CoordinatesList> intersections = new ArrayList<CoordinatesList>();
    final CoordinatesList points1 = CoordinatesListUtil.get(this.line1);
    final DoubleListCoordinatesList currentCoordinates = new DoubleListCoordinatesList(
      points1.getNumAxis());
    Node<LineSegment> previousNode = this.graph1.getNode(this.fromPoint1);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else if (outEdges.size() > 1) {
        System.err.println("Cannot handle overlaps\n" + getLine1() + "\n " + getLine2());
        final GeometryFactory factory = GeometryFactory.getFactory(this.line1);
        return factory.createMultiLineString();
      } else {
        final Edge<LineSegment> edge = outEdges.get(0);
        final LineSegment line = edge.getObject();
        final Node<LineSegment> nextNode = edge.getToNode();
        if (this.graph2.hasEdgeBetween(previousNode, nextNode)) {
          if (currentCoordinates.size() == 0) {
            currentCoordinates.add(line.get(0));
          }
          currentCoordinates.add(line.get(1));
        } else {
          if (currentCoordinates.size() > 0) {
            final CoordinatesList points = new DoubleCoordinatesList(currentCoordinates);
            intersections.add(points);
            currentCoordinates.clear();
          }
        }
        previousNode = nextNode;
      }

    } while (previousNode != null && !previousNode.equals2d(this.fromPoint1));
    if (currentCoordinates.size() > 0) {
      final CoordinatesList points = new DoubleCoordinatesList(currentCoordinates);
      intersections.add(points);
    }
    final GeometryFactory factory = GeometryFactory.getFactory(this.line1);
    return factory.createMultiLineString(intersections);
  }

  public LineString getRelateLine1() {
    return this.graph1.getLine();
  }

  public LineString getRelateLine2() {
    return this.graph2.getLine();
  }

  public boolean isContained() {
    return isContains(this.graph2, this.graph1);
  }

  public boolean isContains() {
    return isContains(this.graph1, this.graph2);
  }

  private boolean isContains(final Graph<LineSegment> graph1, final Graph<LineSegment> graph2) {
    for (final Edge<LineSegment> edge : graph2.getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (!graph1.hasEdgeBetween(fromNode, toNode)) {
        return false;
      }
    }
    return true;
  }

  public boolean isEndOverlaps(final double maxDistance) {
    if (isOverlaps()) {
      boolean overlaps = false;
      final boolean from1Within = isWithin2(this.fromPoint1, maxDistance);
      final boolean to1Within = isWithin2(this.toPoint1, 1);
      if (from1Within != to1Within) {
        final boolean from2Within = isWithin1(this.fromPoint2, 1);
        final boolean to2Within = isWithin1(this.toPoint2, 1);
        if (from2Within != to2Within) {
          overlaps = true;
        }
      }
      if (overlaps) {
        final MultiLineString intersection = getOverlap();
        if (intersection.getNumGeometries() == 1) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isEqual() {
    if (this.graph1.getEdgeCount() == this.graph2.getEdgeCount()) {
      for (final Edge<LineSegment> edge : this.graph1.getEdges()) {
        if (!this.graph2.hasEdge(edge)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean isOverlaps() {
    final LineStringGraph g1 = this.graph1;
    final LineStringGraph g2 = this.graph2;
    if (g1.getEdgeCount() <= g2.getEdgeCount()) {
      return isOverlaps(g1, g2);
    } else {
      return isOverlaps(g2, g1);
    }
  }

  private boolean isOverlaps(final LineStringGraph graph1, final LineStringGraph graph2) {
    for (final Edge<LineSegment> edge : graph1.getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (graph2.hasEdgeBetween(fromNode, toNode)) {
        return true;
      }
    }
    return false;
  }

  private boolean isWithin(final LineStringGraph graph, final Coordinates fromPoint,
    final Coordinates toPoint, final Coordinates point, final double maxDistance) {
    if (point.distance(fromPoint) < maxDistance) {
      return false;
    } else if (point.distance(toPoint) < maxDistance) {
      return false;
    } else {
      if (!graph.findNodes(point, maxDistance).isEmpty()) {
        return true;
      }
      final List<Edge<LineSegment>> edges = graph.findEdges(point, maxDistance);
      for (final Edge<LineSegment> edge : edges) {
        final LineSegment line = edge.getObject();
        if (line.intersects(point, maxDistance)) {
          return true;
        }
      }

    }
    return false;
  }

  public boolean isWithin1(final Coordinates point, final double maxDistance) {
    return isWithin(this.graph1, this.fromPoint1, this.toPoint1, point, maxDistance);
  }

  public boolean isWithin2(final Coordinates point, final double maxDistance) {
    return isWithin(this.graph2, this.fromPoint2, this.toPoint2, point, maxDistance);
  }

  public void splitEdgesCloseToNodes(final double maxDistance) {
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = this.graph1.getPointsOnEdges(
      this.graph2, maxDistance);
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge2 = this.graph2.getPointsOnEdges(
      this.graph1, maxDistance);
    this.graph1.splitEdges(pointsOnEdge1);
    this.graph2.splitEdges(pointsOnEdge2);
  }
}
