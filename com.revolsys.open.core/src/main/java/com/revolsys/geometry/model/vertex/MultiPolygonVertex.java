package com.revolsys.geometry.model.vertex;

import java.util.NoSuchElementException;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Polygon;

public class MultiPolygonVertex extends AbstractVertex {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private int partIndex;

  private int ringIndex;

  private int vertexIndex;

  public MultiPolygonVertex(final MultiPolygon multiPolygon, final int... vertexId) {
    super(multiPolygon);
    setVertexId(vertexId);
  }

  @Override
  public double getCoordinate(final int index) {
    final Polygon polygon = getPolygon();
    final LinearRing ring = polygon.getRing(this.ringIndex);
    if (ring == null) {
      return Double.NaN;
    } else {
      return ring.getCoordinate(this.vertexIndex, index);
    }
  }

  @Override
  public Vertex getLineNext() {
    final LineString ring = getRing();
    if (ring != null) {
      int newVertexIndex = this.vertexIndex + 1;
      if (newVertexIndex >= ring.getVertexCount() - 1) {
        newVertexIndex -= ring.getVertexCount();
      }
      if (newVertexIndex < ring.getVertexCount() - 1) {
        return new MultiPolygonVertex(getMultiPolygon(), this.partIndex, this.ringIndex,
          newVertexIndex);
      }
    }
    return null;
  }

  @Override
  public Vertex getLinePrevious() {
    final LineString ring = getRing();
    if (ring != null) {
      int newVertexIndex = this.vertexIndex - 1;
      if (newVertexIndex == -1) {
        newVertexIndex = ring.getVertexCount() - 2;
      }
      if (newVertexIndex >= 0) {
        return new MultiPolygonVertex(getMultiPolygon(), this.partIndex, this.ringIndex,
          newVertexIndex);
      }
    }
    return null;
  }

  public MultiPolygon getMultiPolygon() {
    return (MultiPolygon)getGeometry();
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  public Polygon getPolygon() {
    final MultiPolygon multiPolygon = getMultiPolygon();
    return multiPolygon.getPolygon(this.partIndex);
  }

  public LinearRing getRing() {
    final Polygon polygon = getPolygon();
    return polygon.getRing(this.ringIndex);
  }

  @Override
  public int getRingIndex() {
    return this.ringIndex;
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.partIndex, this.ringIndex, this.vertexIndex
    };
  }

  @Override
  public int getVertexIndex() {
    return this.vertexIndex;
  }

  @Override
  public boolean hasNext() {
    if (getGeometry().isEmpty()) {
      return false;
    } else {
      final MultiPolygon multiPolygon = getMultiPolygon();
      int partIndex = this.partIndex;
      int ringIndex = this.ringIndex;
      int vertexIndex = this.vertexIndex + 1;

      while (partIndex < multiPolygon.getGeometryCount()) {
        final Polygon polygon = multiPolygon.getPolygon(partIndex);

        while (ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          if (vertexIndex < ring.getVertexCount()) {
            return true;
          } else {
            ringIndex++;
            vertexIndex = 0;
          }
        }
        partIndex++;
        ringIndex = 0;
        vertexIndex = 0;
      }
      return false;
    }
  }

  @Override
  public boolean isFrom() {
    return getVertexIndex() == 0;
  }

  @Override
  public boolean isTo() {
    final int vertexIndex = getVertexIndex();
    final LineString ring = getRing();
    final int lastVertexIndex = ring.getVertexCount() - 1;
    return vertexIndex == lastVertexIndex;
  }

  @Override
  public Vertex next() {
    final MultiPolygon multiPolygon = getMultiPolygon();
    this.vertexIndex++;
    while (this.partIndex < multiPolygon.getGeometryCount()) {
      final Polygon polygon = multiPolygon.getPolygon(this.partIndex);
      while (this.ringIndex < polygon.getRingCount()) {
        final LinearRing ring = polygon.getRing(this.ringIndex);
        if (this.vertexIndex < ring.getVertexCount()) {
          return this;
        } else {
          this.ringIndex++;
          this.vertexIndex = 0;
        }
      }
      this.partIndex++;
      this.ringIndex = 0;
      this.vertexIndex = 0;
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  public void setVertexId(final int... vertexId) {
    this.partIndex = vertexId[0];
    this.ringIndex = vertexId[1];
    this.vertexIndex = vertexId[2];
  }
}
