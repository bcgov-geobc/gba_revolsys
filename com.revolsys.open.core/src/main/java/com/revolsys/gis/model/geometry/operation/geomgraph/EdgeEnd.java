package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.io.PrintStream;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.vividsolutions.jts.algorithm.BoundaryNodeRule;
import com.vividsolutions.jts.util.Assert;

/**
 * Models the end of an edge incident on a node. EdgeEnds have a direction
 * determined by the direction of the ray from the initial point to the next
 * point. EdgeEnds are comparable under the ordering
 * "a has a greater angle with the x-axis than b". This ordering is used to sort
 * EdgeEnds around a node.
 *
 * @version 1.7
 */
public class EdgeEnd implements Comparable {
  protected Edge edge; // the parent edge of this edge end

  protected Label label;

  private Node node; // the node this edge end originates at

  private Coordinates p0, p1; // points of initial line segment

  private double dx, dy; // the direction vector for this edge from its starting
  // point

  private int quadrant;

  protected EdgeEnd(final Edge edge) {
    this.edge = edge;
  }

  public EdgeEnd(final Edge edge, final Coordinates p0, final Coordinates p1) {
    this(edge, p0, p1, null);
  }

  public EdgeEnd(final Edge edge, final Coordinates p0, final Coordinates p1, final Label label) {
    this(edge);
    init(p0, p1);
    this.label = label;
  }

  /**
   * Implements the total order relation:
   * <p>
   * a has a greater angle with the positive x-axis than b
   * <p>
   * Using the obvious algorithm of simply computing the angle is not robust,
   * since the angle calculation is obviously susceptible to roundoff. A robust
   * algorithm is: - first compare the quadrant. If the quadrants are different,
   * it it trivial to determine which vector is "greater". - if the vectors lie
   * in the same quadrant, the computeOrientation function can be used to decide
   * the relative orientation of the vectors.
   */
  public int compareDirection(final EdgeEnd e) {
    if (this.dx == e.dx && this.dy == e.dy) {
      return 0;
    }
    // if the rays are in different quadrants, determining the ordering is
    // trivial
    if (this.quadrant > e.quadrant) {
      return 1;
    }
    if (this.quadrant < e.quadrant) {
      return -1;
    }
    // vectors are in the same quadrant - check relative orientation of
    // direction vectors
    // this is > e if it is CCW of e
    return CoordinatesUtil.orientationIndex(e.p0, e.p1, this.p1);
  }

  @Override
  public int compareTo(final Object obj) {
    final EdgeEnd e = (EdgeEnd)obj;
    return compareDirection(e);
  }

  public void computeLabel(final BoundaryNodeRule boundaryNodeRule) {
    // subclasses should override this if they are using labels
  }

  public Coordinates getCoordinate() {
    return this.p0;
  }

  public Coordinates getDirectedCoordinate() {
    return this.p1;
  }

  public double getDx() {
    return this.dx;
  }

  public double getDy() {
    return this.dy;
  }

  public Edge getEdge() {
    return this.edge;
  }

  public Label getLabel() {
    return this.label;
  }

  public Node getNode() {
    return this.node;
  }

  public int getQuadrant() {
    return this.quadrant;
  }

  protected void init(final Coordinates p0, final Coordinates p1) {
    this.p0 = p0;
    this.p1 = p1;
    this.dx = p1.getX() - p0.getX();
    this.dy = p1.getY() - p0.getY();
    this.quadrant = Quadrant.quadrant(this.dx, this.dy);
    Assert.isTrue(!(this.dx == 0 && this.dy == 0), "EdgeEnd with identical endpoints found");
  }

  public void print(final PrintStream out) {
    final double angle = Math.atan2(this.dy, this.dx);
    final String className = getClass().getName();
    final int lastDotPos = className.lastIndexOf('.');
    final String name = className.substring(lastDotPos + 1);
    out.print("  " + name + ": " + this.p0 + " - " + this.p1 + " " + this.quadrant + ":" + angle
      + "   " + this.label);
  }

  public void setNode(final Node node) {
    this.node = node;
  }
}
