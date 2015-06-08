package com.revolsys.swing.map.tree.renderer;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerGroup;

public class LayerGroupTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = 7356001100251805839L;

  public LayerGroupTreeCellRenderer() {
    final ImageIcon icon = Icons.getIcon("folder");
    setOpenIcon(icon);
    setClosedIcon(icon);
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value,
    final boolean selected, final boolean expanded, final boolean leaf, final int row,
    final boolean hasFocus) {
    final JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected,
      expanded, leaf, row, hasFocus);
    if (value instanceof LayerGroup) {
      final LayerGroup layer = (LayerGroup)value;
      label.setText(layer.getName());
    }
    return label;
  }
}
