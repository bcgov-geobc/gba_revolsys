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

import com.revolsys.awt.WebColors;
import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.Layer;

public class LayerTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final long serialVersionUID = -7481451746832997079L;

  private static final Icon EDIT_ICON = Icons.getIcon("pencil");

  private static final Icon EDIT_DISABLED_ICON = Icons.getIcon("pencil_gray");

  private static final Icon SELECT_ICON = Icons.getIcon("map_select");

  private static final Icon SELECT_DISABLED_ICON = Icons.getIcon("map_select_gray");

  private static final Icon VISIBLE_ICON = Icons.getIcon("map");

  private static final Icon NOT_EXISTS_ICON = Icons.getIcon("error");

  private static final Icon VISIBLE_DISABLED_ICON = Icons.getIcon("map_gray");

  private static final Map<List<Icon>, Icon> ICON_CACHE = new HashMap<List<Icon>, Icon>();

  private static Icon getIcon(final Component component, final Layer layer) {
    final List<Icon> icons = new ArrayList<Icon>();
    if (!layer.isExists() && layer.isInitialized()) {

      return NOT_EXISTS_ICON;
    } else if (layer.getRenderer() != null) {
      if (layer.isVisible()) {
        icons.add(VISIBLE_ICON);
      } else {
        icons.add(VISIBLE_DISABLED_ICON);
      }
      if (layer.isSelectSupported()) {
        if (layer.isSelectable()) {
          icons.add(SELECT_ICON);
        } else {
          icons.add(SELECT_DISABLED_ICON);
        }
      }
    }
    if (!layer.isReadOnly()) {
      if (layer.isEditable()) {
        icons.add(EDIT_ICON);
      } else {
        icons.add(EDIT_DISABLED_ICON);
      }
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

  private static Icon merge(final Component component, final List<Icon> icons,
    final int space) {

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

  public LayerTreeCellRenderer() {
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, Object value,
    final boolean selected, final boolean expanded, final boolean leaf,
    final int row, final boolean hasFocus) {
    Icon icon = null;
    boolean exists = false;
    boolean initialized = false;
    if (value instanceof Layer) {
      final Layer layer = (Layer)value;
      value = layer.getName();
      icon = getIcon(this, layer);
      exists = layer.isExists();
      initialized = layer.isInitialized();
    } else {
      icon = NOT_EXISTS_ICON;
    }

    final JLabel label = (JLabel)super.getTreeCellRendererComponent(tree,
      value, selected, expanded, leaf, row, hasFocus);
    setIcon(icon);
    if (!initialized) {
      label.setEnabled(false);
    } else if (!exists) {
      label.setForeground(WebColors.Red);
    } else {
      label.setEnabled(true);
    }

    return label;
  }
}
