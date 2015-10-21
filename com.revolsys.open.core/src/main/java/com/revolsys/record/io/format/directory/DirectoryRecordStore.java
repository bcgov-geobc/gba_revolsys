package com.revolsys.record.io.format.directory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Path;
import com.revolsys.io.Writer;
import com.revolsys.io.filter.ExtensionFilenameFilter;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;

public class DirectoryRecordStore extends AbstractRecordStore {
  private boolean createMissingRecordStore = true;

  private boolean createMissingTables = true;

  private File directory;

  private List<String> fileExtensions;

  private final Map<RecordDefinition, Resource> resourcesByRecordDefinition = new HashMap<>();

  private final Map<Resource, String> typePathByResource = new HashMap<>();

  private Writer<Record> writer;

  private final Map<String, Writer<Record>> writers = new HashMap<>();

  public DirectoryRecordStore(final File directory, final Collection<String> fileExtensions) {
    this.directory = directory;
    this.fileExtensions = new ArrayList<>(fileExtensions);
  }

  public DirectoryRecordStore(final File directory, final String... fileExtensions) {
    this(directory, Arrays.asList(fileExtensions));
  }

  @Override
  public void close() {
    this.directory = null;
    if (this.writers != null) {
      for (final Writer<Record> writer : this.writers.values()) {
        if (writer != null) {
          writer.close();
        }
      }
      this.writers.clear();
    }
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
    super.close();
  }

  @Override
  public AbstractIterator<Record> createIterator(final Query query,
    final Map<String, Object> properties) {
    final String path = query.getTypeName();
    final RecordReader reader = query(path);
    reader.setProperties(properties);
    return new RecordReaderQueryIterator(reader, query);
  }

  @Override
  public RecordWriter createWriter() {
    return new DirectoryRecordStoreWriter(this);
  }

  @Override
  public void delete(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this) {
      throw new UnsupportedOperationException("Deleting records not supported");
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  public String getFileExtension() {
    return getFileExtensions().get(0);
  }

  public List<String> getFileExtensions() {
    return this.fileExtensions;
  }

  @Override
  public RecordDefinition getRecordDefinition(final RecordDefinition recordDefinition) {
    final RecordDefinition storeRecordDefinition = super.getRecordDefinition(recordDefinition);
    if (storeRecordDefinition == null && this.createMissingTables) {
      final String typePath = recordDefinition.getPath();
      final String schemaPath = Path.getPath(typePath);
      RecordStoreSchema schema = getSchema(schemaPath);
      if (schema == null && this.createMissingTables) {
        final RecordStoreSchema rootSchema = getRootSchema();
        schema = rootSchema.createSchema(schemaPath);
      }
      final File schemaDirectory = new File(this.directory, schemaPath);
      if (!schemaDirectory.exists()) {
        schemaDirectory.mkdirs();
      }
      final RecordDefinitionImpl newRecordDefinition = new RecordDefinitionImpl(schema, typePath);
      for (final FieldDefinition field : recordDefinition.getFields()) {
        final FieldDefinition newField = new FieldDefinition(field);
        newRecordDefinition.addField(newField);
      }
      schema.addElement(newRecordDefinition);
      return newRecordDefinition;
    }
    return storeRecordDefinition;
  }

  protected Resource getResource(final String path) {
    final RecordDefinition recordDefinition = getRecordDefinition(path);
    return getResource(path, recordDefinition);
  }

  protected Resource getResource(final String path, final RecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Table does not exist " + path);
    }
    final Resource resource = this.resourcesByRecordDefinition.get(recordDefinition);
    if (resource == null) {
      throw new IllegalArgumentException("File does not exist for " + path);
    }
    return resource;
  }

  @Override
  public int getRowCount(final Query query) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized Writer<Record> getWriter() {
    if (this.writer == null && this.directory != null) {
      this.writer = new DirectoryRecordStoreWriter(this);
    }
    return this.writer;
  }

  @PostConstruct
  @Override
  public void initialize() {
    if (!this.directory.exists()) {
      this.directory.mkdirs();
    }
    super.initialize();
  }

  @Override
  public synchronized void insert(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    Writer<Record> writer = this.writers.get(typePath);
    if (writer == null) {
      final String schemaName = Path.getPath(typePath);
      final File subDirectory = FileUtil.getDirectory(getDirectory(), schemaName);
      final String fileExtension = getFileExtension();
      final File file = new File(subDirectory, recordDefinition.getName() + "." + fileExtension);
      final Resource resource = new FileSystemResource(file);
      writer = RecordWriter.create(recordDefinition, resource);
      if (writer == null) {
        throw new RuntimeException("Cannot create writer for: " + typePath);
      } else if (writer instanceof ObjectWithProperties) {
        final ObjectWithProperties properties = writer;
        properties.setProperties(getProperties());
      }
      this.writers.put(typePath, writer);
    }
    writer.write(record);
    addStatistic("Insert", record);
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  public boolean isCreateMissingTables() {
    return this.createMissingTables;
  }

  protected RecordDefinition loadRecordDefinition(final RecordStoreSchema schema,
    final String schemaName, final Resource resource) {
    try (
      RecordReader recordReader = RecordReader.create(resource)) {
      final String typePath = Path.toPath(schemaName, resource.getBaseName());
      recordReader.setProperty("schema", schema);
      recordReader.setProperty("typePath", typePath);
      final RecordDefinition recordDefinition = recordReader.getRecordDefinition();
      if (recordDefinition != null) {
        this.resourcesByRecordDefinition.put(recordDefinition, resource);
        this.typePathByResource.put(resource, typePath);
      }
      return recordDefinition;
    }
  }

  @Override
  public RecordReader query(final String path) {
    final RecordDefinition recordDefinition = getRecordDefinition(path);
    final Resource resource = getResource(path, recordDefinition);
    final RecordReader reader = RecordReader.create(resource);
    if (reader == null) {
      throw new IllegalArgumentException("Cannot find reader for: " + path);
    } else {
      final String typePath = this.typePathByResource.get(resource);
      reader.setProperty("schema", recordDefinition.getSchema());
      reader.setProperty("typePath", typePath);
      return reader;
    }
  }

  @Override
  protected Map<String, RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final Map<String, RecordStoreSchemaElement> elements = new TreeMap<>();
    final String schemaPath = schema.getPath();
    final File subDirectory;
    if (schemaPath.equals("/")) {
      subDirectory = this.directory;
    } else {
      subDirectory = new File(this.directory, schemaPath);
    }
    final FileFilter filter = new ExtensionFilenameFilter(this.fileExtensions);
    final File[] files = subDirectory.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (filter.accept(file)) {
          final FileSystemResource resource = new FileSystemResource(file);
          final RecordDefinition recordDefinition = loadRecordDefinition(schema, schemaPath,
            resource);
          if (recordDefinition != null) {
            final String path = recordDefinition.getPath();
            elements.put(path, recordDefinition);
          }
        } else if (file.isDirectory()) {
          final String name = file.getName();
          final String childSchemaPath = Path.getChildName(schemaPath, name);
          RecordStoreSchema childSchema = schema.getSchema(childSchemaPath);
          if (childSchema == null) {
            childSchema = new RecordStoreSchema(schema, childSchemaPath);
          } else {
            if (!childSchema.isInitialized()) {
              childSchema.refresh();
            }
          }
          elements.put(childSchemaPath, childSchema);
        }
      }
    }
    return elements;
  }

  public void setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setDirectory(final File directory) {
    this.directory = directory;
  }

  protected void setFileExtensions(final List<String> fileExtensions) {
    this.fileExtensions = fileExtensions;
  }

  protected void superDelete(final Record record) {
    super.delete(record);
  }

  protected void superUpdate(final Record record) {
    super.update(record);
  }

  @Override
  public String toString() {
    final String fileExtension = getFileExtension();
    return fileExtension + " " + this.directory;
  }

  @Override
  public void update(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this) {
      switch (record.getState()) {
        case Deleted:
        break;
        case Persisted:
        break;
        case Modified:
          throw new UnsupportedOperationException();
        default:
          insert(record);
        break;
      }
    } else {
      insert(record);
    }
  }
}
