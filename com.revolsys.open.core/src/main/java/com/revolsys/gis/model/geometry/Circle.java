/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package com.revolsys.gis.model.geometry;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class Circle extends DoubleCoordinates {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private BoundingBox envelope;

  private final double radius;

  private final double tolerance = 0.0001;

  public Circle(final Coordinates centre, final double radius) {
    super(centre);
    this.radius = radius;
    this.envelope = new BoundingBox(getX(), getY());
    this.envelope = this.envelope.expand(radius);
  }

  public boolean contains(final Coordinates point) {
    final double distanceFromCentre = distance(point);
    return distanceFromCentre < this.radius + this.tolerance;
  }

  public Envelope getEnvelopeInternal() {
    return this.envelope;
  }

  public double getRadius() {
    return this.radius;
  }

  public Geometry toGeometry() {
    final GeometryFactory factory = GeometryFactory.getFactory();
    final Point point = factory.createPoint(this);
    return point.buffer(this.radius);
  }

  @Override
  public String toString() {
    return "CIRCLE(" + getX() + " " + getY() + " " + this.radius + ")";
  }
}
