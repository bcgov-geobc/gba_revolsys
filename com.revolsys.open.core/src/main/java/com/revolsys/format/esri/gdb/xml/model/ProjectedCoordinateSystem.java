package com.revolsys.format.esri.gdb.xml.model;

import com.revolsys.gis.cs.GeometryFactory;

public class ProjectedCoordinateSystem extends SpatialReference {
  public ProjectedCoordinateSystem() {
  }

  public ProjectedCoordinateSystem(final GeometryFactory geometryFactory,
    final String wkt) {
    super(geometryFactory, wkt);
  }

}
