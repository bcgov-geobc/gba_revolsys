package com.revolsys.swing.tree;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.util.Property;

public class TreeNodePropertyEnableCheck extends AbstractEnableCheck {
  private final String propertyName;

  private final Object value;

  private boolean inverse = false;

  public TreeNodePropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public TreeNodePropertyEnableCheck(final String propertyName, final Object value) {
    this(propertyName, value, false);
  }

  public TreeNodePropertyEnableCheck(final String propertyName, final Object value,
    final boolean inverse) {
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean isEnabled() {
    final BaseTreeNode node = BaseTree.getMenuNode();
    if (node == null) {
      return disabled();
    } else {
      try {
        final Object value = Property.get(node, this.propertyName);
        if (this.inverse != EqualsRegistry.equal(value, this.value)) {
          return enabled();
        } else {
          return disabled();
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).debug("Enable check not valid", e);
        return disabled();
      }
    }
  }

  @Override
  public String toString() {
    return this.propertyName + "=" + this.value;
  }
}
