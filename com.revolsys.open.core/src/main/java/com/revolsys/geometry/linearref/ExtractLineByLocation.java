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

package com.revolsys.geometry.linearref;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Assert;

/**
 * Extracts the subline of a linear {@link Geometry} between
 * two {@link LinearLocation}s on the line.
 */
class ExtractLineByLocation {
  /**
   * Computes the subline of a {@link LineString} between
   * two {@link LinearLocation}s on the line.
   * If the start location is after the end location,
   * the computed geometry is reversed.
   *
   * @param line the line to use as the baseline
   * @param start the start location
   * @param end the end location
   * @return the extracted subline
   */
  public static Geometry extract(final Geometry line, final LinearLocation start,
    final LinearLocation end) {
    final ExtractLineByLocation ls = new ExtractLineByLocation(line);
    return ls.extract(start, end);
  }

  private final Geometry line;

  public ExtractLineByLocation(final Geometry line) {
    this.line = line;
  }

  /**
   * Assumes input is valid (e.g. start <= end)
   *
   * @param start
   * @param end
   * @return a linear geometry
   */
  private Geometry computeLinear(final LinearLocation start, final LinearLocation end) {
    final LinearGeometryBuilder builder = new LinearGeometryBuilder(this.line.getGeometryFactory());
    builder.setFixInvalidLines(true);

    if (!start.isVertex()) {
      builder.add(start.getCoordinate(this.line));
    }

    for (final LinearIterator it = new LinearIterator(this.line, start); it.hasNext(); it.next()) {
      if (end.compareLocationValues(it.getComponentIndex(), it.getVertexIndex(), 0.0) < 0) {
        break;
      }

      final Point pt = it.getSegmentStart();
      builder.add(pt);
      if (it.isEndOfLine()) {
        builder.endLine();
      }
    }
    if (!end.isVertex()) {
      builder.add(end.getCoordinate(this.line));
    }

    return builder.getGeometry();
  }

  /**
   * Extracts a subline of the input.
   * If <code>end < start</code> the linear geometry computed will be reversed.
   *
   * @param start the start location
   * @param end the end location
   * @return a linear geometry
   */
  public Geometry extract(final LinearLocation start, final LinearLocation end) {
    if (end.compareTo(start) < 0) {
      return reverse(computeLinear(end, start));
    }
    return computeLinear(start, end);
  }

  private Geometry reverse(final Geometry linear) {
    if (linear instanceof LineString) {
      return ((LineString)linear).reverse();
    }
    if (linear instanceof MultiLineString) {
      return ((MultiLineString)linear).reverse();
    }
    Assert.shouldNeverReachHere("non-linear geometry encountered");
    return null;
  }

  /**
   * Computes a valid and normalized location
   * compatible with the values in a LinearIterator.
   * (I.e. segmentFractions of 1.0 are converted to the next highest coordinate index)
   */
  /*
   * private LinearLocation normalize(LinearLocation loc) { int componentIndex =
   * loc.getComponentIndex(); int segmentIndex = loc.getSegmentIndex(); double
   * segmentFraction = loc.getSegmentFraction(); if (segmentFraction < 0.0) {
   * segmentFraction = 0.0; } if (segmentFraction > 1.0) { segmentFraction =
   * 1.0; } if (componentIndex < 0) { componentIndex = 0; segmentIndex = 0;
   * segmentFraction = 0.0; } if (segmentIndex < 0) { segmentIndex = 0;
   * segmentFraction = 0.0; } if (segmentFraction == 1.0) { segmentFraction =
   * 0.0; segmentIndex += 1; } return new LinearLocation(componentIndex,
   * segmentIndex, segmentFraction); }
   */
}
