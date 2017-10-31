package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.Triangle;
import com.revolsys.util.function.BiConsumerDouble;
import com.revolsys.util.function.BiFunctionDouble;
import com.revolsys.util.function.DoubleConsumer3;

public abstract class AbstractTriangle extends AbstractPolygon implements Triangle {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  public Triangle clone() {
    return (Triangle)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Triangle) {
      final Triangle triangle = (Triangle)other;
      return equals(triangle);
    } else {
      return super.equals(other);
    }
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    if (!isEmpty()) {
      for (int i = 0; i < 3; i++) {
        final double x = getX(i);
        final double y = getY(i);
        final R result = action.accept(x, y);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      for (int i = 0; i < 3; i++) {
        final double x = getX(i);
        final double y = getY(i);
        action.accept(x, y);
      }
    }
  }

  @Override
  public void forEachVertex(final DoubleConsumer3 action) {
    if (!isEmpty()) {
      for (int i = 0; i < 3; i++) {
        final double x = getX(i);
        final double y = getY(i);
        final double z = getZ(i);
        action.accept(x, y, z);
      }
    }
  }
}
