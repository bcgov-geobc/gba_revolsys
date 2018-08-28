package com.revolsys.geometry.model.util;

import java.util.Arrays;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXYGeometryFactory;
import com.revolsys.util.function.BiConsumerDouble;

public class BoundingBoxEditor extends BoundingBoxDoubleXY implements BiConsumerDouble {

  private GeometryFactory geometryFactory = null;

  public BoundingBoxEditor() {
  }

  public BoundingBoxEditor(final BoundingBox boundingBox) {
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.minX = boundingBox.getMinX();
    this.minY = boundingBox.getMinY();
    this.maxX = boundingBox.getMaxX();
    this.maxY = boundingBox.getMaxY();
  }

  public BoundingBoxEditor(final BoundingBoxProxy boundingBox) {
    if (boundingBox != null) {
      this.geometryFactory = boundingBox.getGeometryFactory();
      final BoundingBox boundingBox1 = boundingBox.getBoundingBox();
      this.minX = boundingBox1.getMinX();
      this.minY = boundingBox1.getMinY();
      this.maxX = boundingBox1.getMaxX();
      this.maxY = boundingBox1.getMaxY();
    }
  }

  public BoundingBoxEditor(final GeometryFactoryProxy geometryFactory) {
    this.geometryFactory = geometryFactory.getGeometryFactory();
  }

  @Override
  public void accept(final double x, final double y) {
    if (x < this.minX) {
      this.minX = x;
    }
    if (x > this.maxX) {
      this.maxX = x;
    }
    if (y < this.minY) {
      this.minY = y;
    }
    if (y > this.maxY) {
      this.maxY = y;
    }
  }

  public BoundingBoxEditor addAllBbox(final BoundingBoxProxy... boundingBoxes) {
    for (final BoundingBoxProxy boundingBox : boundingBoxes) {
      addBbox(boundingBox);
    }
    return this;
  }

  public BoundingBoxEditor addAllBbox(final Iterable<? extends BoundingBoxProxy> boundingBoxes) {
    if (boundingBoxes != null) {
      for (final BoundingBoxProxy boundingBox : boundingBoxes) {
        addBbox(boundingBox);
      }
    }
    return this;
  }

  public BoundingBoxEditor addBbox(final BoundingBoxProxy boundingBoxProxy) {
    if (boundingBoxProxy != null) {
      BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      if (boundingBox != null && !boundingBox.isEmpty()) {
        if (!isHasHorizontalCoordinateSystem() && boundingBox.isHasHorizontalCoordinateSystem()) {
          this.geometryFactory = boundingBox.getGeometryFactory();
        }
        if (isProjectionRequired(boundingBox)) {
          // TODO just convert points
          boundingBox = boundingBox.bboxToCs(getGeometryFactory());
        }
        if (!boundingBox.isEmpty()) {
          final double minX = boundingBox.getMinX();
          final double minY = boundingBox.getMinY();
          final double maxX = boundingBox.getMaxX();
          final double maxY = boundingBox.getMaxY();
          addBbox(minX, minY, maxX, maxY);
        }
      }
    }
    return this;
  }

  public BoundingBoxEditor addBbox(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (minX < this.minX) {
      this.minX = minX;
    }
    if (minY < this.minY) {
      this.minY = minY;
    }
    if (maxX > this.maxX) {
      this.maxX = maxX;
    }
    if (maxY > this.maxY) {
      this.maxY = maxY;
    }
    return this;
  }

  public BoundingBoxEditor addGeometry(final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      if (this.geometryFactory == null && geometry.isHasHorizontalCoordinateSystem()) {
        this.geometryFactory = geometry.getGeometryFactory();
      }
      if (isProjectionRequired(geometry)) {
        final CoordinatesOperation coordinatesOperation = getCoordinatesOperation(geometry);
        if (coordinatesOperation == null) {

          addBbox(geometry);
        } else {
          coordinatesOperation.perform2d(geometry, this::addPoint);
        }
      } else {
        addBbox(geometry);
      }
    }
    return this;
  }

  public BoundingBoxEditor addPoint(final double x, final double y) {
    if (x < this.minX) {
      this.minX = x;
    }
    if (x > this.maxX) {
      this.maxX = x;
    }
    if (y < this.minY) {
      this.minY = y;
    }
    if (y > this.maxY) {
      this.maxY = y;
    }
    return this;
  }

  /**
   * If the coordinate system is a projected coordinate system then clip to the {@link CoordinateSystem#getAreaBoundingBox()}.
   */
  public BoundingBoxEditor clipToCoordinateSystem() {
    final BoundingBox areaBoundingBox = getAreaBoundingBox();
    if (areaBoundingBox != null && !areaBoundingBox.isEmpty()) {
      final double minX = areaBoundingBox.getMinX();
      if (this.minX < minX) {
        this.minX = minX;
      }
      final double maxX = areaBoundingBox.getMaxX();
      if (this.maxX < maxX) {
        this.maxX = maxX;
      }
      final double minY = areaBoundingBox.getMinY();
      if (this.minY < minY) {
        this.minY = minY;
      }
      final double maxY = areaBoundingBox.getMaxY();
      if (this.maxY < maxY) {
        this.maxY = maxY;
      }
    }
    return this;
  }

  /**
   * Return a new bounding box expanded by delta.
   *
   * @param delta
   * @return
   */
  public BoundingBoxEditor expandDelta(final double delta) {
    expandDelta(delta, delta);
    return this;
  }

  /**
   * Return a new bounding box expanded by deltaX, deltaY.
   *
   * @param delta
   * @return
   */
  public BoundingBoxEditor expandDelta(final double deltaX, final double deltaY) {
    if (isEmpty() || deltaX == 0 && deltaY == 0) {
    } else {
      this.minX -= deltaX;
      this.minY -= deltaY;
      this.maxX += deltaX;
      this.maxY += deltaY;
    }
    return this;
  }

  public BoundingBoxEditor expandDeltaX(final double deltaX) {
    if (isEmpty() || deltaX == 0) {
    } else {
      this.minX -= deltaX;
      this.maxX += deltaX;
    }
    return this;
  }

  public BoundingBoxEditor expandDeltaY(final double deltaY) {
    if (isEmpty() || deltaY == 0) {
    } else {
      this.minY -= deltaY;
      this.maxY += deltaY;
    }
    return this;
  }

  public BoundingBoxEditor expandPercent(final double factor) {
    return expandPercent(factor, factor);
  }

  public BoundingBoxEditor expandPercent(final double factorX, final double factorY) {
    if (!isEmpty()) {
      final double width = getWidth();
      final double deltaX = width * factorX / 2;
      final double height = getHeight();
      final double deltaY = height * factorY / 2;
      expandDelta(deltaX, deltaY);
    }
    return this;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.geometryFactory == null) {
      return GeometryFactory.DEFAULT_2D;
    } else {
      return this.geometryFactory;
    }
  }

  /**
   * <p>CMoving the min/max x coordinates by xDisplacement and
   * the min/max y coordinates by yDisplacement.</p>
   *
   * @param xDisplacement The distance to move the min/max x coordinates.
   * @param yDisplacement The distance to move the min/max y coordinates.
   */
  public BoundingBoxEditor move(final double xDisplacement, final double yDisplacement) {
    if (isEmpty() || xDisplacement == 0 && yDisplacement == 0) {
    } else {
      this.minX += xDisplacement;
      this.maxX += xDisplacement;
      this.minY += yDisplacement;
      this.maxY += yDisplacement;
    }
    return this;
  }

  public BoundingBox newBoundingBox() {
    if (isEmpty()) {
      if (this.geometryFactory == null) {
        return BoundingBox.empty();
      } else {
        return this.geometryFactory.newBoundingBoxEmpty();
      }
    } else {
      if (this.geometryFactory == null) {
        return new BoundingBoxDoubleXY(this.minX, this.minY, this.maxX, this.maxY);
      } else {
        return new BoundingBoxDoubleXYGeometryFactory(this.geometryFactory, this.minX, this.minY,
          this.maxX, this.maxY);
      }
    }
  }

  public BoundingBoxEditor setGeometryFactory(final GeometryFactoryProxy geometryFactory) {
    if (geometryFactory != null) {
      if (isProjectionRequired(geometryFactory)) {
        final CoordinatesOperation coordinatesOperation = getCoordinatesOperation(geometryFactory);
        if (coordinatesOperation != null) {
          final double minX = this.minX;
          final double minY = this.minY;
          final double maxX = this.maxX;
          final double maxY = this.maxY;
          clear();
          final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
          this.geometryFactory = geometryFactory.getGeometryFactory();
          for (final double y : Arrays.asList(minY, maxY)) {
            for (final double x : Arrays.asList(minX, maxX)) {
              coordinatesOperation.perform2d(point, x, y, this::addPoint);
            }
          }

          double xStep = getWidth() / 10;
          double yStep = getHeight() / 10;
          final double scaleX = this.geometryFactory.getScaleX();
          if (scaleX > 0) {
            if (xStep < 1 / scaleX) {
              xStep = 1 / scaleX;
            }
          }
          final double scaleY = this.geometryFactory.getScaleY();
          if (scaleY > 0) {
            if (yStep < 1 / scaleY) {
              yStep = 1 / scaleY;
            }
          }
          coordinatesOperation.perform2d(point, minX, minY, this::addPoint);
          coordinatesOperation.perform2d(point, minX, maxY, this::addPoint);
          coordinatesOperation.perform2d(point, maxX, minY, this::addPoint);
          coordinatesOperation.perform2d(point, maxX, maxY, this::addPoint);

          if (xStep != 0) {
            for (double x = minX + xStep; x < maxX; x += xStep) {
              coordinatesOperation.perform2d(point, x, minY, this::addPoint);
              coordinatesOperation.perform2d(point, x, maxY, this::addPoint);
            }
          }
          if (yStep != 0) {
            for (double y = minY + yStep; y < maxY; y += yStep) {
              coordinatesOperation.perform2d(point, minX, y, this::addPoint);
              coordinatesOperation.perform2d(point, maxX, y, this::addPoint);
            }
          }
        }
      }
      this.geometryFactory = geometryFactory.getGeometryFactory();
    }
    return this;
  }
}
