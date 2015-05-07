package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.gis.cs.BoundingBox;
import com.vividsolutions.jts.geom.TopologyException;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractRecordLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.ExceptionUtil;

/**
 * Use the first renderer which is visible at the current scale, ignore all
 * others. Changes when the scale changes.
 */
public class ScaleMultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = Icons.getIcon("style_scale");

  private transient long lastScale = 0;

  private transient AbstractDataObjectLayerRenderer renderer;

  public ScaleMultipleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public ScaleMultipleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    super("scaleStyle", layer, parent, style);
    setIcon(ICON);
  }

  private AbstractDataObjectLayerRenderer getRenderer(final Viewport2D viewport) {
    final long scale = (long)viewport.getScale();
    if (scale == this.lastScale && this.renderer != null) {
      if (this.renderer.isVisible(scale)) {
        return this.renderer;
      } else {
        return null;
      }
    } else {
      this.lastScale = scale;
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(scale)) {
          this.renderer = renderer;
          return renderer;
        }
      }
      return null;
    }
  }

  @Override
  public boolean isVisible(final LayerDataObject object) {
    if (super.isVisible() && super.isVisible(object)) {
      if (this.renderer != null) {
        return this.renderer.isVisible(object);
      }
    }
    return false;
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final AbstractRecordLayer layer) {
    if (layer.hasGeometryAttribute()) {
      final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
      if (renderer != null) {
        renderer.render(viewport, graphics, layer);
      }
    }
  }

  // NOTE: Needed for filter styles
  @Override
  public void renderRecord(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final AbstractRecordLayer layer, final LayerDataObject object) {
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      if (isVisible(object)) {
        try {
          renderer.renderRecord(viewport, graphics, visibleArea, layer, object);
        } catch (final TopologyException e) {
        } catch (final Throwable e) {
          ExceptionUtil.log(getClass(), "Unabled to render " + layer.getName()
            + " #" + object.getIdString(), e);
        }
      }
    }
  }

  @Override
  // NOTE: Needed for multiple styles
  protected void renderRecords(final Viewport2D viewport,
    final Graphics2D graphics, final AbstractRecordLayer layer,
    final List<LayerDataObject> objects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      for (final LayerDataObject object : objects) {
        if (isVisible(object)) {
          try {
            renderer.renderRecord(viewport, graphics, visibleArea, layer,
              object);
          } catch (final TopologyException e) {
          }
        }
      }
    }
  }

  @Override
  public void renderSelectedRecord(final Viewport2D viewport,
    final Graphics2D graphics, final AbstractRecordLayer layer,
    final LayerDataObject object) {
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      if (isVisible(object)) {
        try {
          renderer.renderSelectedRecord(viewport, graphics, layer, object);
        } catch (final Throwable e) {
          ExceptionUtil.log(getClass(), "Unabled to render " + layer.getName()
            + " #" + object.getIdString(), e);
        }
      }
    }
  }
}
