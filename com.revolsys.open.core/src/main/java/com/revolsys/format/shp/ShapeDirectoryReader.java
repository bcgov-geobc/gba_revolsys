package com.revolsys.format.shp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordIteratorReader;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.data.io.AbstractDirectoryReader;
import com.revolsys.gis.data.io.RecordDirectoryReader;
import com.revolsys.io.Reader;
import com.revolsys.spring.resource.SpringUtil;

/**
 * <p>
 * The ShapeDirectoryReader is a that can read .shp
 * data files contained in a single directory. The reader will iterate through
 * the .shp files in alpabetical order returning all features.
 * </p>
 * <p>
 * See the {@link AbstractDirectoryReader} class for examples on how to use
 * dataset readers.
 * </p>
 *
 * @author Paul Austin
 * @see AbstractDirectoryReader
 */
public class ShapeDirectoryReader extends RecordDirectoryReader {
  private final Map<String, String> fileNameTypeMap = new HashMap<String, String>();

  private Map<String, RecordDefinition> typeNameRecordDefinitionMap = new HashMap<String, RecordDefinition>();

  public ShapeDirectoryReader() {
    setFileExtensions(ShapefileConstants.FILE_EXTENSION);
  }

  /**
   * Construct a new ShapeDirectoryReader.
   *
   * @param directory The containing the .shp files.
   */
  public ShapeDirectoryReader(final File directory) {
    this();
    setDirectory(directory);
  }

  @Override
  protected Reader<Record> createReader(final Resource resource) {
    try {
      final ArrayRecordFactory factory = new ArrayRecordFactory();
      final ShapefileIterator iterator = new ShapefileIterator(resource, factory);
      final String baseName = SpringUtil.getBaseName(resource).toUpperCase();
      iterator.setTypeName(this.fileNameTypeMap.get(baseName));
      iterator.setRecordDefinition(this.typeNameRecordDefinitionMap.get(iterator.getTypeName()));
      return new RecordIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  public Map<String, String> getFileNameTypeMap() {
    return this.fileNameTypeMap;
  }

  public Map<String, RecordDefinition> getTypeNameRecordDefinitionMap() {
    return this.typeNameRecordDefinitionMap;
  }

  public void setFileNameTypeMap(final Map<String, String> fileNameTypeMap) {
    this.fileNameTypeMap.clear();
    for (final Entry<String, String> entry : fileNameTypeMap.entrySet()) {
      final String fileName = entry.getKey();
      final String typeName = entry.getValue();
      this.fileNameTypeMap.put(fileName.toUpperCase(), typeName);
    }
    setBaseFileNames(fileNameTypeMap.keySet());
  }

  public void setTypeNameRecordDefinitionMap(
    final Map<String, RecordDefinition> typeNameRecordDefinitionMap) {
    this.typeNameRecordDefinitionMap = typeNameRecordDefinitionMap;
  }

}
