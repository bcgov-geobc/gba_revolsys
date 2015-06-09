package com.revolsys.gis.graph.visitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.event.NodeEventListener;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class SplitCrossingEdgesVisitor<T> extends AbstractEdgeListenerVisitor<T> {

  public static final String CROSSING_EDGES = "Crossing edges";

  private final Graph<T> graph;

  private final SplitEdgesCloseToNodeVisitor<T> splitEdgesCloseToNodeVisitor;

  public SplitCrossingEdgesVisitor(final Graph<T> graph) {
    this.graph = graph;
    this.splitEdgesCloseToNodeVisitor = new SplitEdgesCloseToNodeVisitor<T>(graph, CROSSING_EDGES,
        1);
  }

  @Override
  public void addNodeListener(final NodeEventListener<T> listener) {
    this.splitEdgesCloseToNodeVisitor.addNodeListener(listener);
  }

  public Collection<Edge<T>> getNewEdges() {
    return this.splitEdgesCloseToNodeVisitor.getNewEdges();
  }

  public Collection<T> getSplitObjects() {
    return this.splitEdgesCloseToNodeVisitor.getSplitObjects();
  }

  public List<Edge<T>> queryCrosses(final IdObjectIndex<Edge<T>> edgeIndex, final LineString line) {
    final PreparedGeometry preparedLine = PreparedGeometryFactory.prepare(line);
    final Envelope envelope = line.getEnvelopeInternal();
    final List<Edge<T>> edges = edgeIndex.query(envelope);
    // TODO change to use an visitor
    for (final Iterator<Edge<T>> iterator = edges.iterator(); iterator.hasNext();) {
      final Edge<T> edge = iterator.next();
      final LineString matchLine = edge.getLine();
      if (!preparedLine.crosses(matchLine)) {
        iterator.remove();
      }
    }
    return edges;
  }

  public void setNewEdges(final Collection<Edge<T>> newEdges) {
    this.splitEdgesCloseToNodeVisitor.setNewEdges(newEdges);
  }

  public void setSplitObjects(final Collection<T> splitObjects) {
    this.splitEdgesCloseToNodeVisitor.setSplitObjects(splitObjects);
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final IdObjectIndex<Edge<T>> edgeIndex = this.graph.getEdgeIndex();
    final LineString line = edge.getLine();
    final List<Edge<T>> crossings = queryCrosses(edgeIndex, line);
    crossings.remove(edge);

    for (final Edge<T> crossEdge : crossings) {
      if (!crossEdge.isRemoved()) {
        final LineString crossLine = crossEdge.getLine();
        final Coordinates intersection = LineStringUtil.getCrossingIntersection(line, crossLine);
        if (intersection != null) {
          this.graph.getPrecisionModel().makePrecise(intersection);
          final Node<T> node = this.graph.getNode(intersection);
          this.splitEdgesCloseToNodeVisitor.visit(node);
        }
      }
    }
    return true;
  }
}
