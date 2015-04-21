package com.revolsys.gis.graph.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.record.Record;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.util.ObjectProcessor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class GraphProcessor extends BaseInOutProcess<Record, Record> {
  private static final Logger LOG = LoggerFactory.getLogger(GraphProcessor.class);

  private DataObjectGraph graph;

  private CoordinatesPrecisionModel precisionModel;

  private List<ObjectProcessor<DataObjectGraph>> processors = new ArrayList<ObjectProcessor<DataObjectGraph>>();

  public CoordinatesPrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  public List<ObjectProcessor<DataObjectGraph>> getProcessors() {
    return processors;
  }

  @Override
  protected void init() {
    super.init();
    graph = new DataObjectGraph();
    if (precisionModel != null) {
      graph.setPrecisionModel(precisionModel);
    }
  }

  @Override
  protected void postRun(final Channel<Record> in,
    final Channel<Record> out) {
    if (out != null) {
      processGraph();
      for (final Edge<Record> edge : graph.getEdges()) {
        final Record object = edge.getObject();
        out.write(object);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      graph.addEdge(object, line);
    } else {
      if (out != null) {
        out.write(object);
      }
    }
  }

  private void processGraph() {
    if (graph != null) {
      for (final ObjectProcessor<DataObjectGraph> processor : processors) {
        LOG.info(processor.getClass().getName());
        processor.process(graph);
      }
    }
  }

  public void setPrecisionModel(final CoordinatesPrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  public void setProcessors(
    final List<ObjectProcessor<DataObjectGraph>> processors) {
    this.processors = processors;
  }
}
