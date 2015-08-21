package com.revolsys.gis.algorithm.linematch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.comparator.NodeDistanceComparator;
import com.revolsys.gis.graph.visitor.BoundingBoxIntersectsEdgeVisitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class LineMatchGraph<T> extends Graph<LineSegmentMatch> {
  private final GeometryFactory geometryFactory;

  private final List<Set<Node<LineSegmentMatch>>> startNodes = new ArrayList<Set<Node<LineSegmentMatch>>>();

  private final List<T> objects = new ArrayList<T>();

  private final int tolerance = 1;

  public LineMatchGraph(final GeometryFactory geometryFactory, final LineString line) {
    this.geometryFactory = geometryFactory;
    add(line, 0);
  }

  public LineMatchGraph(final GeometryFactory geometryFactory, final T object, final LineString line) {
    this.geometryFactory = geometryFactory;
    addLine(object, line);
  }

  public LineMatchGraph(final LineString line) {
    this(GeometryFactory.getFactory(line), line);
  }

  public LineMatchGraph(final T object, final LineString line) {
    this(GeometryFactory.getFactory(line), object, line);
  }

  private Edge<LineSegmentMatch> add(final Coordinates start, final Coordinates end) {
    final Node<LineSegmentMatch> startNode = getNode(start);
    final Node<LineSegmentMatch> endNode = getNode(end);

    return add(startNode, endNode);
  }

  private Edge<LineSegmentMatch> add(final Coordinates start, final Coordinates end,
    final LineSegment segment, final int index) {
    final Edge<LineSegmentMatch> edge = add(start, end);
    final LineSegmentMatch lineSegmentMatch = edge.getObject();
    lineSegmentMatch.addSegment(segment, index);
    return edge;
  }

  private Edge<LineSegmentMatch> add(final LineSegmentMatch lineSegmentMatch,
    final Node<LineSegmentMatch> from, final Node<LineSegmentMatch> to) {
    final Edge<LineSegmentMatch> newEdge = add(from, to);
    final LineSegmentMatch newLineSegmentMatch = newEdge.getObject();
    for (int i = 0; i < lineSegmentMatch.getSegmentCount(); i++) {
      if (lineSegmentMatch.hasSegment(i)) {
        final LineSegment realSegment = lineSegmentMatch.getSegment(i);
        final Coordinates coordinate0 = realSegment.project(from);
        final Coordinates coordinate1 = realSegment.project(to);

        final LineSegment newSegment = new LineSegment(this.geometryFactory, coordinate0,
          coordinate1);
        newLineSegmentMatch.addSegment(newSegment, i);
      }
    }
    return newEdge;
  }

  public boolean add(final LineString line) {
    final int index = this.startNodes.size();
    add(line, index);
    if (index > 0) {
      integrateLine(line, index);
      return hasMatchedLines(index);
    } else {
      return false;
    }
  }

  private void add(final LineString line, final int index) {
    if (line.getLength() > 0) {
      final CoordinatesList coords = CoordinatesListUtil.get(line);
      final Coordinates coordinate0 = coords.get(0);
      final Node<LineSegmentMatch> node = getNode(coordinate0);
      final Set<Node<LineSegmentMatch>> indexStartNodes = getStartNodes(index);
      indexStartNodes.add(node);

      Coordinates previousCoordinate = coordinate0;
      for (int i = 1; i < coords.size(); i++) {
        final Coordinates coordinate = coords.get(i);
        final LineSegment segment = new LineSegment(this.geometryFactory, previousCoordinate,
          coordinate);
        if (segment.getLength() > 0) {
          add(previousCoordinate, coordinate, segment, index);
        }
        previousCoordinate = coordinate;
      }
    }
  }

  public boolean add(final MultiLineString multiLine) {
    final int index = this.startNodes.size();
    if (multiLine != null && multiLine.getLength() > 0) {
      for (int i = 0; i < multiLine.getNumGeometries(); i++) {
        final LineString line = (LineString)multiLine.getGeometryN(i);
        add(line, index);
      }
      if (index > 0) {
        integrateMultiLine(multiLine, index);
        return hasMatchedLines(index);
      }
    }
    return false;
  }

  private Edge<LineSegmentMatch> add(final Node<LineSegmentMatch> startNode,
    final Node<LineSegmentMatch> endNode) {
    Edge<LineSegmentMatch> edge = getEdge(startNode, endNode);

    if (edge == null) {
      final LineSegmentMatch lineSegmentMatch = new LineSegmentMatch(this.geometryFactory,
        startNode, endNode);
      edge = addEdge(lineSegmentMatch, lineSegmentMatch.getLine());
    }
    return edge;
  }

  public boolean add(final T object, final MultiLineString multiLine) {
    if (add(multiLine)) {
      addObject(object);
      return true;
    } else {
      return false;
    }
  }

  public boolean addLine(final T object, final LineString line) {
    if (add(line)) {
      addObject(object);
      return true;
    } else {
      return false;
    }
  }

  public boolean addLine(final T object, final MultiLineString line) {
    if (add(line)) {
      addObject(object);
      return true;
    } else {
      return false;
    }
  }

  private void addObject(final T object) {
    final int index = this.startNodes.size() - 1;
    for (int i = this.objects.size(); i < index; i++) {
      this.objects.add(null);
    }
    this.objects.add(object);
  }

  public boolean cleanOverlappingMatches() {
    final MultiLineString lines = getOverlappingMatches();
    return lines.isEmpty();
  }

  private void createLine(final List<LineString> lines, final List<Coordinates> coordinates) {
    if (!coordinates.isEmpty()) {
      final LineString line = this.geometryFactory.createLineString(coordinates);
      lines.add(line);
    }
  }

  public MultiLineString getCurrentMatchedLines() {
    return getMatchedLines(this.startNodes.size() - 1);
  }

  public double getDuplicateMatchLength(final Node<LineSegmentMatch> node, final boolean direction,
    final int index1, final int index2) {
    List<Edge<LineSegmentMatch>> edges;
    if (direction) {
      edges = node.getOutEdges();
    } else {
      edges = node.getInEdges();
    }
    for (final Edge<LineSegmentMatch> edge : edges) {
      final LineSegmentMatch lineSegmentMatch = edge.getObject();
      if (lineSegmentMatch.getMatchCount(index1) > 2) {
        if (lineSegmentMatch.hasMatches(index1, index2)) {
          final LineSegment segment = lineSegmentMatch.getSegment(index2);
          final double length = segment.getLength();
          final Node<LineSegmentMatch> nextNode = edge.getOppositeNode(node);
          return length + getDuplicateMatchLength(nextNode, direction, index1, index2);
        }
      }
    }
    return 0;
  }

  private Edge<LineSegmentMatch> getEdge(final Coordinates coordinate0,
    final Coordinates coordinate1) {
    final Node<LineSegmentMatch> node1 = findNode(coordinate0);
    final Node<LineSegmentMatch> node2 = findNode(coordinate1);
    return getEdge(node1, node2);
  }

  private Edge<LineSegmentMatch> getEdge(final Node<LineSegmentMatch> node1,
    final Node<LineSegmentMatch> node2) {
    final List<Edge<LineSegmentMatch>> edges = node1.getOutEdgesTo(node2);
    if (edges.isEmpty()) {
      return null;
    } else {
      return edges.get(0);
    }
  }

  private Set<Edge<LineSegmentMatch>> getEdgesWithoutMatch(final LineString line, final int index) {
    final Set<Edge<LineSegmentMatch>> edges = new LinkedHashSet<Edge<LineSegmentMatch>>();

    final CoordinatesList coordinatesList = CoordinatesListUtil.get(line);
    final Coordinates coordinate0 = coordinatesList.get(0);
    Coordinates previousCoordinate = coordinate0;
    for (int i = 1; i < coordinatesList.size(); i++) {
      final Coordinates coordinate = coordinatesList.get(i);
      if (previousCoordinate.distance(coordinate) > 0) {
        final Edge<LineSegmentMatch> edge = getEdge(previousCoordinate, coordinate);
        if (edge != null) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (!lineSegmentMatch.hasMatches(index)) {
            edges.add(edge);
          }
        }
      }
      previousCoordinate = coordinate;
    }
    return edges;
  }

  public int getIndexCount() {
    return this.startNodes.size();
  }

  public MultiLineString getMatchedLines(final int index) {
    final List<LineString> lines = new ArrayList<LineString>();
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();

    for (Node<LineSegmentMatch> currentNode : getStartNodes(index)) {
      final List<Coordinates> coordinates = new ArrayList<Coordinates>();
      while (currentNode != null) {
        Node<LineSegmentMatch> nextNode = null;
        final List<Edge<LineSegmentMatch>> edges = currentNode.getOutEdges();
        for (final Edge<LineSegmentMatch> edge : edges) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (lineSegmentMatch.hasSegment(index) && !processedEdges.contains(edge)) {
            if (lineSegmentMatch.hasMatches(index)) {
              if (coordinates.isEmpty()) {
                final Coordinates startCoordinate = currentNode;
                coordinates.add(startCoordinate);
              }
              final Node<LineSegmentMatch> toNode = edge.getOppositeNode(currentNode);
              final Coordinates toCoordinate = toNode;
              coordinates.add(toCoordinate);
            } else {
              createLine(lines, coordinates);
              coordinates.clear();
            }
            processedEdges.add(edge);
            nextNode = edge.getOppositeNode(currentNode);
          }
        }
        currentNode = nextNode;
      }
      createLine(lines, coordinates);
    }
    return this.geometryFactory.createMultiLineString(lines);
  }

  public MultiLineString getMatchedLines(final int index1, final int index2) {
    final List<LineString> lines = getMatchedLinesList(index1, index2);
    return this.geometryFactory.createMultiLineString(lines);

  }

  public List<LineString> getMatchedLinesList(final int index1, final int index2) {
    final List<LineString> lines = new ArrayList<LineString>();
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();
    for (Node<LineSegmentMatch> currentNode : getStartNodes(index2)) {
      final List<Coordinates> coordinates = new ArrayList<Coordinates>();
      while (currentNode != null) {
        Node<LineSegmentMatch> nextNode = null;
        final List<Edge<LineSegmentMatch>> edges = currentNode.getOutEdges();
        for (final Edge<LineSegmentMatch> edge : edges) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (lineSegmentMatch.hasSegment(index2) && !processedEdges.contains(edge)) {
            if (lineSegmentMatch.hasMatches(index1, index2)) {
              if (coordinates.isEmpty()) {
                final Coordinates startCoordinate = currentNode;
                coordinates.add(startCoordinate);
              }
              final Node<LineSegmentMatch> toNode = edge.getToNode();
              final Coordinates toCoordinate = toNode;
              coordinates.add(toCoordinate);
            } else {
              createLine(lines, coordinates);
              coordinates.clear();
            }
            processedEdges.add(edge);
            nextNode = edge.getToNode();
          }
        }
        currentNode = nextNode;
      }
      createLine(lines, coordinates);
    }
    return lines;
  }

  public List<T> getMatchedObjects() {
    final ArrayList<T> matchedObjects = new ArrayList<T>();
    for (final T object : this.objects) {
      if (object != null) {
        matchedObjects.add(object);
      }
    }
    return matchedObjects;
  }

  public double getMatchLength(final Node<LineSegmentMatch> node, final boolean direction,
    final int index1, final int index2) {
    List<Edge<LineSegmentMatch>> edges;
    if (direction) {
      edges = node.getOutEdges();
    } else {
      edges = node.getInEdges();
    }
    for (final Edge<LineSegmentMatch> edge : edges) {
      final LineSegmentMatch lineSegmentMatch = edge.getObject();
      if (lineSegmentMatch.hasMatches(index1, index2)) {
        final LineSegment segment = lineSegmentMatch.getSegment(index2);
        final double length = segment.getLength();
        final Node<LineSegmentMatch> nextNode = edge.getOppositeNode(node);
        return length + getMatchLength(nextNode, direction, index1, index2);
      }
    }
    return 0;
  }

  public MultiLineString getNonMatchedLines(final int index) {
    final List<LineString> lines = new ArrayList<LineString>();
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();

    for (Node<LineSegmentMatch> currentNode : getStartNodes(index)) {
      final List<Coordinates> coordinates = new ArrayList<Coordinates>();
      while (currentNode != null) {
        Node<LineSegmentMatch> nextNode = null;
        final List<Edge<LineSegmentMatch>> edges = currentNode.getOutEdges();
        for (final Edge<LineSegmentMatch> edge : edges) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (lineSegmentMatch.hasSegment(index) && !processedEdges.contains(edge)) {
            final Node<LineSegmentMatch> toNode = edge.getToNode();
            if (!lineSegmentMatch.hasMatches(index)) {
              if (coordinates.isEmpty()) {
                coordinates.add(currentNode);
              }
              coordinates.add(toNode);
            } else {
              createLine(lines, coordinates);
              coordinates.clear();
            }
            processedEdges.add(edge);
            nextNode = toNode;
          }
        }
        currentNode = nextNode;
      }
      createLine(lines, coordinates);
    }
    return this.geometryFactory.createMultiLineString(lines);
  }

  public MultiLineString getNonMatchedLines(final int index1, final int index2) {
    final List<LineString> lines = new ArrayList<LineString>();
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();

    for (Node<LineSegmentMatch> currentNode : getStartNodes(index1)) {
      final List<Coordinates> coordinates = new ArrayList<Coordinates>();
      while (currentNode != null) {
        Node<LineSegmentMatch> nextNode = null;
        final List<Edge<LineSegmentMatch>> edges = currentNode.getOutEdges();
        for (final Edge<LineSegmentMatch> edge : edges) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (lineSegmentMatch.hasSegment(index1) && !processedEdges.contains(edge)) {
            if (!lineSegmentMatch.hasMatches(index1, index2)) {
              if (coordinates.isEmpty()) {
                final Coordinates startCoordinate = currentNode;
                coordinates.add(startCoordinate);
              }
              final Node<LineSegmentMatch> toNode = edge.getToNode();
              final Coordinates toCoordinate = toNode;
              coordinates.add(toCoordinate);
            } else {
              createLine(lines, coordinates);
              coordinates.clear();
            }
            processedEdges.add(edge);
            nextNode = edge.getToNode();
          }
        }
        currentNode = nextNode;
      }
      createLine(lines, coordinates);
    }
    return this.geometryFactory.createMultiLineString(lines);

  }

  public T getObject(final int index) {
    if (index < this.objects.size()) {
      return this.objects.get(index);
    } else {
      return null;
    }
  }

  public MultiLineString getOverlappingMatches() {
    final List<LineString> overlappingLines = new ArrayList<LineString>();
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();

    for (Node<LineSegmentMatch> currentNode : getStartNodes(0)) {
      while (currentNode != null) {
        Node<LineSegmentMatch> nextNode = null;
        final List<Edge<LineSegmentMatch>> edges = currentNode.getOutEdges();
        for (final Edge<LineSegmentMatch> edge : edges) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (lineSegmentMatch.hasSegment(0) && !processedEdges.contains(edge)) {
            if (lineSegmentMatch.getMatchCount(0) > 2) {
              for (int i = 1; i < lineSegmentMatch.getSegmentCount()
                && lineSegmentMatch.getMatchCount(0) > 2; i++) {
                if (lineSegmentMatch.hasSegment(i)) {
                  if (!hasMatch(currentNode, false, 0, i)) {
                    final Node<LineSegmentMatch> toNode = edge.getToNode();
                    if (!hasMatch(toNode, true, 0, i)) {
                      lineSegmentMatch.removeSegment(i);
                    } else {
                      final double matchLength = getMatchLength(currentNode, false, 0, i);
                      final double duplicateMatchLength = getDuplicateMatchLength(currentNode,
                        true, 0, i);
                      if (matchLength + duplicateMatchLength <= 2) {
                        lineSegmentMatch.removeSegment(i);
                      }
                    }
                  }
                }
              }
              if (lineSegmentMatch.getMatchCount(0) > 2) {
                overlappingLines.add(lineSegmentMatch.getLine());
              }
            }
            processedEdges.add(edge);
            nextNode = edge.getToNode();
          }
        }
        currentNode = nextNode;
      }
    }
    final MultiLineString lines = this.geometryFactory.createMultiLineString(overlappingLines);
    return lines;
  }

  private Set<Node<LineSegmentMatch>> getStartNodes(final int index) {
    while (index >= this.startNodes.size()) {
      this.startNodes.add(new LinkedHashSet<Node<LineSegmentMatch>>());
    }
    return this.startNodes.get(index);
  }

  public Edge<LineSegmentMatch> getUnprocessedEdgeWithSegment(final Node<LineSegmentMatch> node,
    final int index, final Set<Edge<LineSegmentMatch>> processedEdges) {
    final List<Edge<LineSegmentMatch>> edges = node.getOutEdges();
    for (final Edge<LineSegmentMatch> edge : edges) {
      final LineSegmentMatch lineSegmentMatch = edge.getObject();
      if (lineSegmentMatch.hasSegment(index) && !processedEdges.contains(edge)) {
        processedEdges.add(edge);
        return edge;
      }
    }
    return null;
  }

  public boolean hasMatch(final Node<LineSegmentMatch> node, final boolean direction,
    final int index1, final int index2) {
    List<Edge<LineSegmentMatch>> edges;
    if (direction) {
      edges = node.getOutEdges();
    } else {
      edges = node.getInEdges();
    }
    for (final Edge<LineSegmentMatch> edge : edges) {
      final LineSegmentMatch lineSegmentMatch = edge.getObject();
      if (lineSegmentMatch.hasMatches(index1, index2)) {
        return true;
      }
    }
    return false;

  }

  public boolean hasMatchedLines(final int index) {
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();
    for (Node<LineSegmentMatch> currentNode : getStartNodes(index)) {
      while (currentNode != null) {
        Node<LineSegmentMatch> nextNode = null;
        final List<Edge<LineSegmentMatch>> edges = currentNode.getOutEdges();
        for (final Edge<LineSegmentMatch> edge : edges) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (lineSegmentMatch.hasSegment(index) && !processedEdges.contains(edge)) {
            if (lineSegmentMatch.hasMatches(index)) {
              return true;
            }
            nextNode = edge.getToNode();
          }
          processedEdges.add(edge);
        }
        currentNode = nextNode;
      }
    }
    return false;

  }

  public boolean hasOverlappingMatches() {
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();

    for (Node<LineSegmentMatch> currentNode : getStartNodes(0)) {
      while (currentNode != null) {
        Node<LineSegmentMatch> nextNode = null;
        final List<Edge<LineSegmentMatch>> edges = currentNode.getOutEdges();
        for (final Edge<LineSegmentMatch> edge : edges) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (lineSegmentMatch.hasSegment(0) && !processedEdges.contains(edge)) {
            if (lineSegmentMatch.getMatchCount(0) > 2) {
              return true;
            }
            processedEdges.add(edge);
            nextNode = edge.getToNode();
          }
        }
        currentNode = nextNode;
      }
    }
    return false;

  }

  public boolean hasSegmentsWithIndex(final Node<LineSegmentMatch> node, final int index) {
    final List<Edge<LineSegmentMatch>> edges = node.getEdges();
    for (final Edge<LineSegmentMatch> edge : edges) {
      final LineSegmentMatch lineSegmentMatch = edge.getObject();
      if (lineSegmentMatch.getSegmentCount() > index) {
        if (lineSegmentMatch.getSegment(index) != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Integrate the line, splitting any edges which the nodes from this line are
   * on the other edges.
   *
   * @param line
   * @param index
   */
  private void integrateLine(final LineString line, final int index) {
    final Set<Edge<LineSegmentMatch>> edgesToProcess = getEdgesWithoutMatch(line, index);
    if (!edgesToProcess.isEmpty()) {
      for (final Edge<LineSegmentMatch> edge : edgesToProcess) {
        if (!edge.isRemoved()) {
          final LineSegmentMatch lineSegmentMatch = edge.getObject();
          if (!lineSegmentMatch.hasMatches(index)) {
            final List<Edge<LineSegmentMatch>> matchEdges = BoundingBoxIntersectsEdgeVisitor.getEdges(
              this, edge, this.tolerance);
            if (!matchEdges.isEmpty()) {
              final boolean allowSplit = edge.getLength() >= 2 * this.tolerance;
              final Set<Node<LineSegmentMatch>> splitNodes = new TreeSet<Node<LineSegmentMatch>>(
                new NodeDistanceComparator<LineSegmentMatch>(edge.getFromNode()));
              final Node<LineSegmentMatch> lineStart = edge.getFromNode();
              final Node<LineSegmentMatch> lineEnd = edge.getToNode();

              for (final ListIterator<Edge<LineSegmentMatch>> iterator = matchEdges.listIterator(); iterator.hasNext();) {
                final Edge<LineSegmentMatch> matchEdge = iterator.next();
                iterator.remove();
                final LineSegmentMatch matchLineSegmentMatch = matchEdge.getObject();
                if (!matchLineSegmentMatch.hasSegment(index)
                  && matchLineSegmentMatch.hasOtherSegment(index)) {
                  final Node<LineSegmentMatch> line2Start = matchEdge.getFromNode();
                  final Node<LineSegmentMatch> line2End = matchEdge.getToNode();
                  final Set<Node<LineSegmentMatch>> matchSplitNodes = new TreeSet<Node<LineSegmentMatch>>(
                    new NodeDistanceComparator<LineSegmentMatch>(line2Start));
                  if (matchEdge.getLength() >= 2 * this.tolerance) {
                    if (LineSegmentUtil.isPointOnLineMiddle(line2Start, line2End, lineStart,
                      this.tolerance)) {
                      matchSplitNodes.add(lineStart);
                    }
                    if (LineSegmentUtil.isPointOnLineMiddle(line2Start, line2End, lineEnd,
                      this.tolerance)) {
                      matchSplitNodes.add(lineEnd);
                    }
                  }
                  if (!matchSplitNodes.isEmpty()) {
                    final List<Edge<LineSegmentMatch>> splitEdges = splitEdge(matchEdge,
                      matchSplitNodes);
                    for (final Edge<LineSegmentMatch> splitEdge : splitEdges) {
                      iterator.add(splitEdge);
                    }
                  } else if (allowSplit) {
                    if (LineSegmentUtil.isPointOnLineMiddle(lineStart, lineEnd, line2Start,
                      this.tolerance)) {
                      splitNodes.add(line2Start);
                    }
                    if (LineSegmentUtil.isPointOnLineMiddle(lineStart, lineEnd, line2End,
                      this.tolerance)) {
                      splitNodes.add(line2End);
                    }
                  }
                }
              }
              if (!splitNodes.isEmpty() && allowSplit) {
                splitEdge(edge, splitNodes);
              }
            }
          }
        }
      }
    }
  }

  private void integrateMultiLine(final MultiLineString multLine, final int index) {
    for (int i = 0; i < multLine.getNumGeometries(); i++) {
      final LineString line = (LineString)multLine.getGeometryN(i);
      integrateLine(line, index);
    }
  }

  public boolean isFullyMatched(final int index) {
    final MultiLineString difference = getNonMatchedLines(index);
    if (difference.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Edge<LineSegmentMatch>> splitEdge(final Edge<LineSegmentMatch> edge,
    final Node<LineSegmentMatch> node) {
    final Coordinates coordinate = node;
    final LineSegmentMatch lineSegmentMatch = edge.getObject();
    final LineSegment segment = lineSegmentMatch.getSegment();

    final Edge<LineSegmentMatch> edge1 = add(segment.get(0), coordinate);
    final LineSegmentMatch lineSegmentMatch1 = edge1.getObject();
    final Edge<LineSegmentMatch> edge2 = add(coordinate, segment.get(1));
    final LineSegmentMatch lineSegmentMatch2 = edge2.getObject();

    for (int i = 0; i < lineSegmentMatch.getSegmentCount(); i++) {
      if (lineSegmentMatch.hasSegment(i)) {
        final LineSegment realSegment = lineSegmentMatch.getSegment(i);
        final Coordinates projectedCoordinate = realSegment.project(coordinate);

        final Coordinates startCoordinate = realSegment.get(0);
        final LineSegment segment1 = new LineSegment(this.geometryFactory, startCoordinate,
          projectedCoordinate);
        lineSegmentMatch1.addSegment(segment1, i);

        final Coordinates endCoordinate = realSegment.get(1);
        final LineSegment segment2 = new LineSegment(this.geometryFactory, projectedCoordinate,
          endCoordinate);
        lineSegmentMatch2.addSegment(segment2, i);
      }
    }
    remove(edge);
    return Arrays.asList(edge1, edge2);
  }

  private List<Edge<LineSegmentMatch>> splitEdge(final Edge<LineSegmentMatch> edge,
    final Set<Node<LineSegmentMatch>> splitNodes) {
    final Node<LineSegmentMatch> fromNode = edge.getFromNode();
    final Node<LineSegmentMatch> toNode = edge.getToNode();
    splitNodes.remove(fromNode);
    splitNodes.remove(toNode);

    if (splitNodes.isEmpty()) {
      return Collections.singletonList(edge);
    } else {
      final List<Edge<LineSegmentMatch>> edges = new ArrayList<Edge<LineSegmentMatch>>();
      final LineSegmentMatch lineSegmentMatch = edge.getObject();
      Node<LineSegmentMatch> previousNode = fromNode;
      for (final Node<LineSegmentMatch> currentNode : splitNodes) {
        edges.add(add(lineSegmentMatch, previousNode, currentNode));
        previousNode = currentNode;
      }
      edges.add(add(lineSegmentMatch, previousNode, toNode));
      remove(edge);
      return edges;
    }
  }

}
