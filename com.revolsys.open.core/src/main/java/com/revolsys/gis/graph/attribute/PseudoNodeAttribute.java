package com.revolsys.gis.graph.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgePair;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.vividsolutions.jts.geom.LineString;

public class PseudoNodeAttribute {
  private final Set<String> equalExcludeAttributes = new HashSet<String>();

  private final List<EdgePair<Record>> edgePairs = new ArrayList<EdgePair<Record>>();

  private final List<EdgePair<Record>> reversedEdgePairs = new ArrayList<EdgePair<Record>>();

  private final String typePath;

  public PseudoNodeAttribute(final Node<Record> node,
    final String typePath, final Collection<String> equalExcludeAttributes) {
    this.typePath = typePath;
    if (equalExcludeAttributes != null) {
      this.equalExcludeAttributes.addAll(equalExcludeAttributes);
    }
    final Map<String, Map<LineString, Set<Edge<Record>>>> edgesByTypeNameAndLine = NodeAttributes.getEdgesByTypeNameAndLine(node);
    final Map<LineString, Set<Edge<Record>>> edgesByLine = edgesByTypeNameAndLine.get(typePath);
    init(node, edgesByLine);
  }

  private EdgePair<Record> createEdgePair(final Node<Record> node,
    final Edge<Record> edge1, final Edge<Record> edge2) {
    final Record object1 = edge1.getObject();
    final Record object2 = edge2.getObject();
    if (DirectionalAttributes.canMergeObjects(node, object1, object2,
      equalExcludeAttributes)) {
      return new EdgePair<Record>(edge1, edge2);
    } else {
      return null;
    }
  }

  public List<EdgePair<Record>> getEdgePairs() {
    return edgePairs;
  }

  public List<EdgePair<Record>> getReversedEdgePairs() {
    return reversedEdgePairs;
  }

  public String getTypeName() {
    return typePath;
  }

  private void init(final Node<Record> node,
    final Map<LineString, Set<Edge<Record>>> edgesByLine) {
    if (isPseudoNode(node, edgesByLine)) {

    }
  }

  protected boolean isPseudoNode(final Node<Record> node,
    final Map<LineString, Set<Edge<Record>>> edgesByLine) {
    final Set<LineString> lines = edgesByLine.keySet();
    if (!LineStringUtil.hasLoop(lines)) {
      if (edgesByLine.size() == 2) {
        final Iterator<Set<Edge<Record>>> edgeIter = edgesByLine.values()
          .iterator();
        final Set<Edge<Record>> edges1 = edgeIter.next();
        final Set<Edge<Record>> edges2 = edgeIter.next();
        final int size1 = edges1.size();
        final int size2 = edges2.size();
        if (size1 == size2) {
          if (size1 == 1) {
            final Edge<Record> edge1 = edges1.iterator().next();
            final Edge<Record> edge2 = edges2.iterator().next();
            final EdgePair<Record> edgePair = createEdgePair(node, edge1,
              edge2);
            if (edgePair != null) {
              if (edge1.isForwards(node) == edge2.isForwards(node)) {
                reversedEdgePairs.add(edgePair);
              } else {
                edgePairs.add(edgePair);
              }
              return true;
            }
          } else {
            final List<Edge<Record>> unmatchedEdges1 = new ArrayList<Edge<Record>>(
              edges1);
            final List<Edge<Record>> unmatchedEdges2 = new ArrayList<Edge<Record>>(
              edges2);
            // Find non-reversed matches
            matchEdges(node, unmatchedEdges1, unmatchedEdges2, edgePairs, false);
            if (unmatchedEdges2.isEmpty()) {
              return true;
            } else {
              // Find reversed matches
              matchEdges(node, unmatchedEdges1, unmatchedEdges2,
                reversedEdgePairs, true);
              if (unmatchedEdges2.isEmpty()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  private void matchEdges(final Node<Record> node,
    final List<Edge<Record>> edges1, final List<Edge<Record>> edges2,
    final List<EdgePair<Record>> pairedEdges, final boolean reversed) {
    final Iterator<Edge<Record>> edgeIter1 = edges1.iterator();
    while (edgeIter1.hasNext()) {
      final Edge<Record> edge1 = edgeIter1.next();
      boolean matched = false;
      final Iterator<Edge<Record>> edgeIter2 = edges2.iterator();
      while (!matched && edgeIter2.hasNext()) {
        final Edge<Record> edge2 = edgeIter2.next();
        boolean match = false;
        if (edge1.isForwards(node) == edge2.isForwards(node)) {
          match = reversed;
        } else {
          match = !reversed;
        }
        if (match) {
          final EdgePair<Record> edgePair = createEdgePair(node, edge1,
            edge2);
          if (edgePair != null) {
            matched = true;
            edgeIter1.remove();
            edgeIter2.remove();
            pairedEdges.add(edgePair);
          }
        }
      }
    }
  }
}
