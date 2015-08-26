package com.revolsys.gis.graph.visitor;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeVisitor;
import com.revolsys.jts.geom.BoundingBox;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class NearParallelEdgeVisitor<T> extends EdgeVisitor<T> {

  private final LineString line;

  private final double maxDistance;

  public NearParallelEdgeVisitor(final LineString line, final double maxDistance) {
    this.line = line;
    this.maxDistance = maxDistance;
  }

  @Override
  public Envelope getEnvelope() {
    BoundingBox envelope = BoundingBox.getBoundingBox(this.line);
    envelope = envelope.expand(this.maxDistance);
    return envelope;
  }

  private boolean isAlmostParallel(final LineString matchLine) {
    if (this.line.getEnvelopeInternal()
      .distance(matchLine.getEnvelopeInternal()) > this.maxDistance) {
      return false;
    }
    final CoordinateSequence coords = this.line.getCoordinateSequence();
    final CoordinateSequence matchCoords = this.line.getCoordinateSequence();
    Coordinate previousCoordinate = coords.getCoordinate(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinate coordinate = coords.getCoordinate(i);
      Coordinate previousMatchCoordinate = matchCoords.getCoordinate(0);
      for (int j = 1; j < coords.size(); j++) {
        final Coordinate matchCoordinate = matchCoords.getCoordinate(i);
        final double distance = CGAlgorithms.distanceLineLine(previousCoordinate, coordinate,
          previousMatchCoordinate, matchCoordinate);
        if (distance <= this.maxDistance) {
          final double angle1 = Angle
            .normalizePositive(Angle.angle(previousCoordinate, coordinate));
          final double angle2 = Angle
            .normalizePositive(Angle.angle(previousMatchCoordinate, matchCoordinate));
          final double angleDiff = Math.abs(angle1 - angle2);
          if (angleDiff <= Math.PI / 6) {
            return true;
          }
        }
        previousMatchCoordinate = matchCoordinate;
      }
      previousCoordinate = coordinate;
    }
    return false;
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final LineString matchLine = edge.getLine();
    if (isAlmostParallel(matchLine)) {
      return super.visit(edge);
    } else {
      return true;
    }
  }
}
