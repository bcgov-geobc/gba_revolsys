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
package com.revolsys.geometry.index.chain;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;
import com.revolsys.geometry.util.BoundingBoxUtil;

/**
 * Monotone Chains are a way of partitioning the segments of a linestring to
 * allow for fast searching of intersections.
 * They have the following properties:
 * <ol>
 * <li>the segments within a monotone chain never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is equal to the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * <p>
 * Property 2 allows
 * an efficient binary search to be used to find the intersection points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * <p>
 * One of the goals of this implementation of MonotoneChains is to be
 * as space and time efficient as possible. One design choice that aids this
 * is that a MonotoneChain is based on a subarray of a list of points.
 * This means that new arrays of points (potentially very large) do not
 * have to be allocated.
 * <p>
 *
 * MonotoneChains support the following kinds of queries:
 * <ul>
 * <li>BoundingBoxDoubleGf select: determine all the segments in the chain which
 * intersect a given envelope
 * <li>Overlap: determine all the pairs of segments in two chains whose
 * envelopes overlap
 * </ul>
 *
 * This implementation of MonotoneChains uses the concept of internal iterators
 * ({@link MonotoneChainSelectAction} and {@link MonotoneChainOverlapAction})
 * to return the results for queries.
 * This has time and space advantages, since it
 * is not necessary to build lists of instantiated objects to represent the segments
 * returned by the query.
 * Queries made in this manner are thread-safe.
 *
 * @version 1.7
 */
public class MonotoneChain {

  private Object context = null;// user-defined information

  private BoundingBoxDoubleGf env = null;

  private int id;// useful for optimizing chain comparisons

  private final LineString points;

  private final int start, end;

  public MonotoneChain(final LineString pts, final int start, final int end, final Object context) {
    this.points = pts;
    this.start = start;
    this.end = end;
    this.context = context;
  }

  private void computeOverlaps(final int start0, final int end0, final MonotoneChain mc,
    final int start1, final int end1, final MonotoneChainOverlapAction mco) {
    final Point p00 = this.points.getPoint(start0);
    final Point p01 = this.points.getPoint(end0);
    final Point p10 = mc.points.getPoint(start1);
    final Point p11 = mc.points.getPoint(end1);
    // terminating condition for the recursion
    if (end0 - start0 == 1 && end1 - start1 == 1) {
      mco.overlap(this, start0, mc, start1);
      return;
    }
    // nothing to do if the envelopes of these chains don't overlap
    if (BoundingBoxUtil.intersects(p00, p01, p10, p11)) {

      // the chains overlap, so split each in half and iterate (binary search)
      final int mid0 = (start0 + end0) / 2;
      final int mid1 = (start1 + end1) / 2;

      // mid != start or end (since we checked above for end - start <= 1)
      // check terminating conditions before recursing
      if (start0 < mid0) {
        if (start1 < mid1) {
          computeOverlaps(start0, mid0, mc, start1, mid1, mco);
        }
        if (mid1 < end1) {
          computeOverlaps(start0, mid0, mc, mid1, end1, mco);
        }
      }
      if (mid0 < end0) {
        if (start1 < mid1) {
          computeOverlaps(mid0, end0, mc, start1, mid1, mco);
        }
        if (mid1 < end1) {
          computeOverlaps(mid0, end0, mc, mid1, end1, mco);
        }
      }
    }
  }

  /**
   * Determine all the line segments in two chains which may overlap, and process them.
   * <p>
   * The monotone chain search algorithm attempts to optimize
   * performance by not calling the overlap action on chain segments
   * which it can determine do not overlap.
   * However, it *may* call the overlap action on segments
   * which do not actually interact.
   * This saves on the overhead of checking intersection
   * each time, since clients may be able to do this more efficiently.
   *
   * @param searchEnv the search envelope
   * @param mco the overlap action to execute on selected segments
   */
  public void computeOverlaps(final MonotoneChain mc, final MonotoneChainOverlapAction mco) {
    computeOverlaps(this.start, this.end, mc, mc.start, mc.end, mco);
  }

  private void computeSelect(final BoundingBox searchEnv, final int start0, final int end0,
    final MonotoneChainSelectAction mcs) {
    final Point p0 = this.points.getPoint(start0);
    final Point p1 = this.points.getPoint(end0);
    mcs.tempEnv1 = new BoundingBoxDoubleGf(p0, p1);

    // Debug.println("trying:" + p0 + p1 + " [ " + start0 + ", " + end0 + " ]");
    // terminating condition for the recursion
    if (end0 - start0 == 1) {
      // Debug.println("computeSelect:" + p0 + p1);
      mcs.select(this, start0);
      return;
    }
    // nothing to do if the envelopes don't overlap
    if (!searchEnv.intersects(mcs.tempEnv1)) {
      return;
    }

    // the chains overlap, so split each in half and iterate (binary search)
    final int mid = (start0 + end0) / 2;

    // Assert: mid != start or end (since we checked above for end - start <= 1)
    // check terminating conditions before recursing
    if (start0 < mid) {
      computeSelect(searchEnv, start0, mid, mcs);
    }
    if (mid < end0) {
      computeSelect(searchEnv, mid, end0, mcs);
    }
  }

  public Object getContext() {
    return this.context;
  }

  /**
   * Return the subsequence of coordinates forming this chain.
   * Allocates a new array to hold the Coordinates
   */
  public Point[] getCoordinates() {
    final Point coord[] = new Point[this.end - this.start + 1];
    int index = 0;
    for (int i = this.start; i <= this.end; i++) {
      coord[index++] = this.points.getPoint(i);
    }
    return coord;
  }

  public int getEndIndex() {
    return this.end;
  }

  public BoundingBoxDoubleGf getEnvelope() {
    if (this.env == null) {
      final Point p0 = this.points.getPoint(this.start);
      final Point p1 = this.points.getPoint(this.end);
      this.env = new BoundingBoxDoubleGf(p0, p1);
    }
    return this.env;
  }

  public int getId() {
    return this.id;
  }

  /**
   * Gets the line segment starting at <code>index</code>
   *
   * @param index index of segment
   * @param ls line segment to extract into
   */
  public LineSegment getLineSegment(final int index) {
    return new LineSegmentDouble(this.points.getPoint(index), this.points.getPoint(index + 1));
  }

  public int getStartIndex() {
    return this.start;
  }

  /**
   * Determine all the line segments in the chain whose envelopes overlap
   * the searchEnvelope, and process them.
   * <p>
   * The monotone chain search algorithm attempts to optimize
   * performance by not calling the select action on chain segments
   * which it can determine are not in the search envelope.
   * However, it *may* call the select action on segments
   * which do not intersect the search envelope.
   * This saves on the overhead of checking envelope intersection
   * each time, since clients may be able to do this more efficiently.
   *
   * @param searchEnv the search envelope
   * @param mcs the select action to execute on selected segments
   */
  public void select(final BoundingBox searchEnv, final MonotoneChainSelectAction mcs) {
    computeSelect(searchEnv, this.start, this.end, mcs);
  }

  public void setId(final int id) {
    this.id = id;
  }
}
