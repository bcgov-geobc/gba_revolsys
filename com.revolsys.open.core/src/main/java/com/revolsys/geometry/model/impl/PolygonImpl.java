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
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;

/**
 * Represents a polygon with linear edges, which may include holes.
 * The outer boundary (shell)
 * and inner boundaries (holes) of the polygon are represented by {@link LinearRing}s.
 * The boundary rings of the polygon may have any orientation.
 * Polygons are closed, simple geometries by definition.
 * <p>
 * The polygon model conforms to the assertions specified in the
 * <A HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
 * Specification for SQL</A>.
 * <p>
 * A <code>Polygon</code> is topologically valid if and only if:
 * <ul>
 * <li>the coordinates which define it are valid coordinates
 * <li>the linear rings for the shell and holes are valid
 * (i.e. are closed and do not self-intersect)
 * <li>holes touch the shell or another hole at at most one point
 * (which implies that the rings of the shell and holes must not cross)
 * <li>the interior of the polygon is connected,
 * or equivalently no sequence of touching holes
 * makes the interior of the polygon disconnected
 * (i.e. effectively split the polygon into two pieces).
 * </ul>
 *
 *@version 1.7
 */
public class PolygonImpl extends AbstractPolygon implements Polygon {
  private static final long serialVersionUID = 1L;

  private BoundingBox boundingBox;

  /**
   * The {@link GeometryFactory} used to create this Geometry
   */
  private final GeometryFactory geometryFactory;

  private LinearRing[] rings;

  private Object userData;

  public PolygonImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   *  Constructs a <code>Polygon</code> with the given exterior boundary and
   *  interior boundaries.
   *
   *@param  shell           the outer boundary of the new <code>Polygon</code>,
   *      or <code>null</code> or an empty <code>LinearRing</code> if the empty
   *      geometry is to be created.
   *@param  holes           the inner boundaries of the new <code>Polygon</code>
   *      , or <code>null</code> or empty <code>LinearRing</code>s if the empty
   *      geometry is to be created.
   */
  public PolygonImpl(final GeometryFactory factory, final LinearRing... rings) {
    this.geometryFactory = factory;
    if (rings == null || rings.length == 0) {

    } else if (hasNullElements(rings)) {
      throw new IllegalArgumentException("rings must not contain null elements");
    } else {
      if (rings[0].isEmpty()) {
        for (int i = 1; i < rings.length; i++) {
          final LinearRing ring = rings[i];
          if (!ring.isEmpty()) {
            throw new IllegalArgumentException("shell is empty but hole " + (i - 1) + " is not");
          }
        }
      } else {
        this.rings = rings;
      }
    }
  }

  /**
   * Creates and returns a full copy of this {@link Polygon} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public PolygonImpl clone() {
    final PolygonImpl poly = (PolygonImpl)super.clone();
    if (this.rings != null) {
      poly.rings = this.rings.clone();
      for (int i = 0; i < this.rings.length; i++) {
        poly.rings[i] = this.rings[i].clone();
      }
    }
    return poly;
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

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public LinearRing getRing(final int ringIndex) {
    if (isEmpty() || ringIndex < 0 || ringIndex >= this.rings.length) {
      return null;
    } else {
      return this.rings[ringIndex];
    }
  }

  @Override
  public int getRingCount() {
    if (isEmpty()) {
      return 0;
    } else {
      return this.rings.length;
    }
  }

  @Override
  public List<LinearRing> getRings() {
    return new ArrayList<>(Arrays.asList(this.rings));
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
    return this.rings == null;
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
