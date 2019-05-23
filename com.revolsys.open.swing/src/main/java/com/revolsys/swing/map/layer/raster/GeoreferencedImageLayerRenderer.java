package com.revolsys.swing.map.layer.raster;

import java.awt.RenderingHints;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;

public class GeoreferencedImageLayerRenderer
  extends AbstractLayerRenderer<GeoreferencedImageLayer> {

  public GeoreferencedImageLayerRenderer(final GeoreferencedImageLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final ViewRenderer view, final GeoreferencedImageLayer layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      if (!layer.isEditable()) {
        final GeoreferencedImage image = layer.getImage();
        if (image != null) {
          BoundingBox boundingBox = layer.getBoundingBox();
          if (boundingBox == null || boundingBox.isEmpty()) {
            boundingBox = layer.fitToViewport();
          }
          if (!view.isCancelled()) {
            final GeometryFactory viewGeometryFactory = view.getGeometryFactory();
            if (image.isSameCoordinateSystem(viewGeometryFactory)) {
              view.drawImage(image, true, layer.getOpacity() / 255.0,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else {
              final GeoreferencedImage projectedImage = image.imageToCs(viewGeometryFactory);
              view.drawImage(projectedImage, false, layer.getOpacity() / 255.0,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }
          }
          if (!view.isCancelled()) {
            view.drawDifferentCoordinateSystem(boundingBox);
          }
        }
      }
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
