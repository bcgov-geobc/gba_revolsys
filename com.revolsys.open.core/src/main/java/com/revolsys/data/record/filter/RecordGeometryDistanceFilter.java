/*
 * $URL:$
 * $Author:$
 * $Date:$
 * $Revision:$

 * Copyright 2004-2007 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.data.record.filter;

import java.util.function.Predicate;

import com.revolsys.data.record.Record;
import com.vividsolutions.jts.geom.Geometry;

public class RecordGeometryDistanceFilter implements Predicate<Record> {
  /** The geometry to compare the data objects to to. */
  private final Geometry geometry;

  /** The maximum maxDistance the object can be from the source geometry. */
  private final double maxDistance;

  /**
   * Construct a new LineStringLessThanDistanceFilter.
   *
   * @param geometry The geometry to compare the data objects to to.
   * @param maxDistance
   */
  public RecordGeometryDistanceFilter(final Geometry geometry, final double maxDistance) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  /**
   * Get the geometry to compare the data objects to to.
   *
   * @return The geometry to compare the data objects to to.
   */
  public Geometry getGeometry() {
    return this.geometry;
  }

  /**
   * Get the maximum maxDistance the object can be from the source geometry.
   *
   * @return The maximum maxDistance the object can be from the source geometry.
   */
  public double getMaxDistance() {
    return this.maxDistance;
  }

  @Override
  public boolean test(final Record object) {
    final Geometry matchGeometry = object.getGeometry();
    final double distance = matchGeometry.distance(this.geometry);
    if (distance <= this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }

}
