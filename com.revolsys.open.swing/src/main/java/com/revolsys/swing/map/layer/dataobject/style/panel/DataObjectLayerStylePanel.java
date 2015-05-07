package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.TreePath;

import com.revolsys.collection.PropertyChangeArrayList;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractRecordLayer;
import com.revolsys.swing.map.tree.BaseLayerRendererTreeNodeModel;
import com.revolsys.swing.map.tree.MultipleLayerRendererTreeNodeModel;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.ObjectTreePanel;
import com.revolsys.swing.tree.model.node.ListObjectTreeNodeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;
import com.revolsys.util.Property;

public class DataObjectLayerStylePanel extends ValueField implements
  MouseListener, PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private final JScrollPane editStyleContainer = new JScrollPane();

  private final ObjectTree tree;

  private final AbstractRecordLayer layer;

  private final LayerRenderer<? extends Layer> renderer;

  private final List<LayerRenderer<? extends Layer>> renderers = new PropertyChangeArrayList<LayerRenderer<? extends Layer>>();

  public DataObjectLayerStylePanel(final AbstractRecordLayer layer) {
    this.layer = layer;
    setLayout(new BorderLayout());
    final JLabel instructions = new JLabel(
      "<html><p style=\"padding: 2px 3px 2px\">Click on the style from the left to show the edit panel on the right for that style.</p></html>");
    add(instructions, BorderLayout.NORTH);

    final ListObjectTreeNodeModel listModel = new ListObjectTreeNodeModel(
      new MultipleLayerRendererTreeNodeModel(),
      new BaseLayerRendererTreeNodeModel());

    this.renderer = layer.getRenderer().clone();
    Property.removeAllListeners(this.renderer);
    this.renderer.setEditing(true);
    renderers.add(renderer);
    Property.addListener(renderer, this);

    final ObjectTreePanel styleTree = new ObjectTreePanel(renderers, listModel);
    tree = styleTree.getTree();
    tree.setRootVisible(false);
    tree.setExpandsSelectedPaths(true);
    final TreePath rendererPath = ObjectTree.createTreePath(renderers, renderer);
    tree.setSelectionPath(rendererPath);
    tree.expandPath(rendererPath);
    tree.addMouseListener(this);
    setEditStylePanel(renderer);
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      styleTree, editStyleContainer);

    splitPane.setDividerLocation(200);
    setPreferredSize(new Dimension(810, 600));
    add(splitPane, BorderLayout.CENTER);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (e.getClickCount() == 1 && SwingUtil.isLeftButtonAndNoModifiers(e)) {
      final int x = e.getX();
      final int y = e.getY();
      final TreePath path = tree.getPathForLocation(x, y);
      if (path != null) {
        final ObjectTreeNodeModel<Object, Object> nodeModel = tree.getModel()
          .getNodeModel(path);
        if (nodeModel != null) {
          final Object node = path.getLastPathComponent();
          if (node instanceof LayerRenderer<?>) {
            final LayerRenderer<?> renderer = (LayerRenderer<?>)node;
            setEditStylePanel(renderer);
          }
        }
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  @SuppressWarnings("unchecked")
  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if ("replaceRenderer".equals(event.getPropertyName())) {
      final LayerRenderer<? extends Layer> oldRenderer = (LayerRenderer<? extends Layer>)event.getOldValue();
      Property.removeListener(oldRenderer, this);

      final LayerRenderer<? extends Layer> newRenderer = (LayerRenderer<? extends Layer>)event.getNewValue();
      Property.addListener(newRenderer, this);
      renderers.remove(0);
      renderers.add(newRenderer);
      tree.setVisible(newRenderer, true);

      setSelectedRenderer(newRenderer);
    }
  }

  @Override
  public void save() {
    super.save();
    this.layer.setRenderer(renderer);
  }

  public void setEditStylePanel(final LayerRenderer<? extends Layer> renderer) {
    final Component view = editStyleContainer.getViewport().getView();
    editStyleContainer.setViewportView(null);
    if (view instanceof ValueField) {
      final ValueField valueField = (ValueField)view;
      valueField.save();
    }
    final ValueField stylePanel = renderer.createStylePanel();
    editStyleContainer.setViewportView(stylePanel);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public void setSelectedRenderer(final LayerRenderer<?> renderer) {
    final List<String> pathNames = renderer.getPathNames();
    final LayerRenderer<?> selectedRenderer = this.renderer.getRenderer(pathNames);
    if (selectedRenderer != null) {
      final List path = selectedRenderer.getPathRenderers();
      if (!path.isEmpty()) {
        path.add(0, renderers);
        final TreePath treePath = ObjectTree.createTreePath(path);
        tree.setSelectionPath(treePath);
        tree.expandPath(treePath);
        setEditStylePanel(selectedRenderer);
      }
    }
  }
}
