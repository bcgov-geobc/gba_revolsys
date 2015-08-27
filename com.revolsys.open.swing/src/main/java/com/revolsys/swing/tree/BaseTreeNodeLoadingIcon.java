package com.revolsys.swing.tree;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.commons.collections4.set.MapBackedSet;

import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class BaseTreeNodeLoadingIcon implements ImageObserver {
  private static final ImageIcon ICON = createIcon();

  private static final BaseTreeNodeLoadingIcon INSTANCE = new BaseTreeNodeLoadingIcon();

  private static final Set<BaseTreeNode> NODES = MapBackedSet
    .mapBackedSet(new WeakHashMap<BaseTreeNode, Object>());

  public static synchronized void addNode(final BaseTreeNode node) {
    if (node != null) {
      NODES.add(node);
      ICON.setImageObserver(INSTANCE);
      // Debug.println(new ArrayList<>(NODES));
    }
  }

  private static ImageIcon createIcon() {
    final Class<?> clazz = Icons.class;
    final String resourceName = Icons.RESOURCE_FOLDER + "loading.gif";
    final URL resource = clazz.getResource(resourceName);
    return new ImageIcon(resource);
  }

  public static Icon getIcon() {
    return ICON;
  }

  private static synchronized List<BaseTreeNode> getNodes() {
    return new ArrayList<>(NODES);
  }

  public static synchronized void removeNode(final BaseTreeNode node) {
    NODES.remove(node);
    // Debug.println(new ArrayList<>(NODES));
    if (NODES.isEmpty()) {
      ICON.setImageObserver(null);
    }
  }

  public BaseTreeNodeLoadingIcon() {
  }

  @Override
  public boolean imageUpdate(final Image image, final int flags, final int x, final int y,
    final int width, final int height) {
    if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
      for (final BaseTreeNode node : getNodes()) {
        final JTree tree = node.getTree();
        if (tree != null) {
          final TreePath path = node.getTreePath();
          final Rectangle rect = tree.getPathBounds(path);
          if (rect != null) {
            tree.repaint(rect);
          }
        }
      }
    }
    return (flags & (ALLBITS | ABORT)) == 0;
  }
}
