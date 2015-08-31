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
package com.revolsys.geometry.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.prep.PreparedGeometryCollection;

/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 *
 *
 *@version 1.7
 */
public class GeometryCollectionImpl extends AbstractGeometryCollection {
  // With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
  private static final long serialVersionUID = -5694727726395021467L;

  /**
   *  The bounding box of this <code>Geometry</code>.
   */
  private BoundingBox boundingBox;

  /**
   *  Internal representation of this <code>GeometryCollection</code>.
   */
  private Geometry[] geometries;

  /**
   * The {@link GeometryFactory} used to create this Geometry
   */
  private final GeometryFactory geometryFactory;

  /**
   * An object reference which can be used to carry ancillary data defined
   * by the client.
   */
  private Object userData;

  public GeometryCollectionImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * @param geometries
   *            the <code>Geometry</code>s for this <code>GeometryCollection</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>Geometry</code>s,
   *            but not <code>null</code>s.
   */
  public GeometryCollectionImpl(final GeometryFactory geometryFactory,
    final Geometry[] geometries) {
    this.geometryFactory = geometryFactory;
    if (geometries == null || geometries.length == 0) {
      this.geometries = null;
    } else if (hasNullElements(geometries)) {
      throw new IllegalArgumentException("geometries must not contain null elements");
    } else {
      this.geometries = geometries;
    }
  }

  /**
   * Creates and returns a full copy of this {@link GeometryCollection} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public GeometryCollectionImpl clone() {
    final GeometryCollectionImpl gc = (GeometryCollectionImpl)super.clone();
    gc.geometries = new Geometry[this.geometries.length];
    for (int i = 0; i < this.geometries.length; i++) {
      gc.geometries[i] = this.geometries[i].clone();
    }
    return gc;
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      if (isEmpty()) {
        this.boundingBox = new BoundingBoxDoubleGf(getGeometryFactory());
      } else {
        this.boundingBox = computeBoundingBox();
      }
    }
    return this.boundingBox;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    if (this.geometries == null) {
      return new ArrayList<V>();
    } else {
      return (List<V>)new ArrayList<>(Arrays.asList(this.geometries));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int n) {
    if (this.geometries == null) {
      return null;
    } else {
      return (V)this.geometries[n];
    }
  }

  @Override
  public int getGeometryCount() {
    if (this.geometries == null) {
      return 0;
    } else {
      return this.geometries.length;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   * Gets the user data object for this geometry, if any.
   *
   * @return the user data object, or <code>null</code> if none set
   */
  @Override
  public Object getUserData() {
    return this.userData;
  }

  @Override
  public boolean isEmpty() {
    return this.geometries == null;
  }

  @Override
  public GeometryCollection prepare() {
    return new PreparedGeometryCollection(this);
  }

  /**
   * A simple scheme for applications to add their own custom data to a Geometry.
   * An example use might be to add an object representing a Point Reference System.
   * <p>
   * Note that user data objects are not present in geometries created by
   * construction methods.
   *
   * @param userData an object, the semantics for which are defined by the
   * application using this Geometry
   */
  @Override
  public void setUserData(final Object userData) {
    this.userData = userData;
  }

}
