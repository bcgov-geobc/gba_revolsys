package com.revolsys.format.esri.gdb.xml.model;

import com.revolsys.gis.cs.GeometryFactory;

public class GeographicCoordinateSystem extends SpatialReference {

  public GeographicCoordinateSystem() {
  }

  public GeographicCoordinateSystem(final GeometryFactory geometryFactory,
    final String wkt) {
    super(geometryFactory, wkt);
  }

}
