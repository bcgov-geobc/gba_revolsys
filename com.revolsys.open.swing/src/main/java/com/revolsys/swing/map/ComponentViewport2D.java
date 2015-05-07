package com.revolsys.swing.map;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.Measurable;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import javax.swing.JComponent;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class ComponentViewport2D extends Viewport2D implements
  PropertyChangeListener {

  private final JComponent component;

  private int maxIntegerDigits;

  private int maxDecimalDigits;

  public ComponentViewport2D(final Project project, final JComponent component) {
    super(project);
    this.component = component;
    Property.addListener(project, "geometryFactory", this);
    Property.addListener(project, "viewBoundingBox", this);
    component.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        if (isInitialized()) {
          updateCachedFields();
        }
      }
    });
  }

  /**
   * Get the bounding box for the dimensions of the viewport at the specified
   * scale, centred at the x, y model coordinates.
   * 
   * @param x The model x coordinate.
   * @param y The model y coordinate.
   * @param scale The scale.
   * @return The bounding box.
   */
  public BoundingBox getBoundingBox(final double x, final double y,
    final double scale) {
    final double width = getModelWidth(scale);
    final double height = getModelHeight(scale);

    final double x1 = x - width / 2;
    final double y1 = y - height / 2;
    final double x2 = x1 + width;
    final double y2 = y1 + height;
    final BoundingBox boundingBox = new BoundingBox(getGeometryFactory(), x1,
      y1, x2, y2);
    return boundingBox;
  }

  /**
   * Get the bounding box in model units for the pair of coordinates in view
   * units. The bounding box will be clipped to the model's bounding box.
   * 
   * @param x1 The first x value.
   * @param y1 The first y value.
   * @param x2 The second x value.
   * @param y2 The second y value.
   * @return The bounding box.
   */
  public BoundingBox getBoundingBox(final double x1, final double y1,
    final double x2, final double y2) {
    final double[] c1 = toModelCoordinates(x1, y1);
    final double[] c2 = toModelCoordinates(x2, y2);
    final BoundingBox boundingBox = new BoundingBox(getGeometryFactory(),
      c1[0], c1[1], c2[0], c2[1]);

    // Clip the bounding box with the map's visible area
    BoundingBox intersection = boundingBox.intersection(boundingBox);
    // If the clipped bounding box is null then move the map to the new BBOX
    if (intersection.isEmpty()) {
      intersection = boundingBox;
    }
    return intersection;
  }

  /**
   * Get the bounding box for the dimensions of the viewport at the specified
   * scale, centred at the x, y view coordinates.
   * 
   * @param x The view x coordinate.
   * @param y The view y coordinate.
   * @param scale The scale.
   * @return The bounding box.
   */
  public BoundingBox getBoundingBox(final int x, final int y, final double scale) {
    final double[] ordinates = toModelCoordinates(x, y);
    final double mapX = ordinates[0];
    final double mapY = ordinates[1];
    return getBoundingBox(mapX, mapY, scale);
  }

  public double getMaxScale() {
    final BoundingBox areaBoundingBox = getGeometryFactory().getCoordinateSystem()
      .getAreaBoundingBox();
    final Measurable<Length> areaWidth = areaBoundingBox.getWidthLength();
    final Measurable<Length> areaHeight = areaBoundingBox.getHeightLength();

    final Measurable<Length> viewWidth = getViewWidthLength();
    final Measurable<Length> viewHeight = getViewHeightLength();

    final double maxHorizontalScale = getScale(viewWidth, areaWidth);
    final double maxVerticalScale = getScale(viewHeight, areaHeight);
    final double maxScale = Math.max(maxHorizontalScale, maxVerticalScale);
    return maxScale;
  }

  public double getModelHeight(final double scale) {
    final Unit<Length> scaleUnit = getScaleUnit(scale);

    final Measurable<Length> viewHeight = getViewHeightLength();
    final double height = viewHeight.doubleValue(scaleUnit);
    return height;
  }

  public <Q extends Quantity> Unit<Q> getModelToScreenUnit(
    final Unit<Q> modelUnit) {
    final double viewWidth = getViewWidthPixels();
    final double modelWidth = getModelWidth();
    return modelUnit.times(viewWidth).divide(modelWidth);
  }

  public double getModelWidth(final double scale) {
    final Unit<Length> scaleUnit = getScaleUnit(scale);

    final Measurable<Length> viewWidth = getViewWidthLength();
    final double width = viewWidth.doubleValue(scaleUnit);
    return width;
  }

  /**
   * Get the rectangle in view units for the pair of coordinates in view units.
   * The bounding box will be clipped to the view's dimensions.
   * 
   * @param x1 The first x value.
   * @param y1 The first y value.
   * @param x2 The second x value.
   * @param y2 The second y value.
   * @return The rectangle.
   */
  public Rectangle getRectangle(final int x1, final int y1, final int x2,
    final int y2) {
    final int x3 = Math.min(getViewWidthPixels() - 1, Math.max(0, x2));
    final int y3 = Math.min(getViewHeightPixels() - 1, Math.max(0, y2));

    final int x = Math.min(x1, x3);
    final int y = Math.min(y1, y3);
    final int width = Math.abs(x1 - x3);
    final int height = Math.abs(y1 - y3);
    return new Rectangle(x, y, width, height);
  }

  public Unit<Length> getScaleUnit(final double scale) {
    final Unit<Length> lengthUnit = getGeometryFactory().getCoordinateSystem()
      .getLengthUnit();
    final Unit<Length> scaleUnit = lengthUnit.divide(scale);
    return scaleUnit;
  }

  public <Q extends Quantity> Unit<Q> getScreenToModelUnit(
    final Unit<Q> modelUnit) {
    final double viewWidth = getViewWidthPixels();
    final double modelWidth = getModelWidth();
    return modelUnit.times(modelWidth).divide(viewWidth);
  }

  public BoundingBox getValidBoundingBox(final BoundingBox boundingBox) {
    BoundingBox validBoundingBox = boundingBox;
    final double viewAspectRatio = getViewAspectRatio();
    double modelWidth = validBoundingBox.getWidth();
    double modelHeight = validBoundingBox.getHeight();

    /*
     * If the new bounding box has a zero width and height, expand it by 50 view
     * units.
     */
    if (modelWidth == 0 && modelHeight == 0) {
      validBoundingBox = validBoundingBox.expand(
        getModelUnitsPerViewUnit() * 50, getModelUnitsPerViewUnit() * 50);
      modelWidth = validBoundingBox.getWidth();
      modelHeight = validBoundingBox.getHeight();
    }
    final double newModelAspectRatio = modelWidth / modelHeight;
    double modelUnitsPerViewUnit;
    if (viewAspectRatio <= newModelAspectRatio) {
      modelUnitsPerViewUnit = modelWidth / getViewWidthPixels();
    } else {
      modelUnitsPerViewUnit = modelHeight / getViewHeightPixels();
    }
    final double logUnits = Math.log10(Math.abs(modelUnitsPerViewUnit));
    if (logUnits < 0 && Math.abs(Math.floor(logUnits)) > this.maxDecimalDigits) {
      validBoundingBox = new BoundingBox(validBoundingBox);
      modelUnitsPerViewUnit = 2 * Math.pow(10, -this.maxDecimalDigits);
      final double minModelWidth = getViewWidthPixels() * modelUnitsPerViewUnit;
      final double minModelHeight = getViewHeightPixels()
        * modelUnitsPerViewUnit;
      validBoundingBox = validBoundingBox.expand(
        (minModelWidth - modelWidth) / 2, (minModelHeight - modelHeight) / 2);
    }
    return validBoundingBox;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (isInitialized() && event.getSource() == getProject()) {
      if (event.getPropertyName().equals("viewBoundingBox")) {
        // final BoundingBox boundingBox = (BoundingBox)event.getNewValue();
        // if (isInitialized()) {
        // updateCachedFields();
        // } else {
        // setBoundingBoxInternal(boundingBox);
        // }
      } else {
        Invoke.later(this, "updateCachedFields");
      }
    }
  }

  public void repaint() {
    this.component.repaint();
  }

  /**
   * Set the coordinate system the map is displayed in.
   * 
   * @param coordinateSystem The coordinate system the map is displayed in.
   */
  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != getGeometryFactory()) {
      final GeometryFactory oldGeometryFactory = getGeometryFactory();
      super.setGeometryFactory(geometryFactory);
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      final double minX = areaBoundingBox.getMinX();
      final double maxX = areaBoundingBox.getMaxX();
      final double minY = areaBoundingBox.getMinY();
      final double maxY = areaBoundingBox.getMaxY();
      final double logMinX = Math.log10(Math.abs(minX));
      final double logMinY = Math.log10(Math.abs(minY));
      final double logMaxX = Math.log10(Math.abs(maxX));
      final double logMaxY = Math.log10(Math.abs(maxY));
      final double maxLog = Math.abs(Math.max(Math.max(logMinX, logMinY),
        Math.max(logMaxX, logMaxY)));
      this.maxIntegerDigits = (int)Math.floor(maxLog + 1);
      this.maxDecimalDigits = 15 - this.maxIntegerDigits;
      getPropertyChangeSupport().firePropertyChange("geometryFactory",
        oldGeometryFactory, geometryFactory);
      final BoundingBox boundingBox = getBoundingBox();
      if (boundingBox != null) {
        final BoundingBox newBoundingBox = boundingBox.convert(geometryFactory);
        final BoundingBox intersection = newBoundingBox.intersection(areaBoundingBox);
        if (intersection.isEmpty()) {
          setBoundingBox(areaBoundingBox);
        } else {
          setBoundingBox(intersection);
        }
      }
    }
  }

  @Override
  public void setInitialized(final boolean initialized) {
    if (initialized && !isInitialized()) {
      updateCachedFields();
    }
    super.setInitialized(initialized);
  }

  public void translate(final double dx, final double dy) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox newBoundingBox = new BoundingBox(
      boundingBox.getGeometryFactory(), boundingBox.getMinX() + dx,
      boundingBox.getMinY() + dy, boundingBox.getMaxX() + dx,
      boundingBox.getMaxY() + dy);
    setBoundingBox(newBoundingBox);

  }

  @Override
  public void update() {
    repaint();
  }

  public void updateCachedFields() {
    final LayerGroup project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    if (geometryFactory != null) {
      if (geometryFactory != getGeometryFactory()) {
        setGeometryFactory(geometryFactory);
      }
      final Insets insets = this.component.getInsets();

      final int viewWidth = this.component.getWidth() - insets.left
        - insets.right;
      final int viewHeight = this.component.getHeight() - insets.top
        - insets.bottom;

      setViewWidth(viewWidth);
      setViewHeight(viewHeight);
      setBoundingBox(getBoundingBox());

      this.component.repaint();
    }
  }
}
