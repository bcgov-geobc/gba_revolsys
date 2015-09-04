/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.triangulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.CoordinateArrays;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.triangulate.quadedge.QuadEdgeSubdivision;
import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * A utility class which creates Voronoi Diagrams
 * from collections of points.
 * The diagram is returned as a {@link GeometryCollection} of {@link Polygon}s,
 * clipped to the larger of a supplied envelope or to an envelope determined
 * by the input sites.
 *
 * @author Martin Davis
 *
 */
public class VoronoiDiagramBuilder {
  private static Geometry clipGeometryCollection(final Geometry geom, final BoundingBox clipEnv) {
    final GeometryFactory r = geom.getGeometryFactory();
    final Geometry clipPoly = clipEnv.toGeometry();
    final List<Geometry> clipped = new ArrayList<Geometry>();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry g = geom.getGeometry(i);
      Geometry result = null;
      // don't clip unless necessary
      if (clipEnv.covers(g.getBoundingBox())) {
        result = g;
      } else if (clipEnv.intersects(g.getBoundingBox())) {
        result = clipPoly.intersection(g);
        // keep vertex key info
        result.setUserData(g.getUserData());
      }

      if (result != null && !result.isEmpty()) {
        clipped.add(result);
      }
    }
    return geom.getGeometryFactory().geometryCollection(clipped);
  }

  private BoundingBox clipEnv = null;

  private BoundingBox diagramEnv = null;

  private Collection siteCoords;

  private QuadEdgeSubdivision subdiv = null;

  private double tolerance = 0.0;

  /**
   * Creates a new Voronoi diagram builder.
   *
   */
  public VoronoiDiagramBuilder() {
  }

  private void create() {
    if (this.subdiv != null) {
      return;
    }

    final BoundingBoxDoubleGf siteEnv = DelaunayTriangulationBuilder.envelope(this.siteCoords);
    this.diagramEnv = siteEnv;
    // add a buffer around the final envelope
    final double expandBy = Math.max(this.diagramEnv.getWidth(), this.diagramEnv.getHeight());
    this.diagramEnv = this.diagramEnv.expand(expandBy);
    if (this.clipEnv != null) {
      this.diagramEnv.expandToInclude(this.clipEnv);
    }

    final List vertices = DelaunayTriangulationBuilder.toVertices(this.siteCoords);
    this.subdiv = new QuadEdgeSubdivision(siteEnv, this.tolerance);
    final IncrementalDelaunayTriangulator triangulator = new IncrementalDelaunayTriangulator(
      this.subdiv);
    triangulator.insertSites(vertices);
  }

  /**
   * Gets the faces of the computed diagram as a {@link GeometryCollection}
   * of {@link Polygon}s, clipped as specified.
   *
   * @param geomFact the geometry factory to use to create the output
   * @return the faces of the diagram
   */
  public Geometry getDiagram(final GeometryFactory geomFact) {
    create();
    final Geometry polys = this.subdiv.getVoronoiDiagram(geomFact);

    // clip polys to diagramEnv
    return clipGeometryCollection(polys, this.diagramEnv);
  }

  /**
   * Gets the {@link QuadEdgeSubdivision} which models the computed diagram.
   *
   * @return the subdivision containing the triangulation
   */
  public QuadEdgeSubdivision getSubdivision() {
    create();
    return this.subdiv;
  }

  /**
   * Sets the envelope to clip the diagram to.
   * The diagram will be clipped to the larger
   * of this envelope or an envelope surrounding the sites.
   *
   * @param clipEnv the clip envelope.
   */
  public void setClipEnvelope(final BoundingBox clipEnv) {
    this.clipEnv = clipEnv;
  }

  /**
   * Sets the sites (point or vertices) which will be diagrammed
   * from a collection of {@link Coordinates}s.
   *
   * @param coords a collection of Coordinates.
   */
  public void setSites(final Collection coords) {
    // remove any duplicate points (they will cause the triangulation to fail)
    this.siteCoords = DelaunayTriangulationBuilder
      .unique(CoordinateArrays.toCoordinateArray(coords));
  }

  /**
   * Sets the sites (point or vertices) which will be diagrammed.
   * All vertices of the given geometry will be used as sites.
   *
   * @param geom the geometry from which the sites will be extracted.
   */
  public void setSites(final Geometry geom) {
    // remove any duplicate points (they will cause the triangulation to fail)
    this.siteCoords = DelaunayTriangulationBuilder.extractUniqueCoordinates(geom);
  }

  /**
   * Sets the snapping tolerance which will be used
   * to improved the robustness of the triangulation computation.
   * A tolerance of 0.0 specifies that no snapping will take place.
   *
   * @param tolerance the tolerance distance to use
   */
  public void setTolerance(final double tolerance) {
    this.tolerance = tolerance;
  }
}
