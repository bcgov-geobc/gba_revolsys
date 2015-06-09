package com.revolsys.swing.tree.datastore;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.TreeNode;

import com.revolsys.util.Property;

import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.gis.data.io.DataObjectStoreConnectionMapProxy;
import com.revolsys.gis.data.io.DataObjectStoreProxy;
import com.revolsys.io.FileUtil;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.file.FileTreeNode;

public class FileDataObjectStoreTreeNode extends FileTreeNode implements DataObjectStoreProxy,
  DataObjectStoreConnectionMapProxy {
  private static final MenuFactory MENU = new MenuFactory();

  static {
    MENU.addMenuItemTitleIcon("default", "Add Data Store Connection", "link_add", null,
      FileDataObjectStoreTreeNode.class, "addDataStoreConnection");
  }

  public static void addDataStoreConnection() {
    final FileDataObjectStoreTreeNode node = BaseTree.getMouseClickItem();
    final File file = node.getUserData();
    final String fileName = FileUtil.getBaseName(file);

    final ValueField panel = new ValueField();
    panel.setTitle("Add Data Store Connection");
    SwingUtil.setTitledBorder(panel, "Data Store Connection");

    SwingUtil.addLabel(panel, "File");
    final JLabel fileLabel = new JLabel(file.getAbsolutePath());
    panel.add(fileLabel);

    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);
    nameField.setText(fileName);

    SwingUtil.addLabel(panel, "Folder Connections");
    final List<DataObjectStoreConnectionRegistry> registries = DataObjectStoreConnectionManager.get()
      .getVisibleConnectionRegistries();
    final JComboBox registryField = new JComboBox(new Vector<DataObjectStoreConnectionRegistry>(
      registries));

    panel.add(registryField);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final DataObjectStoreConnectionRegistry registry = (DataObjectStoreConnectionRegistry)registryField.getSelectedItem();
      String connectionName = nameField.getText();
      if (!Property.hasValue(connectionName)) {
        connectionName = fileName;
      }
      final String baseConnectionName = connectionName;
      int i = 0;
      while (registry.getConnection(connectionName) != null) {
        connectionName = baseConnectionName + i;
        i++;
      }
      final Map<String, Object> connection = node.getDataStoreConnectionMap();
      final Map<String, Object> config = new HashMap<String, Object>();
      config.put("name", connectionName);
      config.put("connection", connection);
      registry.createConnection(config);
    }
  }

  public FileDataObjectStoreTreeNode(final TreeNode parent, final File file) {
    super(parent, file);
    setType("Data Store");
    setName(FileUtil.getFileName(file));
    setIcon(FileTreeNode.ICON_FILE_DATABASE);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final RecordStore dataStore = getDataStore();
    for (final RecordStoreSchema schema : dataStore.getSchemas()) {
      final String schemaPath = schema.getPath();

      final DataObjectStoreSchemaTreeNode schemaNode = new DataObjectStoreSchemaTreeNode(this,
        schemaPath);
      children.add(schemaNode);
    }
    return children;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getDataStore() {
    final File file = getUserData();
    return (V)DataObjectStoreConnectionManager.getDataStore(file);
  }

  @Override
  public Map<String, Object> getDataStoreConnectionMap() {
    final TreeNode parent = getParent();
    final File file = getUserData();
    final URL url = FileTreeNode.getUrl(parent, file);

    return Collections.<String, Object> singletonMap("url", url.toString());
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

}
