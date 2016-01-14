package com.revolsys.swing.tree;

import java.awt.Rectangle;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.revolsys.collection.EmptyReference;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.dnd.TreeTransferHandler;

public class BaseTree extends JTree {
  private static Reference<BaseTreeNode> menuNode = new EmptyReference<>();

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unchecked")
  public static <V extends BaseTreeNode> V getMenuNode() {
    return (V)menuNode.get();
  }

  protected static void setMenuNode(final BaseTreeNode menuNode) {
    BaseTree.menuNode = new WeakReference<>(menuNode);
  }

  private boolean menuEnabled = true;

  private BaseTreeListener treeListener = new BaseTreeListener(this);

  public BaseTree(final BaseTreeNode root) {
    super(new DefaultTreeModel(root, true));
    setRoot(root);
    setRootVisible(true);
    setShowsRootHandles(true);
    setLargeModel(true);
    setToggleClickCount(0);
    setRowHeight(0);
    setCellRenderer(new BaseTreeCellRenderer());
    setExpandsSelectedPaths(true);
    setTransferHandler(new TreeTransferHandler());
    setDragEnabled(true);
    setDropMode(DropMode.ON_OR_INSERT);
    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setSelectionModel(selectionModel);
    expandPath(root);
  }

  public void collapsePath(final List<Object> items) {
    final TreePath path = getTreePath(items);
    collapsePath(path);
  }

  public void collapsePath(final Object... items) {
    if (items != null) {
      collapsePath(Arrays.asList(items));
    }
  }

  @Override
  public void collapsePath(final TreePath path) {
    final Object node = path.getLastPathComponent();
    if (node instanceof BaseTreeNode) {
      final BaseTreeNode treeNode = (BaseTreeNode)node;
      treeNode.collapseChildren();
      treeNode.nodeCollapsed(treeNode);
    }
    super.collapsePath(path);
  }

  public void expandAllNodes() {
    expandAllNodes(0, getRowCount());
  }

  private void expandAllNodes(final int startingIndex, final int rowCount) {
    for (int i = startingIndex; i < rowCount; ++i) {
      expandRow(i);
    }

    if (getRowCount() != rowCount) {
      expandAllNodes(rowCount, getRowCount());
    }
  }

  public void expandPath(final List<?> items) {
    final TreePath path = getTreePath(items);
    expandPath(path);
  }

  public void expandPath(final Object... items) {
    if (items != null) {
      expandPath(Arrays.asList(items));
    }
  }

  public MenuFactory getMenuFactory(final TreePath path) {
    final Object node = path.getLastPathComponent();
    if (node instanceof BaseTreeNode) {
      final BaseTreeNode treeNode = (BaseTreeNode)node;
      return treeNode.getMenu();
    } else {
      return null;
    }
  }

  @Override
  public DefaultTreeModel getModel() {
    return (DefaultTreeModel)super.getModel();
  }

  @Override
  public Rectangle getPathBounds(final TreePath path) {
    final Object lastPathComponent = path.getLastPathComponent();
    if (lastPathComponent instanceof BaseTreeNode) {
      final BaseTreeNode treeNode = (BaseTreeNode)lastPathComponent;
      if (!treeNode.isVisible()) {
        return null;
      }
    }
    return super.getPathBounds(path);
  }

  public BaseTreeNode getRootNode() {
    final TreeModel model = getModel();
    final BaseTreeNode root = (BaseTreeNode)model.getRoot();
    return root;
  }

  public TreePath getTreePath(final List<?> items) {
    final BaseTreeNode root = getRootNode();
    if (root == null) {
      return null;
    } else {
      return root.getTreePath(items);
    }

  }

  public boolean isMenuEnabled() {
    return this.menuEnabled;
  }

  @Override
  protected void setExpandedState(final TreePath path, final boolean state) {
    Invoke.later(() -> {
      super.setExpandedState(path, state);
      if (isExpanded(path) == state) {
        final Object node = path.getLastPathComponent();
        if (node instanceof OpenStateTreeNode) {
          final OpenStateTreeNode openState = (OpenStateTreeNode)node;
          openState.setOpen(state);
        }
        if (node instanceof BaseTreeNode) {
          final BaseTreeNode treeNode = (BaseTreeNode)node;
          if (treeNode.isAllowsChildren()) {
            for (final BaseTreeNode childNode : treeNode.getChildren()) {
              if (childNode instanceof OpenStateTreeNode) {
                final OpenStateTreeNode childState = (OpenStateTreeNode)childNode;
                if (childState.isOpen()) {
                  final TreePath childPath = childNode.getTreePath();
                  setExpandedState(childPath, true);
                }
              }
            }
          }
        }
      }
    });
  }

  public void setMenuEnabled(final boolean menuEnabled) {
    this.menuEnabled = menuEnabled;
  }

  public void setRoot(final BaseTreeNode root) {
    final DefaultTreeModel model = getModel();
    final BaseTreeNode oldRoot = getRootNode();
    if (oldRoot != null && root != oldRoot) {
      oldRoot.delete();
    }
    model.setRoot(root);
    root.setTree(this);
  }

  public void setTreeListener(final BaseTreeListener treeListener) {
    if (this.treeListener != treeListener) {
      this.treeListener.close();
    }
    this.treeListener = treeListener;
  }
}
