package com.revolsys.gis.model.geometry.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLinearRing;

public class MultiLinearRingImpl extends MultiLineStringImpl implements MultiLinearRing {

  protected MultiLinearRingImpl(final GeometryFactoryImpl geometryFactory,
    final Collection<? extends Geometry> geometries) {
    super(geometryFactory, LinearRing.class, geometries);
  }

  @Override
  public MultiLinearRingImpl clone() {
    return (MultiLinearRingImpl)super.clone();
  }

  @Override
  public Iterator<LinearRing> iterator() {
    final List<LinearRing> geometries = getGeometries();
    return geometries.iterator();
  }
}
