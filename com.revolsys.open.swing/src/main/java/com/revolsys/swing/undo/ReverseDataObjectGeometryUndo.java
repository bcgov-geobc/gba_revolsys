package com.revolsys.swing.undo;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.vividsolutions.jts.geom.Geometry;

public class ReverseDataObjectGeometryUndo extends AbstractUndoableEdit {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Record object;

  private final Geometry oldValue;

  public ReverseDataObjectGeometryUndo(final Record object) {
    this.object = object;
    this.oldValue = object.getGeometryValue();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Geometry value = this.object.getGeometryValue();
      if (EqualsRegistry.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Geometry value = this.object.getGeometryValue();
      if (EqualsRegistry.equal(value.reverse(), this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(this.object);
    property.reverseGeometry(this.object);
  }

  @Override
  protected void doUndo() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(this.object);
    property.reverseGeometry(this.object);
  }

  @Override
  public String toString() {
    return "Reverse geometry";
  }
}
