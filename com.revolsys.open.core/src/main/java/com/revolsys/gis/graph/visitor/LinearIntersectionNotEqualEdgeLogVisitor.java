package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.data.equals.Geometry3DExactEquals;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordLog;
import com.revolsys.data.record.filter.DataObjectGeometryFilter;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.filter.NotFilter;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.jts.filter.EqualFilter;
import com.revolsys.gis.jts.filter.LinearIntersectionFilter;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;
import com.vividsolutions.jts.geom.LineString;

public class LinearIntersectionNotEqualEdgeLogVisitor extends AbstractVisitor<Edge<Record>>
  implements ObjectProcessor<DataObjectGraph> {
  private static final String PROCESSED = LinearIntersectionNotEqualLineEdgeCleanupVisitor.class.getName()
    + ".PROCESSED";
  static {
    Geometry3DExactEquals.addExclude(PROCESSED);
  }

  @Override
  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean visit(final Edge<Record> edge) {
    final Record object = edge.getObject();
    final LineString line = edge.getLine();
    if (JtsGeometryUtil.getGeometryProperty(line, PROCESSED) != Boolean.TRUE) {
      final String typePath = edge.getTypeName();

      final Graph<Record> graph = edge.getGraph();

      final AndFilter<Edge<Record>> attributeAndGeometryFilter = new AndFilter<Edge<Record>>();

      attributeAndGeometryFilter.addFilter(new EdgeTypeNameFilter<Record>(typePath));

      final Filter<Edge<Record>> filter = getFilter();
      if (filter != null) {
        attributeAndGeometryFilter.addFilter(filter);
      }

      final Filter<Record> notEqualLineFilter = new NotFilter<Record>(
        new DataObjectGeometryFilter<LineString>(new EqualFilter<LineString>(line)));

      final DataObjectGeometryFilter<LineString> linearIntersectionFilter = new DataObjectGeometryFilter<LineString>(
        new LinearIntersectionFilter(line));

      attributeAndGeometryFilter.addFilter(new EdgeObjectFilter<Record>(new AndFilter<Record>(
        notEqualLineFilter, linearIntersectionFilter)));

      final List<Edge<Record>> intersectingEdges = graph.getEdges(attributeAndGeometryFilter, line);

      if (!intersectingEdges.isEmpty()) {
        RecordLog.error(getClass(), "Overlapping edge", object);
        JtsGeometryUtil.setGeometryProperty(line, PROCESSED, Boolean.TRUE);
        for (final Edge<Record> intersectingEdge : intersectingEdges) {
          final Record intersectingObject = intersectingEdge.getObject();
          final LineString intersectingLine = intersectingObject.getGeometryValue();
          if (JtsGeometryUtil.getGeometryProperty(intersectingLine, PROCESSED) != Boolean.TRUE) {
            JtsGeometryUtil.setGeometryProperty(intersectingLine, PROCESSED, Boolean.TRUE);
          }
        }
      }
    }
    return true;
  }
}
