package com.revolsys.record.io;

import java.io.File;
import java.util.Map;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public interface RecordWriter extends Writer<Record> {
  static boolean isWritable(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (isWritable(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  static boolean isWritable(final String fileNameExtension) {
    return IoFactory.isAvailable(RecordWriterFactory.class, fileNameExtension);
  }

  static RecordWriter newRecordWriter(final Record record, final Object target) {
    if (record != null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      return newRecordWriter(recordDefinition, target);
    }
    return null;
  }

  static RecordWriter newRecordWriter(final RecordDefinition recordDefinition,
    final Object target) {
    final Resource resource = Resource.getResource(target);
    final RecordWriterFactory writerFactory = IoFactory.factory(RecordWriterFactory.class,
      resource);
    if (writerFactory == null || recordDefinition == null) {
      return null;
    } else {
      final RecordWriter writer = writerFactory.newRecordWriter(recordDefinition, resource);
      return writer;
    }
  }

  default RecordDefinition getRecordDefinition() {
    return null;
  }

  boolean isIndent();

  default boolean isValueWritable(final Object value) {
    return Property.hasValue(value) || isWriteNulls() || value instanceof Geometry;
  }

  boolean isWriteCodeValues();

  boolean isWriteNulls();

  default Record newRecord() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition);
  }

  default Record newRecord(final Iterable<? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition, values);
  }

  default Record newRecord(final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition, values);
  }

  default Record newRecord(final Object... values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition, values);
  }

  void setIndent(final boolean indent);

  void setWriteCodeValues(boolean writeCodeValues);

  void setWriteNulls(boolean writeNulls);

  default void write(final Iterable<? extends Object> values) {
    final Record record = newRecord(values);
    write(record);
  }

  default void write(final Object... values) {
    final Record record = newRecord(values);
    write(record);
  }
}
