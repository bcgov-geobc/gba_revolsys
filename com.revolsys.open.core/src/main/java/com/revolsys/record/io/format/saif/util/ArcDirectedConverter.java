package com.revolsys.record.io.format.saif.util;

import java.io.IOException;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.impl.LineStringDoubleBuilder;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.io.format.saif.geometry.ArcDirectedLineString;
import com.revolsys.record.io.format.saif.geometry.ArcLineString;

public class ArcDirectedConverter extends ArcConverter {

  public ArcDirectedConverter(final GeometryFactory geometryFactory) {
    super(geometryFactory, SaifConstants.ARC_DIRECTED);
  }

  @Override
  public LineString newLineString(final GeometryFactory geometryFactory,
    final LineStringDoubleBuilder line) {
    return new ArcDirectedLineString(geometryFactory, line);
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer, final ArcLineString line)
    throws IOException {
    if (line instanceof ArcDirectedLineString) {
      final ArcDirectedLineString dirLine = (ArcDirectedLineString)line;
      final String flowDirection = dirLine.getFlowDirection();
      attributeEnum(serializer, "flowDirection", flowDirection);

    }
    super.writeAttributes(serializer, line);
  }
}
