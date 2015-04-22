package com.revolsys.swing.map.overlay;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.geometry.util.GeometryEditUtil;
import com.revolsys.gis.model.geometry.util.IndexedLineSegment;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class CloseLocation {

  private final LayerDataObject object;

  private final int[] vertexIndex;

  private final IndexedLineSegment segment;

  private final AbstractDataObjectLayer layer;

  private final Geometry geometry;

  private final Point point;

  public CloseLocation(final AbstractDataObjectLayer layer,
    final LayerDataObject object, final Geometry geometry,
    final int[] vertexIndex, final IndexedLineSegment segment, final Point point) {
    this.object = object;
    this.layer = layer;
    this.geometry = geometry;
    this.vertexIndex = vertexIndex;
    this.segment = segment;
    this.point = point;
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G getGeometry() {
    return (G)this.geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return this.layer.getGeometryFactory();
  }

  public Object getId() {
    Object id = null;
    if (this.object != null) {
      id = this.object.getIdValue();
    }
    if (id == null) {
      id = "NEW";
    }
    return id;
  }

  public String getIdAttributeName() {
    return getMetaData().getIdFieldName();
  }

  public String getIndexString() {
    int[] index = this.vertexIndex;
    if (index != null) {
    } else {
      index = this.segment.getIndex();
    }
    return CollectionUtil.toString(CollectionUtil.arrayToList(index));
  }

  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  public RecordDefinition getMetaData() {
    return this.layer.getMetaData();
  }

  public LayerDataObject getObject() {
    return this.object;
  }

  public Point getPoint() {
    return this.point;
  }

  public IndexedLineSegment getSegment() {
    return this.segment;
  }

  public int[] getSegmentIndex() {
    return this.segment.getIndex();
  }

  public String getType() {
    if (geometry instanceof Point) {
      return "Point";
    } else if (segment != null) {
      return "Edge";
    } else {
      if (GeometryEditUtil.isFromPoint(geometry, vertexIndex)
        || GeometryEditUtil.isToPoint(geometry, vertexIndex)) {
        return "End-Vertex";
      } else {
        return "Vertex";
      }
    }
  }

  public String getTypePath() {
    final RecordDefinition metaData = getMetaData();
    return metaData.getPath();
  }

  public int[] getVertexIndex() {
    return this.vertexIndex;
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();
    string.append(getTypePath());
    string.append(", ");
    final RecordDefinition metaData = getMetaData();
    string.append(metaData.getIdFieldName());
    string.append("=");
    final Object id = getId();
    string.append(id);
    string.append(", ");
    string.append(getType());
    int[] index = this.vertexIndex;
    if (index != null) {
      string.append(", index=");
    } else {
      string.append(", index=");
      index = this.segment.getIndex();
    }
    final String indexString = CollectionUtil.toString(CollectionUtil.arrayToList(index));
    string.append(indexString);
    return string.toString();
  }

}
