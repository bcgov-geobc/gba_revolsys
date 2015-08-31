/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.geomgraph;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;

/**
 * A map of nodes, indexed by the coordinate of the node
 * @version 1.7
 */
public class NodeMap implements Iterable<Node> {
  NodeFactory nodeFact;

  // Map nodeMap = new HashMap();
  Map<Point, Node> nodeMap = new TreeMap<>();

  public NodeMap(final NodeFactory nodeFact) {
    this.nodeFact = nodeFact;
  }

  /**
   * Adds a node for the start point of this EdgeEnd
   * (if one does not already exist in this map).
   * Adds the EdgeEnd to the (possibly new) node.
   */
  public void add(final EdgeEnd e) {
    final Point p = e.getCoordinate();
    final Node n = addNode(p);
    n.add(e);
  }

  public Node addNode(final Node n) {
    final Node node = this.nodeMap.get(n.getCoordinate());
    if (node == null) {
      this.nodeMap.put(n.getCoordinate(), n);
      return n;
    }
    node.mergeLabel(n);
    return node;
  }

  /**
   * Factory function - subclasses can override to create their own types of nodes
   */
  /*
   * protected Node createNode(Point coord) { return new Node(coord); }
   */
  /**
   * This method expects that a node has a coordinate value.
   */
  public Node addNode(final Point coord) {
    Node node = this.nodeMap.get(coord);
    if (node == null) {
      node = this.nodeFact.createNode(coord);
      this.nodeMap.put(coord, node);
    }
    return node;
  }

  /**
   * @return the node if found; null otherwise
   */
  public Node find(final Point coord) {
    return this.nodeMap.get(coord);
  }

  public Collection<Node> getBoundaryNodes(final int geomIndex) {
    final Collection<Node> boundaryNodes = new ArrayList<>();
    for (final Node node : this) {
      if (node.getLabel().getLocation(geomIndex) == Location.BOUNDARY) {
        boundaryNodes.add(node);
      }
    }
    return boundaryNodes;
  }

  @Override
  public Iterator<Node> iterator() {
    return this.nodeMap.values().iterator();
  }

  public void print(final PrintStream out) {
    for (final Node node : this) {
      node.print(out);
    }
  }

  public Collection<Node> values() {
    return this.nodeMap.values();
  }
}
