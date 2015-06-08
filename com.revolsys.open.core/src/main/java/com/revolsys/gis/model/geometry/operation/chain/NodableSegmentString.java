package com.revolsys.gis.model.geometry.operation.chain;

import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * An interface for classes which support adding nodes to
 * a segment string.
 *
 * @author Martin Davis
 */
public interface NodableSegmentString extends SegmentString {
  /**
   * Adds an intersection node for a given point and segment to this segment string.
   *
   * @param intPt the location of the intersection
   * @param segmentIndex the index of the segment containing the intersection
   */
  public void addIntersection(Coordinates intPt, int segmentIndex);
}
