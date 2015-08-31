package com.revolsys.geometry.operation.simple;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.operation.valid.GeometryError;

public class SelfOverlapLineSegmentError extends GeometryError {
  public SelfOverlapLineSegmentError(final Geometry geometry, final LineSegment segment) {
    super("Self Overlap at Line", geometry, segment);
  }
}
