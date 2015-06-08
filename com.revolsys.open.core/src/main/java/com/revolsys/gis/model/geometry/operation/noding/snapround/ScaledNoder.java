package com.revolsys.gis.model.geometry.operation.noding.snapround;

import java.util.Collection;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.operation.chain.NodedSegmentString;
import com.revolsys.gis.model.geometry.operation.chain.Noder;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.vividsolutions.jts.util.CollectionUtil;

/**
 * Wraps a {@link Noder} and transforms its input into the integer domain. This
 * is intended for use with Snap-Rounding noders, which typically are only
 * intended to work in the integer domain. Offsets can be provided to increase
 * the number of digits of available precision.
 *
 * @version 1.7
 */
public class ScaledNoder implements Noder {
  private final Noder noder;

  private final double scaleFactor;

  private double offsetX;

  private double offsetY;

  private boolean isScaled = false;

  public ScaledNoder(final Noder noder, final double scaleFactor) {
    this(noder, scaleFactor, 0, 0);
  }

  public ScaledNoder(final Noder noder, final double scaleFactor, final double offsetX,
    final double offsetY) {
    this.noder = noder;
    this.scaleFactor = scaleFactor;
    // no need to scale if input precision is already integral
    this.isScaled = !isIntegerPrecision();
  }

  @Override
  public void computeNodes(final Collection inputSegStrings) {
    Collection intSegStrings = inputSegStrings;
    if (this.isScaled) {
      intSegStrings = scale(inputSegStrings);
    }
    this.noder.computeNodes(intSegStrings);
  }

  @Override
  public Collection getNodedSubstrings() {
    final Collection splitSS = this.noder.getNodedSubstrings();
    if (this.isScaled) {
      rescale(splitSS);
    }
    return splitSS;
  }

  public boolean isIntegerPrecision() {
    return this.scaleFactor == 1.0;
  }

  private void rescale(final Collection segStrings) {
    // System.out.println("Rescaled: scaleFactor = " + scaleFactor);
    CollectionUtil.apply(segStrings, new CollectionUtil.Function() {
      @Override
      public Object execute(final Object obj) {
        final SegmentString ss = (SegmentString)obj;
        rescale(ss.getCoordinates());
        return null;
      }
    });
  }

  private void rescale(final CoordinatesList pts) {
    for (int i = 0; i < pts.size(); i++) {
      pts.setX(i, pts.getX(i) / this.scaleFactor + this.offsetX);
      pts.setY(i, pts.getY(i) / this.scaleFactor + this.offsetY);
    }
  }

  // private double scale(double val) { return (double) Math.round(val *
  // scaleFactor); }

  private Collection scale(final Collection segStrings) {
    // System.out.println("Scaled: scaleFactor = " + scaleFactor);
    return CollectionUtil.transform(segStrings, new CollectionUtil.Function() {
      @Override
      public Object execute(final Object obj) {
        final SegmentString ss = (SegmentString)obj;
        return new NodedSegmentString(scale(ss.getCoordinates()), ss.getData());
      }
    });
  }

  private CoordinatesList scale(final CoordinatesList pts) {
    final CoordinatesList roundPts = new DoubleCoordinatesList(pts.size(), pts.getNumAxis());
    for (int i = 0; i < pts.size(); i++) {
      roundPts.setX(i, Math.round((pts.getX(i) - this.offsetX) * this.scaleFactor));
      roundPts.setX(i, Math.round((pts.getY(i) - this.offsetY) * this.scaleFactor));
    }
    final CoordinatesList roundPtsNoDup = CoordinatesListUtil.removeRepeatedPoints(roundPts);
    return roundPtsNoDup;
  }

  // private double rescale(double val) { return val / scaleFactor; }
}
