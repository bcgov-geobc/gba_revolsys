package com.revolsys.swing.map.layer.raster;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.raster.GeoReferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;

public class GeoReferencedImageLayerRenderer extends
  AbstractLayerRenderer<GeoReferencedImageLayer> {

  public static void render(final Viewport2D viewport,
    final Graphics2D graphics, final GeoReferencedImage image,
    final boolean useTransform) {
    if (image != null) {
      final BoundingBox viewBoundingBox = viewport.getBoundingBox();
      final int viewWidth = viewport.getViewWidthPixels();
      final int viewHeight = viewport.getViewHeightPixels();
      image.drawImage(graphics, viewBoundingBox, viewWidth, viewHeight,
        useTransform);
    }
  }

  public static void renderAlpha(final Graphics2D graphics,
    final Viewport2D viewport, final GeoReferencedImage image,
    final double alpha, final boolean useTransform) {
    final Composite composite = graphics.getComposite();
    try {
      AlphaComposite alphaComposite = AlphaComposite.SrcOver;
      if (alpha < 1) {
        alphaComposite = alphaComposite.derive((float)alpha);
      }
      graphics.setComposite(alphaComposite);
      render(viewport, graphics, image, useTransform);
    } finally {
      graphics.setComposite(composite);
    }
  }

  public GeoReferencedImageLayerRenderer(final GeoReferencedImageLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final GeoReferencedImageLayer layer) {
    final double scale = viewport.getScale();
    if (layer.isVisible(scale)) {
      if (!layer.isEditable()) {
        final GeoReferencedImage image = layer.getImage();
        if (image != null) {
          BoundingBox boundingBox = layer.getBoundingBox();
          if (boundingBox == null || boundingBox.isEmpty()) {
            boundingBox = layer.fitToViewport();
          }
          renderAlpha(graphics, viewport, image, 1.0, true);
        }
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    return Collections.emptyMap();
  }
}
