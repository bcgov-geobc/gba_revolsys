package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.visitor.CreateListVisitor;
import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;

public class EdgeIntersectsLinearlyEdgeVisitor<T> implements Consumer<Edge<T>> {

  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Edge<T> edge) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final LineString line = edge.getLine();
    final Envelope env = line.getEnvelopeInternal();
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.visit(env, new EdgeIntersectsLinearlyEdgeVisitor<T>(edge, results));
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;

  }

  private final Edge<T> edge;

  private final Consumer<Edge<T>> matchVisitor;

  public EdgeIntersectsLinearlyEdgeVisitor(final Edge<T> edge,
    final Consumer<Edge<T>> matchVisitor) {
    this.edge = edge;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Edge<T> edge2) {
    if (edge2 != this.edge) {
      final LineString line1 = this.edge.getLine();
      final LineString line2 = edge2.getLine();
      final Envelope envelope1 = line1.getEnvelopeInternal();
      final Envelope envelope2 = line2.getEnvelopeInternal();
      if (envelope1.intersects(envelope2)) {
        final IntersectionMatrix relate = line1.relate(line2);
        if (relate.get(0, 0) == Dimension.L) {
          this.matchVisitor.accept(edge2);
        }
      }
    }
  }

}
