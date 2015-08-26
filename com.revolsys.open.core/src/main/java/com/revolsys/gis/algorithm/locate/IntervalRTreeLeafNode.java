package com.revolsys.gis.algorithm.locate;

import java.util.function.Consumer;

public class IntervalRTreeLeafNode<V> extends IntervalRTreeNode<V> {
  private final V item;

  public IntervalRTreeLeafNode(final double min, final double max, final V item) {
    super(min, max);
    this.item = item;
  }

  @Override
  public void query(final double queryMin, final double queryMax, final Consumer<V> visitor) {
    if (intersects(queryMin, queryMax)) {
      visitor.accept(this.item);
    }
  }
}
