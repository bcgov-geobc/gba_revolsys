package com.revolsys.gis.model.geometry.operation.chain;

import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.impl.BoundingBox;

/**
 * The action for the internal iterator for performing envelope select queries
 * on a MonotoneChain
 *
 * @version 1.7
 */
public class MonotoneChainSelectAction {
  // these envelopes are used during the MonotoneChain search process
  BoundingBox tempEnv1 = new BoundingBox();

  LineSegment selectedSegment = new LineSegment();

  /**
   * This is a convenience function which can be overridden to obtain the actual
   * line segment which is selected.
   *
   * @param seg
   */
  public void select(final LineSegment seg) {
  }

  /**
   * This function can be overridden if the original chain is needed.
   */
  public void select(final MonotoneChain mc, final int start) {
    mc.getLineSegment(start, this.selectedSegment);
    select(this.selectedSegment);
  }
}
