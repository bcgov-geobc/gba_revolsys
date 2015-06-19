package com.revolsys.swing.tree.node.file;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.io.RecordIo;
import com.revolsys.data.record.io.RecordStoreFactoryRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.raster.AbstractGeoreferencedImageFactory;
import com.revolsys.raster.GeoreferencedImageFactory;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodePropertyEnableCheck;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.record.FileRecordStoreTreeNode;
import com.revolsys.util.Property;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;

public class FileTreeNode extends LazyLoadTreeNode implements UrlProxy {

  private static final JFileChooser CHOOSER = new JFileChooser();

  private static final UIDefaults DEFAULTS = UIManager.getDefaults();

  public static final Icon ICON_FILE = CHOOSER.getIcon(FileUtil.createTempFile("xxxx", "6z4gsdj"));

  public static final Icon ICON_FILE_DATABASE = Icons.getIconWithBadge(ICON_FILE, "database");

  public static final Icon ICON_FILE_IMAGE = Icons.getIconWithBadge(ICON_FILE, "picture");

  public static final Icon ICON_FILE_TABLE = Icons.getIconWithBadge(ICON_FILE, "table");

  public static final Icon ICON_FILE_VECTOR = Icons.getIconWithBadge(ICON_FILE, "table");

  public static final Icon ICON_FOLDER = DEFAULTS.getIcon("Tree.closedIcon");

  public static final Icon ICON_FOLDER_DRIVE = Icons.getIconWithBadge(ICON_FOLDER, "drive");

  public static final Icon ICON_FOLDER_LINK = Icons.getIconWithBadge(ICON_FOLDER, "link");

  public static final Icon ICON_FOLDER_MISSING = Icons.getIcon("folder_error");

  private static final MenuFactory MENU = new MenuFactory("File");

  static {
    final EnableCheck isDirectory = new TreeNodePropertyEnableCheck("directory");
    final EnableCheck isFileLayer = new TreeNodePropertyEnableCheck("fileLayer");

    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh", "arrow_refresh",
      NODE_EXISTS, "refresh");
    MENU.addMenuItem("default", refresh);

    MENU.addMenuItem("default",
      TreeNodeRunnable.createAction("Add Layer", "map_add", isFileLayer, "addLayer"));

    MENU.addMenuItem("default", TreeNodeRunnable.createAction("Add Folder Connection", "link_add",
      new AndEnableCheck(isDirectory, NODE_EXISTS), "addFolderConnection"));
  }

  public static List<BaseTreeNode> getFileNodes(final BaseTreeNode parent, final File file) {
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      return getFileNodes(parent, files);
    } else {
      return Collections.emptyList();
    }
  }

  public static List<BaseTreeNode> getFileNodes(final BaseTreeNode parent, final File[] files) {
    final List<BaseTreeNode> children = new ArrayList<>();
    if (files != null) {
      for (final File childFile : files) {
        if (!childFile.isHidden()) {
          if (FileTreeNode.isRecordStore(childFile)) {
            final FileRecordStoreTreeNode recordStoreNode = new FileRecordStoreTreeNode(childFile);
            children.add(recordStoreNode);
          } else {
            final FileTreeNode child = new FileTreeNode(childFile);
            children.add(child);
          }
        }
      }
    }
    return children;
  }

  public static Icon getIcon(final File file) {
    if (file == null) {
      return ICON_FOLDER_MISSING;
    } else {
      final Icon icon = CHOOSER.getIcon(file);
      if (isImage(file)) {
        return ICON_FILE_IMAGE;
      } else if (RecordIo.canReadRecords(file)) {
        return ICON_FILE_TABLE;
      }
      return icon;
    }
  }

  public static List<BaseTreeNode> getPathNodes(final BaseTreeNode parent,
    final Iterable<Path> paths) {
    final List<BaseTreeNode> children = new ArrayList<>();
    if (paths != null) {
      for (final Path childPath : paths) {
        if (!Paths.isHidden(childPath)) {
          final File file = childPath.toFile();
          if (FileTreeNode.isRecordStore(childPath)) {
            final FileRecordStoreTreeNode recordStoreNode = new FileRecordStoreTreeNode(file);
            children.add(recordStoreNode);
          } else {
            final FileTreeNode child = new FileTreeNode(file);
            children.add(child);
          }
        }
      }
    }
    return children;
  }

  public static URL getUrl(final BaseTreeNode parent, final File file) {
    if (parent instanceof UrlProxy) {
      final UrlProxy parentProxy = (UrlProxy)parent;
      String childPath = FileUtil.getFileName(file);

      if (file.isDirectory()) {
        childPath += "/";
      }
      return UrlUtil.getUrl(parentProxy, childPath);
    } else {
      return FileUtil.toUrl(file);
    }
  }

  public static boolean isAllowsChildren(final File file) {
    if (file == null) {
      return true;
    } else if (!file.exists()) {
      return false;
    } else if (file.isDirectory()) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isImage(final File file) {
    final String fileNameExtension = FileUtil.getFileNameExtension(file);
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(GeoreferencedImageFactory.class,
      fileNameExtension);
  }

  public static boolean isRecordStore(final File file) {
    final Set<String> fileExtensions = RecordStoreFactoryRegistry.getFileExtensions();
    final String extension = FileUtil.getFileNameExtension(file).toLowerCase();
    return fileExtensions.contains(extension);
  }

  public static boolean isRecordStore(final Path path) {
    final Set<String> fileExtensions = RecordStoreFactoryRegistry.getFileExtensions();
    final String extension = Paths.getFileNameExtension(path).toLowerCase();
    return fileExtensions.contains(extension);
  }

  public FileTreeNode(final File file) {
    super(file);
    final String fileName = FileUtil.getFileName(file);
    setName(fileName);
    final Icon icon = getIcon(file);
    setIcon(icon);
  }

  public void addFolderConnection() {
    if (isDirectory()) {
      final File directory = getUserData();
      final String fileName = getName();

      final ValueField panel = new ValueField();
      panel.setTitle("Add Folder Connection");
      SwingUtil.setTitledBorder(panel, "Folder Connection");

      SwingUtil.addLabel(panel, "Folder");
      final JLabel fileLabel = new JLabel(directory.getAbsolutePath());
      panel.add(fileLabel);

      SwingUtil.addLabel(panel, "Name");
      final TextField nameField = new TextField(20);
      panel.add(nameField);
      nameField.setText(fileName);

      SwingUtil.addLabel(panel, "Folder Connections");
      final List<FolderConnectionRegistry> registries = new ArrayList<>();
      for (final FolderConnectionRegistry registry : FolderConnectionManager.get()
        .getVisibleConnectionRegistries()) {
        if (!registry.isReadOnly()) {
          registries.add(registry);
        }
      }
      final JComboBox registryField = new JComboBox(
        new Vector<FolderConnectionRegistry>(registries));

      panel.add(registryField);

      GroupLayoutUtil.makeColumns(panel, 2, true);
      panel.showDialog();
      if (panel.isSaved()) {
        final FolderConnectionRegistry registry = (FolderConnectionRegistry)registryField.getSelectedItem();
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
        registry.addConnection(connectionName, directory);
      }
    }
  }

  public void addLayer() {
    final URL url = getUrl();
    final Project project = Project.get();
    project.openFile(url);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final File file = getFile();
    return getFileNodes(this, file);
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof FileTreeNode) {
      final FileTreeNode fileNode = (FileTreeNode)other;
      final File file = getFile();
      final File otherFile = fileNode.getFile();
      final boolean equal = EqualsRegistry.equal(file, otherFile);
      return equal;
    }
    return false;
  }

  public File getFile() {
    return getUserData();
  }

  @Override
  public Icon getIcon() {
    Icon icon = super.getIcon();
    if (icon == null) {
      if (isExists()) {
        final File file = getUserData();
        icon = getIcon(file);
        setIcon(icon);
      }
    }
    return icon;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  public String getType() {
    final File file = getUserData();
    if (file.isDirectory()) {
      return "Folder";
    } else if (file.exists()) {
      final String extension = FileUtil.getFileNameExtension(file);
      if (Property.hasValue(extension)) {
        final IoFactory factory = IoFactoryRegistry.getInstance().getFactoryByFileExtension(
          IoFactory.class, extension);
        if (factory != null) {
          return factory.getName();
        }
      }
      return "File";
    } else {
      return "Missing File/Folder";
    }
  }

  @Override
  public URL getUrl() {
    final File file = getFile();
    final BaseTreeNode parent = getParent();
    return getUrl(parent, file);
  }

  @Override
  public int hashCode() {
    final File file = getUserData();
    if (file == null) {
      return 0;
    } else {
      return file.hashCode();
    }
  }

  @Override
  public boolean isAllowsChildren() {
    final File file = getUserData();
    return isAllowsChildren(file);
  }

  public boolean isDirectory() {
    final File file = getFile();
    return file.isDirectory();
  }

  @Override
  public boolean isExists() {
    final File file = getFile();
    if (file == null) {
      return false;
    } else {
      return file.exists() && super.isExists();
    }
  }

  public boolean isFileLayer() {
    final File file = getFile();
    final String fileName = FileUtil.getFileName(file);
    if (AbstractGeoreferencedImageFactory.hasGeoreferencedImageFactory(fileName)) {
      return true;
    } else if (RecordIo.hasRecordReaderFactory(fileName)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void refresh() {
    setIcon(null);
    super.refresh();
  }
}
