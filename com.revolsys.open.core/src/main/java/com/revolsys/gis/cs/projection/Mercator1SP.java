package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.ProjectionParameterNames;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.algorithm.Angle;

public class Mercator1SP implements CoordinatesProjection {

  private final double a;

  private final double e;

  private final double eOver2;

  private final double lambda0; // central meridian

  private final double x0;

  private final double y0;

  public Mercator1SP(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final Datum datum = geographicCS.getDatum();
    final double centralMeridian = cs.getDoubleParameter(ProjectionParameterNames.LONGITUDE_OF_CENTER);

    final Spheroid spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_NORTHING);
    this.lambda0 = Math.toRadians(centralMeridian);
    this.a = spheroid.getSemiMajorAxis();
    this.e = spheroid.getEccentricity();
    this.eOver2 = e / 2;

  }

  @Override
  public void inverse(final Coordinates from, final Coordinates to) {
    final double x = (from.getX() - x0);
    final double y = (from.getY() - y0);

    final double lambda = x / a + lambda0;

    final double t = Math.pow(Math.E, -y / a);
    // TODO phi
    double phi = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {
      final double eSinPhi = e * Math.sin(phi);
      final double phi1 = Angle.PI_OVER_2 - 2
        * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), eOver2));
      delta = Math.abs(phi1 - phi);
      phi = phi1;
    } while (delta > 1.0e-011);

    to.setValue(0, lambda);
    to.setValue(1, phi);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  @Override
  public void project(final Coordinates from, final Coordinates to) {
    final double lambda = from.getX();
    final double phi = from.getY();

    final double x = a * (lambda - lambda0);

    final double eSinPhi = e * Math.sin(phi);
    final double y = a
      * Math.log(Math.tan(Angle.PI_OVER_4 + phi / 2)
        * Math.pow((1 - eSinPhi) / (1 + eSinPhi), eOver2));

    to.setValue(0, x0 + x);
    to.setValue(1, y0 + y);

    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

}
