package com.revolsys.swing;

import com.revolsys.elevation.gridded.rasterizer.ColourGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisRestServer;
import com.revolsys.swing.map.layer.bing.Bing;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.MultipleGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.elevation.gridded.RasterizerGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.elevation.gridded.TiledGriddedElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.TiledMultipleGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.elevation.tin.TriangulatedIrregularNetworkLayer;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.grid.GridLayerRenderer;
import com.revolsys.swing.map.layer.mapguide.MapGuideWebServer;
import com.revolsys.swing.map.layer.ogc.wms.OgcWms;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapApiLayer;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapLayer;
import com.revolsys.swing.map.layer.pointcloud.PointCloudLayer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayer;
import com.revolsys.swing.map.layer.record.FileRecordLayer;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;
import com.revolsys.swing.map.layer.record.renderer.FilterMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.ScaleMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.marker.SvgMarker;
import com.revolsys.swing.map.layer.record.style.marker.TextMarker;
import com.revolsys.swing.map.layer.webmercatortilecache.WebMercatorTileCache;
import com.revolsys.swing.map.layer.wikipedia.WikipediaBoundingBoxLayerWorker;
import com.revolsys.swing.map.symbol.SymbolLibrary;
import com.revolsys.util.ServiceInitializer;

public class RsSwingServiceInitializer implements ServiceInitializer {
  private static void markers() {
    MapObjectFactoryRegistry.newFactory("markerText", "Marker Font and Text", TextMarker::new);
    MapObjectFactoryRegistry.newFactory("markerSvg", "Marker SVG", SvgMarker::new);
  }

  @Override
  public void initializeService() {
    SymbolLibrary.factoryInit();
    markers();
    layerRenderers();
    layers();
  }

  private void layerRenderers() {
    MapObjectFactoryRegistry.newFactory("geometryStyle", GeometryStyleRenderer::new);
    MapObjectFactoryRegistry.newFactory("textStyle", TextStyleRenderer::new);
    MapObjectFactoryRegistry.newFactory("markerStyle", MarkerStyleRenderer::new);
    MapObjectFactoryRegistry.newFactory("multipleStyle", MultipleRenderer::new);
    MapObjectFactoryRegistry.newFactory("scaleStyle", ScaleMultipleRenderer::new);
    MapObjectFactoryRegistry.newFactory("filterStyle", FilterMultipleRenderer::new);
    MapObjectFactoryRegistry.newFactory("gridLayerRenderer", GridLayerRenderer::new);

    MapObjectFactoryRegistry.newFactory("hillShadeGriddedElevationModelRasterizer",
      HillShadeGriddedElevationModelRasterizer::new);
    MapObjectFactoryRegistry.newFactory("colourGriddedElevationModelRasterizer",
      ColourGriddedElevationModelRasterizer::new);
    MapObjectFactoryRegistry.newFactory("rasterizerGriddedElevationModelLayerRenderer",
      RasterizerGriddedElevationModelLayerRenderer::new);

    MapObjectFactoryRegistry.newFactory("multipleGriddedElevationModelLayerRenderer",
      MultipleGriddedElevationModelLayerRenderer::new);

    MapObjectFactoryRegistry.newFactory("tiledMultipleGriddedElevationModelLayerRenderer",
      TiledMultipleGriddedElevationModelLayerRenderer::new);
  }

  private void layers() {
    MapObjectFactoryRegistry.newFactory("layerGroup", "Layer Group", LayerGroup::newLayer);

    MapObjectFactoryRegistry.newFactory("baseMapLayerGroup", "Base Map Layer Group",
      BaseMapLayerGroup::newLayer);

    MapObjectFactoryRegistry.newFactory("recordFileLayer", "File", FileRecordLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("recordStoreLayer", "Record Store Layer",
      RecordStoreLayer::new);

    MapObjectFactoryRegistry.newFactory("openStreetMapVectorApi", "Open Street Map (Vector API)",
      OpenStreetMapApiLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("gridLayer", "Grid Layer", GridLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("wikipedia", "Wikipedia Articles",
      WikipediaBoundingBoxLayerWorker::newLayer);

    MapObjectFactoryRegistry.newFactory("geoname", "Geoname.org",
      GeoNamesBoundingBoxLayerWorker::newLayer);

    MapObjectFactoryRegistry.newFactory("geoReferencedImageLayer", "Geo-referenced Image Layer",
      GeoreferencedImageLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("griddedElevationModelLayer",
      "Gridded Elevation Model Layer", GriddedElevationModelLayer::new);

    MapObjectFactoryRegistry.newFactory("tiledGriddedElevationModelLayer",
      "Tiled Gridded Elevation Model Layer", TiledGriddedElevationModelLayer::new);

    MapObjectFactoryRegistry.newFactory("triangulatedIrregularNetworkLayer",
      "Triangulated Irregular Network Layer", TriangulatedIrregularNetworkLayer::new);

    MapObjectFactoryRegistry.newFactory("pointCloudLayer", "Point Cloud Layer",
      PointCloudLayer::new);

    ArcGisRestServer.factoryInit();

    Bing.factoryInit();

    MapGuideWebServer.factoryInit();

    OgcWms.factoryInit();

    MapObjectFactoryRegistry.newFactory("openStreetMap", "Open Street Map Tiles",
      OpenStreetMapLayer::new);

    WebMercatorTileCache.factoryInit();
  }

}