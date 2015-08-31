package com.revolsys.geometry.graph.visitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.equals.RecordEquals;
import com.revolsys.data.record.Record;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.graph.attribute.NodeProperties;
import com.revolsys.gis.io.Statistics;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;

public class ItersectsNodeEdgeCleanupVisitor extends AbstractVisitor<Edge<Record>>
  implements ObjectProcessor<RecordGraph> {

  private static final Logger LOG = LoggerFactory.getLogger(ItersectsNodeEdgeCleanupVisitor.class);

  private final Set<String> equalExcludeFieldNames = new HashSet<String>(
    Arrays.asList(RecordEquals.EXCLUDE_ID, RecordEquals.EXCLUDE_GEOMETRY));

  private Statistics splitStatistics;

  @Override
  public void accept(final Edge<Record> edge) {
    final String typePath = edge.getTypeName();
    final Node<Record> fromNode = edge.getFromNode();
    final Node<Record> toNode = edge.getToNode();

    final Graph<Record> graph = edge.getGraph();
    final List<Node<Record>> nodes = graph.findNodes(edge, 2);
    for (final Iterator<Node<Record>> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
      final Node<Record> node = nodeIter.next();
      final List<Edge<Record>> edges = NodeProperties.getEdgesByType(node, typePath);
      if (edges.isEmpty()) {
        nodeIter.remove();
      }
    }
    if (!nodes.isEmpty()) {
      if (nodes.size() > 1) {
        for (int i = 0; i < nodes.size(); i++) {
          Node<Record> node1 = nodes.get(i);
          for (int j = i + 1; j < nodes.size(); j++) {
            final Node<Record> node2 = nodes.get(j);
            if (node1.distance(node2) < 2) {
              if (edge.distance(node1) <= edge.distance(node2)) {
                nodes.remove(j);
              } else {
                nodes.remove(i);
                node1 = node2;
              }
            }
          }
        }
      }
      if (nodes.size() == 1) {
        final Node<Record> node = nodes.get(0);
        if (node.distance(fromNode) <= 10) {
          moveEndUndershoots(typePath, fromNode, node);
        } else if (node.distance(toNode) <= 10) {
          moveEndUndershoots(typePath, toNode, node);
        } else {
          graph.splitEdge(edge, nodes);
          this.splitStatistics.add(typePath);
        }
      } else {
        graph.splitEdge(edge, nodes);
      }

    }
  }

  @PreDestroy
  public void destroy() {
    if (this.splitStatistics != null) {
      this.splitStatistics.disconnect();
    }
    this.splitStatistics = null;
  }

  public Set<String> getEqualExcludeFieldNames() {
    return this.equalExcludeFieldNames;
  }

  @PostConstruct
  public void init() {
    this.splitStatistics = new Statistics("Split edges");
    this.splitStatistics.connect();
  }

  private boolean moveEndUndershoots(final String typePath, final Node<Record> node1,
    final Node<Record> node2) {
    boolean matched = false;
    if (!node2.hasEdgeTo(node1)) {
      final Set<Double> angles1 = NodeProperties.getEdgeAnglesByType(node2, typePath);
      final Set<Double> angles2 = NodeProperties.getEdgeAnglesByType(node1, typePath);
      if (angles1.size() == 1 && angles2.size() == 1) {

        matched = node1.getGraph().moveNodesToMidpoint(typePath, node2, node1);
      }
    }
    return matched;
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.forEachEdge(this);
  }
}
