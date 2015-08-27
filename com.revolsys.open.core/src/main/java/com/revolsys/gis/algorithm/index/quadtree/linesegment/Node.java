package com.revolsys.gis.algorithm.index.quadtree.linesegment;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.DoubleBits;

public class Node extends NodeBase {
  public static Envelope computeKey(final Coordinate point, final int level,
    final Envelope itemEnv) {
    final double quadSize = DoubleBits.powerOf2(level);
    point.x = Math.floor(itemEnv.getMinX() / quadSize) * quadSize;
    point.y = Math.floor(itemEnv.getMinY() / quadSize) * quadSize;
    return new Envelope(point.x, point.x + quadSize, point.y, point.y + quadSize);
  }

  public static Envelope computeKey(final int level, final Coordinate point,
    final Envelope itemEnv) {
    final double quadSize = DoubleBits.powerOf2(level);
    final double x = Math.floor(itemEnv.getMinX() / quadSize) * quadSize;
    final double y = Math.floor(itemEnv.getMinY() / quadSize) * quadSize;
    point.x = x;
    point.y = y;
    return new Envelope(x, x + quadSize, y, y + quadSize);
  }

  public static int computeQuadLevel(final Envelope env) {
    final double dx = env.getWidth();
    final double dy = env.getHeight();
    final double dMax = dx > dy ? dx : dy;
    final int level = DoubleBits.exponent(dMax) + 1;
    return level;
  }

  public static Node createExpanded(final Node node, final Envelope addEnv) {
    final Envelope expandEnv = new Envelope(addEnv);
    if (node != null) {
      expandEnv.expandToInclude(node.env);
    }

    final Node largerNode = createNode(expandEnv);
    if (node != null) {
      largerNode.insertNode(node);
    }
    return largerNode;
  }

  public static Node createNode(final Envelope itemEnv) {
    final Coordinate point = new Coordinate();
    int level = computeQuadLevel(itemEnv);
    Envelope nodeEnvelope = computeKey(level, point, itemEnv);
    // MD - would be nice to have a non-iterative form of this algorithm
    while (!nodeEnvelope.contains(itemEnv)) {
      level += 1;
      nodeEnvelope = computeKey(level, point, itemEnv);
    }

    final Node node = new Node(nodeEnvelope, level);
    return node;
  }

  private final Coordinates centre;

  private final Envelope env;

  private final int level;

  public Node(final Envelope env, final int level) {
    this.env = env;
    this.level = level;
    final double x = (env.getMinX() + env.getMaxX()) / 2;
    final double y = (env.getMinY() + env.getMaxY()) / 2;
    this.centre = new DoubleCoordinates(x, y);
  }

  private Node createSubnode(final int index) {
    double minX = 0.0;
    double maxX = 0.0;
    double minY = 0.0;
    double maxY = 0.0;

    switch (index) {
      case 0:
        minX = this.env.getMinX();
        maxX = this.centre.getX();
        minY = this.env.getMinY();
        maxY = this.centre.getY();
      break;
      case 1:
        minX = this.centre.getX();
        maxX = this.env.getMaxX();
        minY = this.env.getMinY();
        maxY = this.centre.getY();
      break;
      case 2:
        minX = this.env.getMinX();
        maxX = this.centre.getX();
        minY = this.centre.getY();
        maxY = this.env.getMaxY();
      break;
      case 3:
        minX = this.centre.getX();
        maxX = this.env.getMaxX();
        minY = this.centre.getY();
        maxY = this.env.getMaxY();
      break;
    }
    final Envelope envelope = new Envelope(minX, maxX, minY, maxY);
    final Node node = new Node(envelope, this.level - 1);
    return node;
  }

  public NodeBase find(final Envelope searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, this.centre);
    if (subnodeIndex == -1) {
      return this;
    }
    if (getNode(subnodeIndex) != null) {
      final Node node = getNode(subnodeIndex);
      return node.find(searchEnv);
    }
    return this;
  }

  public Envelope getEnvelope() {
    return this.env;
  }

  public Node getNode(final Envelope searchEnv) {
    final int subnodeIndex = getSubnodeIndex(searchEnv, this.centre);
    if (subnodeIndex != -1) {
      final Node node = getSubnode(subnodeIndex);
      return node.getNode(searchEnv);
    } else {
      return this;
    }
  }

  private Node getSubnode(final int index) {
    if (getNode(index) == null) {
      setNode(index, createSubnode(index));
    }
    return getNode(index);
  }

  void insertNode(final Node node) {
    final int index = getSubnodeIndex(node.env, this.centre);
    if (node.level == this.level - 1) {
      setNode(index, node);
    } else {
      final Node childNode = createSubnode(index);
      childNode.insertNode(node);
      setNode(index, childNode);
    }
  }

  @Override
  protected boolean isSearchMatch(final Envelope searchEnv) {
    return this.env.intersects(searchEnv);
  }

}
