package com.revolsys.gis.graph.visitor;

import java.util.List;
import java.util.Map;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.util.ObjectProcessor;

public class MapPseudoNodeRemovalVisitor extends AbstractNodeListenerVisitor<Map<String, Object>>
  implements ObjectProcessor<Graph<Map<String, Object>>> {

  public MapPseudoNodeRemovalVisitor() {
  }

  @Override
  public void accept(final Node<Map<String, Object>> node) {
    final List<Edge<Map<String, Object>>> edges = node.getEdges();
    if (edges.size() == 2) {
      final Edge<Map<String, Object>> edge1 = edges.get(0);
      final Edge<Map<String, Object>> edge2 = edges.get(1);
      final Graph<Map<String, Object>> graph = node.getGraph();
      graph.merge(node, edge1, edge2);
    }
  }

  @Override
  public void process(final Graph<Map<String, Object>> graph) {
    graph.visitNodes(this);
  }
}
