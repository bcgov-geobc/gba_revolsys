package com.revolsys.gis.graph.visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.collection.Visitor;
import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;

public class PolygonNodeRemovalVisitor implements Visitor<Node<Record>> {

  private final Collection<String> excludedAttributes = new HashSet<String>();

  private final DataObjectGraph graph;

  public PolygonNodeRemovalVisitor(final RecordDefinition metaData, final DataObjectGraph graph,
    final Collection<String> excludedAttributes) {
    super();
    this.graph = graph;
    if (excludedAttributes != null) {
      this.excludedAttributes.addAll(excludedAttributes);
    }
  }

  @Override
  public boolean visit(final Node<Record> node) {
    final Set<Edge<Record>> edges = new LinkedHashSet<Edge<Record>>(node.getEdges());
    while (edges.size() > 1) {
      final Edge<Record> edge = edges.iterator().next();
      final Record object = edge.getObject();
      final Set<Edge<Record>> matchedEdges = new HashSet<Edge<Record>>();
      for (final Edge<Record> matchEdge : edges) {
        final Record matchObject = matchEdge.getObject();
        if (edge != matchEdge) {
          if (edge.isForwards(node) != matchEdge.isForwards(node)) {
            if (EqualsInstance.INSTANCE.equals(object, matchObject, this.excludedAttributes)) {
              matchedEdges.add(matchEdge);
            }
          }
        }
      }
      if (matchedEdges.size() == 1) {
        final Edge<Record> matchedEdge = matchedEdges.iterator().next();
        if (edge.isForwards(node)) {
          this.graph.merge(node, matchedEdge, edge);
        } else {
          this.graph.merge(node, edge, matchedEdge);
        }
      }
      edges.removeAll(matchedEdges);
      edges.remove(edge);
    }
    return true;
  }

}
