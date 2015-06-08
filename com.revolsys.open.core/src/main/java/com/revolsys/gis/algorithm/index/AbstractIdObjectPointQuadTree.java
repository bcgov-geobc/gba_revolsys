package com.revolsys.gis.algorithm.index;

import java.util.Collection;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractIdObjectPointQuadTree<T> extends AbstractPointSpatialIndex<T>
  implements IdObjectIndex<T> {

  private final PointSpatialIndex<Integer> index = new PointQuadTree<Integer>();

  public void add(final Collection<Integer> ids) {
    for (final Integer id : ids) {
      final T object = getObject(id);
      add(object);
    }
  }

  @Override
  public T add(final T object) {
    final Coordinates point = getCoordinates(object);
    put(point, object);
    return object;
  }

  public abstract Coordinates getCoordinates(T object);

  @Override
  public void put(final Coordinates point, final T object) {
    final int id = getId(object);
    this.index.put(point, id);
  }

  @Override
  public boolean remove(final Coordinates point, final T object) {
    final int id = getId(object);
    return this.index.remove(point, id);
  }

  @Override
  public boolean remove(final T object) {
    final Coordinates point = getCoordinates(object);
    return remove(point, object);
  }

  public void removeAll(final Collection<T> objects) {
    for (final T object : objects) {
      remove(object);
    }
  }

  @Override
  public void visit(final Envelope envelope, final Visitor<T> visitor) {
    final IdObjectIndexEnvelopeVisitor<T> itemVisitor = new IdObjectIndexEnvelopeVisitor<T>(this,
      envelope, visitor);
    this.index.visit(envelope, itemVisitor);
  }

  @Override
  public void visit(final Visitor<T> visitor) {
    final IdObjectIndexVisitor<T> itemVisitor = new IdObjectIndexVisitor<T>(this, visitor);
    this.index.visit(itemVisitor);
  }

}
