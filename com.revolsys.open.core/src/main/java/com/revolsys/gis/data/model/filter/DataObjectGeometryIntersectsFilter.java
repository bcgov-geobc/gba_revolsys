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
package com.revolsys.gis.data.model.filter;

import java.util.Collection;
import java.util.List;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectGeometryIntersectsFilter implements Filter<Record> {
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public static <D extends Record> List<D> filter(
    final Collection<D> collection, final BoundingBox boundingBox) {
    final Filter filter = new DataObjectGeometryIntersectsFilter(boundingBox);
    return FilterUtil.filter(collection, filter);
  }

  /** The geometry to compare the data objects to to. */
  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  public DataObjectGeometryIntersectsFilter(final BoundingBox boundingBox) {
    this(boundingBox.toPolygon());
  }

  /**
   * Construct a new DataObjectGeometryIntersectsFilter.
   * 
   * @param geometry The geometry to compare the data objects to to.
   */
  public DataObjectGeometryIntersectsFilter(final Geometry geometry) {
    this.geometry = geometry;
    this.geometryFactory = GeometryFactory.getFactory(geometry);
  }

  @Override
  public boolean accept(final Record object) {
    try {
      final Geometry matchGeometry = object.getGeometryValue();
      final Geometry convertedGeometry = geometryFactory.copy(matchGeometry);
      if (convertedGeometry != null && geometry != null
        && convertedGeometry.intersects(geometry)) {
        return true;
      } else {
        return false;
      }
    } catch (final Throwable t) {
      t.printStackTrace();
      return false;
    }
  }

  /**
   * Get the geometry to compare the data objects to to.
   * 
   * @return The geometry to compare the data objects to to.
   */
  public Geometry getGeometry() {
    return geometry;
  }
}
