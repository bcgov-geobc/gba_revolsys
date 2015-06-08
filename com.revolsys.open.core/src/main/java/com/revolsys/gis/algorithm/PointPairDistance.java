/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.revolsys.gis.algorithm;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Contains a pair of points and the distance between them. Provides methods to
 * update with a new point pair with either maximum or minimum distance.
 */
public class PointPairDistance {

  private double distance = Double.NaN;

  private boolean isNull = true;

  private final Coordinate[] pt = {
    new Coordinate(), new Coordinate()
  };

  public PointPairDistance() {
  }

  public Coordinate getCoordinate(final int i) {
    return this.pt[i];
  }

  public Coordinate[] getCoordinates() {
    return this.pt;
  }

  public double getDistance() {
    return this.distance;
  }

  public void initialize() {
    this.isNull = true;
  }

  public void initialize(final Coordinate p0, final Coordinate p1) {
    this.pt[0].setCoordinate(p0);
    this.pt[1].setCoordinate(p1);
    this.distance = p0.distance(p1);
    this.isNull = false;
  }

  /**
   * Initializes the points, avoiding recomputing the distance.
   *
   * @param p0
   * @param p1
   * @param distance the distance between p0 and p1
   */
  private void initialize(final Coordinate p0, final Coordinate p1, final double distance) {
    this.pt[0].setCoordinate(p0);
    this.pt[1].setCoordinate(p1);
    this.distance = distance;
    this.isNull = false;
  }

  public void setMaximum(final Coordinate p0, final Coordinate p1) {
    if (this.isNull) {
      initialize(p0, p1);
      return;
    }
    final double dist = p0.distance(p1);
    if (dist > this.distance) {
      initialize(p0, p1, dist);
    }
  }

  public void setMaximum(final PointPairDistance ptDist) {
    setMaximum(ptDist.pt[0], ptDist.pt[1]);
  }

  public void setMinimum(final Coordinate p0, final Coordinate p1) {
    if (this.isNull) {
      initialize(p0, p1);
      return;
    }
    final double dist = p0.distance(p1);
    if (dist < this.distance) {
      initialize(p0, p1, dist);
    }
  }

  public void setMinimum(final PointPairDistance ptDist) {
    setMinimum(ptDist.pt[0], ptDist.pt[1]);
  }
}
