package com.revolsys.gis.model.geometry.operation.relate;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.operation.geomgraph.GeometryGraph;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.vividsolutions.jts.algorithm.BoundaryNodeRule;

/**
 * The base class for operations that require {@link GeometryGraph}s.
 *
 * @version 1.7
 */
public class GeometryGraphOperation {
  protected final LineIntersector li = new RobustLineIntersector();

  //
  // protected PrecisionModel resultPrecisionModel;

  /**
   * The operation args into an array so they can be accessed by index
   */
  protected GeometryGraph[] arg; // the arg(s) of the operation

  public GeometryGraphOperation(final Geometry g0) {
    // setComputationPrecision(g0.getPrecisionModel());

    this.arg = new GeometryGraph[1];
    this.arg[0] = new GeometryGraph(0, g0);
  }

  public GeometryGraphOperation(final Geometry g0, final Geometry g1) {
    this(g0, g1, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE
      // BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE
        );
  }

  public GeometryGraphOperation(final Geometry g0, final Geometry g1,
    final BoundaryNodeRule boundaryNodeRule) {
    // use the most precise model for the result
    // if (g0.getPrecisionModel().compareTo(g1.getPrecisionModel()) >= 0)
    // setComputationPrecision(g0.getPrecisionModel());
    // else
    // setComputationPrecision(g1.getPrecisionModel());

    this.arg = new GeometryGraph[2];
    this.arg[0] = new GeometryGraph(0, g0, boundaryNodeRule);
    this.arg[1] = new GeometryGraph(1, g1, boundaryNodeRule);
  }

  public Geometry getArgGeometry(final int i) {
    return this.arg[i].getGeometry();
  }

  // protected void setComputationPrecision(PrecisionModel pm) {
  // resultPrecisionModel = pm;
  // li.setPrecisionModel(resultPrecisionModel);
  // }
}
