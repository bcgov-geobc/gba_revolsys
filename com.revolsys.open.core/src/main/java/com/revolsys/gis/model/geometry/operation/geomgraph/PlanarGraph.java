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
package com.revolsys.gis.model.geometry.operation.geomgraph;

/**
 * @version 1.7
 */
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Location;

/**
 * The computation of the <code>IntersectionMatrix</code> relies on the use of a
 * structure called a "topology graph". The topology graph contains nodes and
 * edges corresponding to the nodes and line segments of a <code>Geometry</code>
 * . Each node and edge in the graph is labeled with its topological location
 * relative to the source geometry.
 * <P>
 * Note that there is no requirement that points of self-intersection be a
 * vertex. Thus to obtain a correct topology graph, <code>Geometry</code>s must
 * be self-noded before constructing their graphs.
 * <P>
 * Two fundamental operations are supported by topology graphs:
 * <UL>
 * <LI>Computing the intersections between all the edges and nodes of a single
 * graph
 * <LI>Computing the intersections between the edges and nodes of two different
 * graphs
 * </UL>
 *
 * @version 1.7
 */
public class PlanarGraph {
  /**
   * For nodes in the Collection, link the DirectedEdges at the node that are in
   * the result. This allows clients to link only a subset of nodes in the
   * graph, for efficiency (because they know that only a subset is of
   * interest).
   */
  public static void linkResultDirectedEdges(final Collection nodes) {
    for (final Iterator nodeit = nodes.iterator(); nodeit.hasNext();) {
      final Node node = (Node)nodeit.next();
      ((DirectedEdgeStar)node.getEdges()).linkResultDirectedEdges();
    }
  }

  protected List edges = new ArrayList();

  protected NodeMap nodes;

  protected List edgeEndList = new ArrayList();

  public PlanarGraph() {
    this.nodes = new NodeMap(new NodeFactory());
  }

  public PlanarGraph(final NodeFactory nodeFact) {
    this.nodes = new NodeMap(nodeFact);
  }

  public void add(final EdgeEnd e) {
    this.nodes.add(e);
    this.edgeEndList.add(e);
  }

  /**
   * Add a set of edges to the graph. For each edge two DirectedEdges will be
   * created. DirectedEdges are NOT linked by this method.
   */
  public void addEdges(final List edgesToAdd) {
    // create all the nodes for the edges
    for (final Iterator it = edgesToAdd.iterator(); it.hasNext();) {
      final Edge e = (Edge)it.next();
      this.edges.add(e);

      final DirectedEdge de1 = new DirectedEdge(e, true);
      final DirectedEdge de2 = new DirectedEdge(e, false);
      de1.setSym(de2);
      de2.setSym(de1);

      add(de1);
      add(de2);
    }
  }

  public Node addNode(final Coordinates coord) {
    return this.nodes.addNode(coord);
  }

  public Node addNode(final Node node) {
    return this.nodes.addNode(node);
  }

  void debugPrint(final Object o) {
    System.out.print(o);
  }

  void debugPrintln(final Object o) {
    System.out.println(o);
  }

  /**
   * @return the node if found; null otherwise
   */
  public Node find(final Coordinates coord) {
    return this.nodes.find(coord);
  }

  /**
   * Returns the edge whose first two coordinates are p0 and p1
   *
   * @return the edge, if found <code>null</code> if the edge was not found
   */
  public Edge findEdge(final Coordinates p0, final Coordinates p1) {
    for (int i = 0; i < this.edges.size(); i++) {
      final Edge e = (Edge)this.edges.get(i);
      final CoordinatesList eCoord = e.getCoordinates();
      if (p0.equals(eCoord.get(0)) && p1.equals(eCoord.get(1))) {
        return e;
      }
    }
    return null;
  }

  /**
   * Returns the EdgeEnd which has edge e as its base edge (MD 18 Feb 2002 -
   * this should return a pair of edges)
   *
   * @return the edge, if found <code>null</code> if the edge was not found
   */
  public EdgeEnd findEdgeEnd(final Edge e) {
    for (final Object element : getEdgeEnds()) {
      final EdgeEnd ee = (EdgeEnd)element;
      if (ee.getEdge() == e) {
        return ee;
      }
    }
    return null;
  }

  /**
   * Returns the edge which starts at p0 and whose first segment is parallel to
   * p1
   *
   * @return the edge, if found <code>null</code> if the edge was not found
   */
  public Edge findEdgeInSameDirection(final Coordinates p0, final Coordinates p1) {
    for (int i = 0; i < this.edges.size(); i++) {
      final Edge e = (Edge)this.edges.get(i);

      final CoordinatesList eCoord = e.getCoordinates();
      if (matchInSameDirection(p0, p1, eCoord.get(0), eCoord.get(1))) {
        return e;
      }

      if (matchInSameDirection(p0, p1, eCoord.get(eCoord.size() - 1), eCoord.get(eCoord.size() - 2))) {
        return e;
      }
    }
    return null;
  }

  public Collection<DirectedEdge> getEdgeEnds() {
    return this.edgeEndList;
  }

  public Iterator getEdgeIterator() {
    return this.edges.iterator();
  }

  public Iterator getNodeIterator() {
    return this.nodes.iterator();
  }

  public Collection<Node> getNodes() {
    return this.nodes.values();
  }

  protected void insertEdge(final Edge e) {
    this.edges.add(e);
  }

  public boolean isBoundaryNode(final int geomIndex, final Coordinates coord) {
    final Node node = this.nodes.find(coord);
    if (node == null) {
      return false;
    }
    final Label label = node.getLabel();
    if (label != null && label.getLocation(geomIndex) == Location.BOUNDARY) {
      return true;
    }
    return false;
  }

  /**
   * Link the DirectedEdges at the nodes of the graph. This allows clients to
   * link only a subset of nodes in the graph, for efficiency (because they know
   * that only a subset is of interest).
   */
  public void linkAllDirectedEdges() {
    for (final Iterator nodeit = this.nodes.iterator(); nodeit.hasNext();) {
      final Node node = (Node)nodeit.next();
      ((DirectedEdgeStar)node.getEdges()).linkAllDirectedEdges();
    }
  }

  /**
   * Link the DirectedEdges at the nodes of the graph. This allows clients to
   * link only a subset of nodes in the graph, for efficiency (because they know
   * that only a subset is of interest).
   */
  public void linkResultDirectedEdges() {
    for (final Iterator nodeit = this.nodes.iterator(); nodeit.hasNext();) {
      final Node node = (Node)nodeit.next();
      ((DirectedEdgeStar)node.getEdges()).linkResultDirectedEdges();
    }
  }

  /**
   * The coordinate pairs match if they define line segments lying in the same
   * direction. E.g. the segments are parallel and in the same quadrant (as
   * opposed to parallel and opposite!).
   */
  private boolean matchInSameDirection(final Coordinates p0, final Coordinates p1,
    final Coordinates ep0, final Coordinates ep1) {
    if (!p0.equals(ep0)) {
      return false;
    }

    if (CoordinatesUtil.orientationIndex(p0, p1, ep1) == CGAlgorithms.COLLINEAR
      && Quadrant.quadrant(p0, p1) == Quadrant.quadrant(ep0, ep1)) {
      return true;
    }
    return false;
  }

  public void printEdges(final PrintStream out) {
    out.println("Edges:");
    for (int i = 0; i < this.edges.size(); i++) {
      out.println("edge " + i + ":");
      final Edge e = (Edge)this.edges.get(i);
      e.print(out);
      e.eiList.print(out);
    }
  }

}
