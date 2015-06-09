package com.revolsys.swing.tree.model.node;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.ExceptionUtil;

public abstract class LazyLoadTreeNode extends AbstractTreeNode {

  private static final DefaultMutableTreeNode LOADING_NODE = new DefaultMutableTreeNode(
      "Loading...");

  private static final List<TreeNode> LOADING_NODES = Collections.<TreeNode> singletonList(LOADING_NODE);

  private static Method setChildrenMethod;

  static {
    LOADING_NODE.setAllowsChildren(false);
    try {
      final Class<LazyLoadTreeNode> clazz = LazyLoadTreeNode.class;
      setChildrenMethod = clazz.getDeclaredMethod("setChildren", Integer.TYPE, List.class);
      setChildrenMethod.setAccessible(true);
    } catch (final Throwable e) {
      ExceptionUtil.log(LazyLoadTreeNode.class, e);
    }
  }

  private final AtomicInteger updateIndicies = new AtomicInteger();

  private List<TreeNode> children = LOADING_NODES;

  private final Object sync = new Object();

  public LazyLoadTreeNode(final TreeNode parent, final Object userObject) {
    super(parent, userObject);

  }

  protected void addNode(final int index, final TreeNode node) {
    final List<TreeNode> children = this.children;
    if (children != LOADING_NODES) {
      children.add(index, node);
    }
  }

  @Override
  protected void doDelete() {
    this.children = LOADING_NODES;
    super.doDelete();
  }

  protected List<TreeNode> doLoadChildren() {
    return new ArrayList<TreeNode>();
  }

  @Override
  public List<TreeNode> getChildren() {
    return this.children;
  }

  protected int getUpdateIndex() {
    synchronized (this.updateIndicies) {
      return this.updateIndicies.incrementAndGet();
    }
  }

  public void loadChildren() {
    if (SwingUtilities.isEventDispatchThread()) {
      if (this.children == LOADING_NODES) {
        Invoke.background("Load tree node " + this.getName(), this, "loadChildren");
      }
    } else {
      synchronized (this.sync) {
        if (this.children == LOADING_NODES) {
          final int updateIndex = getUpdateIndex();
          List<TreeNode> children = doLoadChildren();
          if (children == null) {
            children = Collections.emptyList();
          }
          Invoke.later(this, setChildrenMethod, updateIndex, children);
        }
      }
    }
  }

  @Override
  public void nodeCollapsed(final AbstractTreeNode treeNode) {
    super.nodeCollapsed(treeNode);
    if (treeNode != this) {
      final int updateIndex = getUpdateIndex();
      setChildren(updateIndex, LOADING_NODES);
    }
  }

  public void refresh() {
    final List<TreeNode> oldNodes = getChildren();

    final List<TreeNode> newNodes = doLoadChildren();
    for (int i = 0; i < oldNodes.size();) {
      final TreeNode oldNode = oldNodes.get(i);
      if (newNodes.contains(oldNode)) {
        i++;
      } else {
        oldNodes.remove(i);
        nodeRemoved(i, oldNode);
      }
    }
    for (int i = 0; i < newNodes.size();) {
      final TreeNode oldNode;
      if (i < oldNodes.size()) {
        oldNode = oldNodes.get(i);
      } else {
        oldNode = null;
      }
      final TreeNode newNode = newNodes.get(i);
      if (!newNode.equals(oldNode)) {
        oldNodes.add(i, newNode);
        nodesInserted(i);
      }
      i++;
    }
  }

  public void removeNode(final int index) {
    final List<TreeNode> children = this.children;
    if (children != LOADING_NODES) {
      if (index > 0 && index < children.size()) {
        final TreeNode node = children.remove(index);
        nodeRemoved(index, node);
      }
    }
  }

  public void removeNode(final TreeNode node) {
    final List<TreeNode> children = this.children;
    if (children != LOADING_NODES) {
      final int index = children.indexOf(node);
      if (index != -1) {
        nodeRemoved(index, node);
      }
    }
  }

  protected void setChildren(final int updateIndex, final List<TreeNode> children) {
    synchronized (this.updateIndicies) {
      if (updateIndex == this.updateIndicies.get()) {
        nodeChanged();
        this.children = Collections.emptyList();
        nodeRemoved(0, LOADING_NODE);
        this.children = children;
        final int[] newIndicies = new int[children.size()];
        for (int i = 0; i < newIndicies.length; i++) {
          newIndicies[i] = i;
        }
        nodesInserted(newIndicies);
      } else if (children != LOADING_NODES) {
        delete(children);
      }
    }
  }
}
