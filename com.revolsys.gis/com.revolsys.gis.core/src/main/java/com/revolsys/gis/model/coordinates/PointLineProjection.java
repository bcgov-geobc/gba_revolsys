package com.revolsys.gis.model.coordinates;

public class PointLineProjection {
  private final Coordinates lineEnd;

  private final Coordinates lineStart;

  private final Coordinates point;

  private final CoordinatesPrecisionModel precisionModel;

  private double projectionFactor = Double.NaN;

  private Coordinates projectedPoint;

  private Boolean pointOnLine;

  public PointLineProjection(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    this.precisionModel = precisionModel;
    this.lineStart = lineStart;
    this.lineEnd = lineEnd;
    this.point = point;
  }

  public double getProjectionFactor() {
    if (Double.isNaN(projectionFactor)) {
      projectionFactor = LineSegmentUtil.projectionFactor(lineStart, lineEnd,
        point);
    }
    return projectionFactor;
  }

  public Coordinates getProjectedPoint() {
    if (projectedPoint == null) {
      double projectionFactor = getProjectionFactor();
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        projectedPoint = LineSegmentUtil.project(lineStart, lineEnd,
          projectionFactor);
        precisionModel.makePrecise(projectedPoint);
      }
    }
    return projectedPoint;
  }

  /**
   * Check to see if the point is on either exactly or approximately on the
   * line. The approximate case uses the precision model to calculate the
   * projection of the point onto the line. If the projection factor >= 0 & <=1
   * and the projected point made precise according to the precision model has
   * the same 2D coordinates as the point then it is considered t be on the
   * line.
   * 
   * @return True if the point is on the line.
   */
  public boolean isPointOnLine() {
    if (pointOnLine == null) {
      pointOnLine = Boolean.FALSE;
      double projectionFactor = getProjectionFactor();
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        final Coordinates projectedPoint = getProjectedPoint();
        if (projectedPoint.equals2d(point)) {
          pointOnLine = Boolean.TRUE;
        }
      }
    }
    return pointOnLine;
  }
}
