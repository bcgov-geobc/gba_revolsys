package com.revolsys.gis.algorithm.index;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Envelope;

public class RTreeLeaf<T> extends RTreeNode<T> {

  /**
   *
   */
  private static final long serialVersionUID = 5073275000676209987L;

  private Object[] objects;

  private Envelope[] envelopes;

  private int size = 0;

  public RTreeLeaf() {
  }

  public RTreeLeaf(final int size) {
    this.objects = new Object[size];
    this.envelopes = new Envelope[size];
  }

  public void add(final Envelope envelope, final T object) {
    this.envelopes[this.size] = envelope;
    this.objects[this.size] = object;
    this.size++;
    expandToInclude(envelope);
  }

  @SuppressWarnings("unchecked")
  public T getObject(final int index) {
    return (T)this.objects[index];
  }

  public int getSize() {
    return this.size;
  }

  @Override
  public boolean remove(final LinkedList<RTreeNode<T>> path, final Envelope envelope, final T object) {
    for (int i = 0; i < this.size; i++) {
      final Envelope envelope1 = this.envelopes[i];
      final T object1 = getObject(i);
      if (object1 == object) {
        if (envelope1.equals(envelope)) {
          System.arraycopy(this.envelopes, i + 1, this.envelopes, i, this.size - i - 1);
          this.envelopes[this.size - 1] = null;
          System.arraycopy(this.objects, i + 1, this.objects, i, this.size - i - 1);
          this.objects[this.size - 1] = null;
          this.size--;
          path.add(this);
          updateEnvelope();
          return true;
        } else {
          System.err.println();
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public List<RTreeNode<T>> split(final Envelope envelope, final T object) {
    final RTreeLeaf<T> leaf1 = new RTreeLeaf<T>(this.objects.length);
    final RTreeLeaf<T> leaf2 = new RTreeLeaf<T>(this.objects.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(this.size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      final Envelope envelope1 = this.envelopes[i];
      final T object1 = getObject(i);
      leaf1.add(envelope1, object1);
    }
    for (int i = midPoint + 1; i < this.size; i++) {
      final Envelope envelope1 = this.envelopes[i];
      final T object1 = getObject(i);
      leaf2.add(envelope1, object1);
    }
    leaf2.add(envelope, object);
    return Arrays.<RTreeNode<T>> asList(leaf1, leaf2);
  }

  @Override
  protected void updateEnvelope() {
    init();
    for (int i = 0; i < this.size; i++) {
      final Envelope envelope = this.envelopes[i];
      expandToInclude(envelope);
    }
  }

  @Override
  public boolean visit(final Envelope envelope, final Filter<T> filter, final Visitor<T> visitor) {
    for (int i = 0; i < this.size; i++) {
      final Envelope objectEnvelope = this.envelopes[i];
      if (envelope.intersects(objectEnvelope)) {
        final T object = getObject(i);
        if (filter.accept(object)) {
          if (!visitor.visit(object)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(final Envelope envelope, final Visitor<T> visitor) {
    for (int i = 0; i < this.size; i++) {
      final Envelope objectEnvelope = this.envelopes[i];
      if (envelope.intersects(objectEnvelope)) {
        final T object = getObject(i);
        if (!visitor.visit(object)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(final Visitor<T> visitor) {
    for (int i = 0; i < this.size; i++) {
      final T object = getObject(i);
      if (!visitor.visit(object)) {
        return false;
      }
    }
    return true;
  }
}
