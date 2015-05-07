package com.revolsys.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.gis.data.io.AbstractDataObjectIoFactory;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;
import com.vividsolutions.jts.geom.Geometry;

public class DirectoryDataObjectStore extends AbstractRecordStore {

  private boolean createMissingTables = true;

  private final Map<String, Writer<Record>> writers = new HashMap<String, Writer<Record>>();

  private File directory;

  private String fileExtension;

  private Writer<Record> writer;

  private boolean createMissingDataStore = true;

  public DirectoryDataObjectStore(final File directory,
    final String fileExtension) {
    this.directory = directory;
    this.fileExtension = fileExtension;
  }

  @Override
  public void close() {
    directory = null;
    if (writers != null) {
      for (final Writer<Record> writer : writers.values()) {
        writer.close();
      }
      writers.clear();
    }
    if (writer != null) {
      writer.close();
      writer = null;
    }
    super.close();
  }

  @Override
  public Writer<Record> createWriter() {
    return new DirectoryDataObjectStoreWriter(this);
  }

  public File getDirectory() {
    return directory;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  @Override
  public RecordDefinition getRecordDefinition(final RecordDefinition objectMetaData) {
    final RecordDefinition metaData = super.getRecordDefinition(objectMetaData);
    if (metaData == null && createMissingTables) {
      final String typePath = objectMetaData.getPath();
      final String schemaName = PathUtil.getPath(typePath);
      RecordStoreSchema schema = getSchema(schemaName);
      if (schema == null && createMissingTables) {
        schema = new RecordStoreSchema(this, schemaName);
        addSchema(schema);
      }
      final File schemaDirectory = new File(directory, schemaName);
      if (!schemaDirectory.exists()) {
        schemaDirectory.mkdirs();
      }
      final RecordDefinitionImpl newMetaData = new RecordDefinitionImpl(
        this, schema, typePath);
      for (final FieldDefinition attribute : objectMetaData.getFields()) {
        final FieldDefinition newAttribute = new FieldDefinition(attribute);
        newMetaData.addField(newAttribute);
      }
      schema.addMetaData(newMetaData);
    }
    return metaData;
  }

  @Override
  public int getRowCount(final Query query) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized Writer<Record> getWriter() {
    if (writer == null && directory != null) {
      writer = new DirectoryDataObjectStoreWriter(this);
    }
    return writer;
  }

  @PostConstruct
  @Override
  public void initialize() {
    if (!directory.exists()) {
      directory.mkdirs();
    }
    super.initialize();
  }

  @Override
  public synchronized void insert(final Record object) {
    final RecordDefinition metaData = object.getRecordDefinition();
    final String typePath = metaData.getPath();
    Writer<Record> writer = writers.get(typePath);
    if (writer == null) {
      final String schemaName = PathUtil.getPath(typePath);
      final File subDirectory = new File(getDirectory(), schemaName);
      final File file = new File(subDirectory, metaData.getTypeName() + "."
        + getFileExtension());
      final Resource resource = new FileSystemResource(file);
      writer = AbstractDataObjectIoFactory.dataObjectWriter(metaData, resource);
      if (writer instanceof ObjectWithProperties) {
        final ObjectWithProperties properties = writer;
        properties.setProperties(getProperties());
      }
      writers.put(typePath, writer);
    }
    writer.write(object);
    addStatistic("Insert", object);
  }

  public boolean isCreateMissingDataStore() {
    return createMissingDataStore;
  }

  public boolean isCreateMissingTables() {
    return createMissingTables;
  }

  protected RecordDefinition loadMetaData(final String schemaName,
    final File file) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final RecordStoreSchema schema,
    final Map<String, RecordDefinition> metaDataMap) {
    final String schemaName = schema.getPath();
    final File subDirectory = new File(directory, schemaName);
    final File[] files = subDirectory.listFiles(new ExtensionFilenameFilter(
      fileExtension));
    if (files != null) {
      for (final File file : files) {
        final RecordDefinition metaData = loadMetaData(schemaName, file);
        if (metaData != null) {
          final String typePath = metaData.getPath();
          metaDataMap.put(typePath, metaData);
        }
      }
    }
  }

  @Override
  protected void loadSchemas(final Map<String, RecordStoreSchema> schemaMap) {
    final File[] directories = directory.listFiles(new DirectoryFilenameFilter());
    if (directories != null) {
      for (final File subDirectory : directories) {
        final String directoryName = FileUtil.getFileName(subDirectory);
        addSchema(new RecordStoreSchema(this, directoryName));
      }
    }
  }

  @Override
  public Reader<Record> query(final RecordFactory dataObjectFactory,
    final String typePath, final Geometry geometry) {
    throw new UnsupportedOperationException();
  }

  public void setCreateMissingRecordStore(final boolean createMissingDataStore) {
    this.createMissingDataStore = createMissingDataStore;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setDirectory(final File directory) {
    this.directory = directory;
  }

  protected void setFileExtension(final String fileExtension) {
    this.fileExtension = fileExtension;
  }
}
