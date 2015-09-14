package com.revolsys.gis.graph.visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.equals.EqualsInstance;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.RecordGraph;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class NodeRemovalVisitor implements Consumer<Node<Record>> {

  private final Collection<String> excludedAttributes = new HashSet<String>();

  private final RecordGraph graph;

  public NodeRemovalVisitor(final RecordDefinition recordDefinition, final RecordGraph graph,
    final Collection<String> excludedAttributes) {
    super();
    this.graph = graph;
    if (excludedAttributes != null) {
      this.excludedAttributes.addAll(excludedAttributes);
    }
  }

  @Override
  public void accept(final Node<Record> node) {
    if (node.getDegree() == 2) {
      final List<Edge<Record>> edges = node.getEdges();
      if (edges.size() == 2) {
        final Edge<Record> edge1 = edges.get(0);
        final Edge<Record> edge2 = edges.get(1);
        if (edge1 != edge2) {
          final Record object1 = edge1.getObject();
          final Record object2 = edge2.getObject();
          if (EqualsInstance.INSTANCE.equals(object1, object2, this.excludedAttributes)) {
            if (edge1.isForwards(node) == edge2.isForwards(node)) {
              // if (!fixReversedEdges(node, reversedEdges, edge1, edge2)) {
              return;
              // }
            }
            if (edge1.isForwards(node)) {
              this.graph.merge(node, edge2, edge1);
            } else {
              this.graph.merge(node, edge1, edge2);
            }
          }
        }
      }
    }
  }

}
