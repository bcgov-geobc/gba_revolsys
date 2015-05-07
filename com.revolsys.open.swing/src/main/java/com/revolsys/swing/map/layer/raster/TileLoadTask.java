package com.revolsys.swing.map.layer.raster;

import org.slf4j.LoggerFactory;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.map.layer.MapTile;

public class TileLoadTask implements Runnable {
  private final MapTile mapTile;

  private final GeometryFactory geometryFactory;

  private final TiledImageLayerRenderer renderer;

  public TileLoadTask(final TiledImageLayerRenderer renderer,
    final GeometryFactory geometryFactory, final MapTile mapTile) {
    this.renderer = renderer;
    this.geometryFactory = geometryFactory;
    this.mapTile = mapTile;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public MapTile getMapTile() {
    return mapTile;
  }

  public TiledImageLayerRenderer getRenderer() {
    return renderer;
  }

  @Override
  public void run() {
    try {
      this.mapTile.loadImage(this.geometryFactory);
      this.renderer.setLoaded(this);
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Unable to load " + this.mapTile, e);
    }
  }

}
