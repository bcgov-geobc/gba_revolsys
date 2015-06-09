package com.revolsys.gis.model.geometry.operation.buffer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdge;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdgeStar;
import com.revolsys.gis.model.geometry.operation.geomgraph.Label;
import com.revolsys.gis.model.geometry.operation.geomgraph.Node;
import com.revolsys.gis.model.geometry.operation.geomgraph.Position;
import com.revolsys.gis.model.geometry.util.TopologyException;
import com.vividsolutions.jts.geom.Envelope;

/**
 * @version 1.7
 */

/**
 * A connected subset of the graph of {@link DirectedEdge}s and {@link Node}s.
 * Its edges will generate either
 * <ul>
 * <li>a single polygon in the complete buffer, with zero or more holes, or
 * <li>one or more connected holes
 * </ul>
 *
 * @version 1.7
 */
public class BufferSubgraph implements Comparable {
  private final RightmostEdgeFinder finder;

  private final List dirEdgeList = new ArrayList();

  private final List nodes = new ArrayList();

  private Coordinates rightMostCoord = null;

  private Envelope env = null;

  public BufferSubgraph() {
    this.finder = new RightmostEdgeFinder();
  }

  /**
   * Adds the argument node and all its out edges to the subgraph
   *
   * @param node the node to add
   * @param nodeStack the current set of nodes being traversed
   */
  private void add(final Node node, final Stack nodeStack) {
    node.setVisited(true);
    this.nodes.add(node);
    for (final Iterator i = ((DirectedEdgeStar)node.getEdges()).iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      this.dirEdgeList.add(de);
      final DirectedEdge sym = de.getSym();
      final Node symNode = sym.getNode();
      /**
       * NOTE: this is a depth-first traversal of the graph. This will cause a
       * large depth of recursion. It might be better to do a breadth-first
       * traversal.
       */
      if (!symNode.isVisited()) {
        nodeStack.push(symNode);
      }
    }
  }

  /**
   * Adds all nodes and edges reachable from this node to the subgraph. Uses an
   * explicit stack to avoid a large depth of recursion.
   *
   * @param node a node known to be in the subgraph
   */
  private void addReachable(final Node startNode) {
    final Stack nodeStack = new Stack();
    nodeStack.add(startNode);
    while (!nodeStack.empty()) {
      final Node node = (Node)nodeStack.pop();
      add(node, nodeStack);
    }
  }

  private void clearVisitedEdges() {
    for (final Iterator it = this.dirEdgeList.iterator(); it.hasNext();) {
      final DirectedEdge de = (DirectedEdge)it.next();
      de.setVisited(false);
    }
  }

  /**
   * BufferSubgraphs are compared on the x-value of their rightmost Coordinate.
   * This defines a partial ordering on the graphs such that:
   * <p>
   * g1 >= g2 <==> Ring(g2) does not contain Ring(g1)
   * <p>
   * where Polygon(g) is the buffer polygon that is built from g.
   * <p>
   * This relationship is used to sort the BufferSubgraphs so that shells are
   * guaranteed to be built before holes.
   */
  @Override
  public int compareTo(final Object o) {
    final BufferSubgraph graph = (BufferSubgraph)o;
    if (this.rightMostCoord.getX() < graph.rightMostCoord.getX()) {
      return -1;
    }
    if (this.rightMostCoord.getX() > graph.rightMostCoord.getX()) {
      return 1;
    }
    return 0;
  }

  public void computeDepth(final int outsideDepth) {
    clearVisitedEdges();
    // find an outside edge to assign depth to
    final DirectedEdge de = this.finder.getEdge();
    final Node n = de.getNode();
    final Label label = de.getLabel();
    // right side of line returned by finder is on the outside
    de.setEdgeDepths(Position.RIGHT, outsideDepth);
    copySymDepths(de);

    // computeNodeDepth(n, de);
    computeDepths(de);
  }

  /**
   * Compute depths for all dirEdges via breadth-first traversal of nodes in
   * graph
   *
   * @param startEdge edge to start processing with
   */
  // <FIX> MD - use iteration & queue rather than recursion, for speed and
  // robustness
  private void computeDepths(final DirectedEdge startEdge) {
    final Set nodesVisited = new HashSet();
    final LinkedList nodeQueue = new LinkedList();

    final Node startNode = startEdge.getNode();
    nodeQueue.addLast(startNode);
    nodesVisited.add(startNode);
    startEdge.setVisited(true);

    while (!nodeQueue.isEmpty()) {
      // System.out.println(nodes.size() + " queue: " + nodeQueue.size());
      final Node n = (Node)nodeQueue.removeFirst();
      nodesVisited.add(n);
      // compute depths around node, starting at this edge since it has depths
      // assigned
      computeNodeDepth(n);

      // add all adjacent nodes to process queue,
      // unless the node has been visited already
      for (final Iterator i = ((DirectedEdgeStar)n.getEdges()).iterator(); i.hasNext();) {
        final DirectedEdge de = (DirectedEdge)i.next();
        final DirectedEdge sym = de.getSym();
        if (sym.isVisited()) {
          continue;
        }
        final Node adjNode = sym.getNode();
        if (!nodesVisited.contains(adjNode)) {
          nodeQueue.addLast(adjNode);
          nodesVisited.add(adjNode);
        }
      }
    }
  }

  private void computeNodeDepth(final Node n) {
    // find a visited dirEdge to start at
    DirectedEdge startEdge = null;
    for (final Iterator i = ((DirectedEdgeStar)n.getEdges()).iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      if (de.isVisited() || de.getSym().isVisited()) {
        startEdge = de;
        break;
      }
    }
    // MD - testing Result: breaks algorithm
    // if (startEdge == null) return;

    // only compute string append if assertion would fail
    if (startEdge == null) {
      throw new TopologyException("unable to find edge to compute depths at " + n.getCoordinate());
    }

    ((DirectedEdgeStar)n.getEdges()).computeDepths(startEdge);

    // copy depths to sym edges
    for (final Iterator i = ((DirectedEdgeStar)n.getEdges()).iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      de.setVisited(true);
      copySymDepths(de);
    }
  }

  private void copySymDepths(final DirectedEdge de) {
    final DirectedEdge sym = de.getSym();
    sym.setDepth(Position.LEFT, de.getDepth(Position.RIGHT));
    sym.setDepth(Position.RIGHT, de.getDepth(Position.LEFT));
  }

  /**
   * Creates the subgraph consisting of all edges reachable from this node.
   * Finds the edges in the graph and the rightmost coordinate.
   *
   * @param node a node to start the graph traversal from
   */
  public void create(final Node node) {
    addReachable(node);
    this.finder.findEdge(this.dirEdgeList);
    this.rightMostCoord = this.finder.getCoordinate();
  }

  /**
   * Find all edges whose depths indicates that they are in the result area(s).
   * Since we want polygon shells to be oriented CW, choose dirEdges with the
   * interior of the result on the RHS. Mark them as being in the result.
   * Interior Area edges are the result of dimensional collapses. They do not
   * form part of the result area boundary.
   */
  public void findResultEdges() {
    for (final Iterator it = this.dirEdgeList.iterator(); it.hasNext();) {
      final DirectedEdge de = (DirectedEdge)it.next();
      /**
       * Select edges which have an interior depth on the RHS and an exterior
       * depth on the LHS. Note that because of weird rounding effects there may
       * be edges which have negative depths! Negative depths count as
       * "outside".
       */
      // <FIX> - handle negative depths
      if (de.getDepth(Position.RIGHT) >= 1 && de.getDepth(Position.LEFT) <= 0
          && !de.isInteriorAreaEdge()) {
        de.setInResult(true);
        // Debug.print("in result "); Debug.println(de);
      }
    }
  }

  public List getDirectedEdges() {
    return this.dirEdgeList;
  }

  /**
   * Computes the envelope of the edges in the subgraph. The envelope is cached
   * after being computed.
   *
   * @return the envelope of the graph.
   */
  public Envelope getEnvelope() {
    if (this.env == null) {
      final Envelope edgeEnv = new Envelope();
      for (final Iterator it = this.dirEdgeList.iterator(); it.hasNext();) {
        final DirectedEdge dirEdge = (DirectedEdge)it.next();
        final CoordinatesList pts = dirEdge.getEdge().getCoordinates();
        for (int i = 0; i < pts.size() - 1; i++) {
          edgeEnv.expandToInclude(pts.getCoordinate(i));
        }
      }
      this.env = edgeEnv;
    }
    return this.env;
  }

  public List getNodes() {
    return this.nodes;
  }

  /**
   * Gets the rightmost coordinate in the edges of the subgraph
   */
  public Coordinates getRightmostCoordinate() {
    return this.rightMostCoord;
  }

}
