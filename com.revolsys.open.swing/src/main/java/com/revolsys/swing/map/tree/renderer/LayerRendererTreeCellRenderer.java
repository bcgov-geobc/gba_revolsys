package com.revolsys.swing.map.tree.renderer;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerRenderer;

public class LayerRendererTreeCellRenderer extends DefaultTreeCellRenderer {
  /**
   *
   */
  private static final long serialVersionUID = -7481451746832997079L;

  private static final Icon VISIBLE_ICON = Icons.getIcon("map");

  private static final Icon VISIBLE_DISABLED_ICON = Icons.getIcon("map_gray");

  private static final Map<List<Icon>, Icon> ICON_CACHE = new HashMap<List<Icon>, Icon>();

  private static Icon getIcon(final Component component, final LayerRenderer<?> renderer) {
    final List<Icon> icons = new ArrayList<Icon>();
    if (renderer.isVisible()) {
      icons.add(VISIBLE_ICON);
    } else {
      icons.add(VISIBLE_DISABLED_ICON);
    }

    if (icons.isEmpty()) {
      return null;
    } else if (icons.size() == 1) {
      return icons.get(0);
    } else {
      Icon icon = ICON_CACHE.get(icons);
      if (icon == null) {
        icon = merge(component, icons, 5);
        ICON_CACHE.put(icons, icon);
      }
      return icon;
    }
  }

  private static Icon merge(final Component component, final List<Icon> icons, final int space) {

    final MediaTracker tracker = new MediaTracker(component);

    int maxWidth = 0;
    int maxHeight = 0;
    int i = 0;
    for (final Icon icon : icons) {
      if (icon != null) {
        final Image image = ((ImageIcon)icon).getImage();
        tracker.addImage(image, i);
        maxWidth += icon.getIconWidth();
        maxHeight = Math.max(maxHeight, icon.getIconHeight());
        i++;
      }
    }
    maxWidth += (i - 1) * space;

    try {
      tracker.waitForAll();
    } catch (final InterruptedException e) {
      throw new ThreadInterruptedException(e);
    }

    if (maxWidth == 0) {
      return null;
    }
    if (maxHeight == 0) {
      return null;
    }

    final BufferedImage newImage = new BufferedImage(maxWidth, maxHeight,
      BufferedImage.TYPE_INT_ARGB);

    final Graphics g = newImage.createGraphics();
    int x = 0;
    for (final Icon icon : icons) {
      if (icon != null) {
        final Image image = ((ImageIcon)icon).getImage();
        final int iconWidth = icon.getIconWidth();
        final int iconHeight = icon.getIconHeight();
        g.drawImage(image, x, 0, iconWidth, iconHeight, component);
        x += iconWidth;
        x += space;
      }
    }

    return new ImageIcon(newImage);
  }

  public LayerRendererTreeCellRenderer() {
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value,
    final boolean selected, final boolean expanded, final boolean leaf, final int row,
    final boolean hasFocus) {
    if (value instanceof LayerRenderer) {
      final LayerRenderer<?> renderer = (LayerRenderer<?>)value;
      final Icon icon = getIcon(this, renderer);
      setIcon(icon);
      setText(renderer.getName());
    }
    final JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, selected,
      expanded, leaf, row, hasFocus);
    if (value instanceof LayerRenderer) {
      final LayerRenderer<?> renderer = (LayerRenderer<?>)value;
      final Icon icon = getIcon(this, renderer);
      setIcon(icon);
      label.setText(renderer.getName());
    }
    return label;
  }
}
