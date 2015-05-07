package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.OidFieldDefinition;
import com.revolsys.io.AbstractRecordWriter;

public class FileGdbWriter extends AbstractRecordWriter {
  private FileGdbRecordStoreImpl recordStore;

  private Map<String, Table> tables = new HashMap<String, Table>();

  FileGdbWriter(final FileGdbRecordStoreImpl recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      if (this.tables != null) {
        for (final String typePath : this.tables.keySet()) {
          this.recordStore.releaseTableAndWriteLock(typePath);
        }
      }
    } finally {
      this.tables = null;
      this.recordStore = null;
    }
  }

  public void closeTable(final String typePath) {
    if (this.tables != null) {
      synchronized (this.tables) {
        if (this.tables.remove(typePath) != null) {
          this.recordStore.releaseTableAndWriteLock(typePath);
        }
      }
    }
  }

  private void delete(final Record record) {
    final Object objectId = record.getValue("OBJECTID");
    if (objectId != null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final String typePath = recordDefinition.getPath();
      final Table table = getTable(typePath);
      final String whereClause = "OBJECTID=" + objectId;
      final EnumRows rows = this.recordStore.search(typePath, table, "OBJECTID", whereClause, false);
      if (rows != null) {
        try {
          final Row row = this.recordStore.nextRow(rows);
          if (row != null) {
            try {
              this.recordStore.deleteRow(typePath, table, row);
              record.setState(RecordState.Deleted);
            } finally {
              this.recordStore.closeRow(row);
              this.recordStore.addStatistic("Delete", record);
            }
          }
        } finally {
          this.recordStore.closeEnumRows(rows);
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  private Table getTable(final String typePath) {
    synchronized (this) {
      Table table = this.tables.get(typePath);
      if (table == null) {
        table = this.recordStore.getTableWithWriteLock(typePath);
        if (table != null) {
          this.tables.put(typePath, table);
        }
      }
      return table;
    }
  }

  private void insert(final Record record) {
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(sourceRecordDefinition);
    final String typePath = sourceRecordDefinition.getPath();
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String name = field.getName();
      if (field.isRequired()) {
        final Object value = record.getValue(name);
        if (value == null && !(field instanceof OidFieldDefinition)) {
          throw new IllegalArgumentException("Atribute " + typePath + "." + name + " is required");
        }
      }
    }
    final Table table = getTable(typePath);
    try {
      final Row row = this.recordStore.createRowObject(table);
      try {
        final List<Object> values = new ArrayList<Object>();
        for (final FieldDefinition field : recordDefinition.getFields()) {
          final String name = field.getName();
          final Object value = record.getValue(name);
          final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
          final Object esriValue = esriField.setInsertValue(record, row, value);
          values.add(esriValue);
        }
        this.recordStore.insertRow(table, row);
        for (final FieldDefinition field : recordDefinition.getFields()) {
          final AbstractFileGdbFieldDefinition esriFieldDefinition = (AbstractFileGdbFieldDefinition)field;
          esriFieldDefinition.setPostInsertValue(record, row);
        }
        record.setState(RecordState.Persisted);
      } finally {
        this.recordStore.closeRow(row);
        this.recordStore.addStatistic("Insert", record);
      }
    } catch (final IllegalArgumentException e) {
      throw new RuntimeException("Unable to insert row " + e.getMessage() + "\n"
        + record.toString(), e);
    } catch (final RuntimeException e) {
      if (LoggerFactory.getLogger(FileGdbWriter.class).isDebugEnabled()) {
        LoggerFactory.getLogger(FileGdbWriter.class).debug(
          "Unable to insert row \n:" + record.toString());
      }
      throw new RuntimeException("Unable to insert row", e);
    }

  }

  private void update(final Record record) {
    final Object objectId = record.getValue("OBJECTID");
    if (objectId == null) {
      insert(record);
    } else {
      final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
      final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(sourceRecordDefinition);
      final String typePath = sourceRecordDefinition.getPath();
      final Table table = getTable(typePath);
      final String whereClause = "OBJECTID=" + objectId;
      final EnumRows rows = this.recordStore.search(typePath, table, "*", whereClause, true);
      if (rows != null) {
        try {
          final Row row = this.recordStore.nextRow(rows);
          if (row != null) {
            try {
              final List<Object> esriValues = new ArrayList<Object>();
              try {
                for (final FieldDefinition field : recordDefinition.getFields()) {
                  final String name = field.getName();
                  final Object value = record.getValue(name);
                  final AbstractFileGdbFieldDefinition esriFieldDefinition = (AbstractFileGdbFieldDefinition)field;
                  try {
                    final Object esriValue = esriFieldDefinition.setUpdateValue(record, row, value);
                    esriValues.add(esriValue);
                  } catch (final RuntimeException e) {
                    LoggerFactory.getLogger(FileGdbWriter.class).error(
                      "Unable to update field " + name + "=" + value, e);
                    throw e;
                  }
                }
                this.recordStore.updateRow(typePath, table, row);
              } finally {
                this.recordStore.addStatistic("Update", record);
              }
            } catch (final IllegalArgumentException e) {
              LoggerFactory.getLogger(FileGdbWriter.class).error(
                "Unable to update row " + e.getMessage() + "\n" + record.toString(), e);
            } catch (final RuntimeException e) {
              LoggerFactory.getLogger(FileGdbWriter.class).error(
                "Unable to update row \n:" + record.toString());
              if (LoggerFactory.getLogger(FileGdbWriter.class).isDebugEnabled()) {
                LoggerFactory.getLogger(FileGdbWriter.class).debug(
                  "Unable to update row \n:" + record.toString());
              }
              throw new RuntimeException("Unable to update row", e);
            } finally {
              this.recordStore.closeRow(row);
            }
          }
        } finally {
          this.recordStore.closeEnumRows(rows);
        }
      }
    }
  }

  @Override
  public void write(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this.recordStore) {
      switch (record.getState()) {
        case New:
          insert(record);
        break;
        case Modified:
          update(record);
        break;
        case Persisted:
        // No action required
        break;
        case Deleted:
          delete(record);
        break;
        default:
          throw new IllegalStateException("State not known");
      }
    } else {
      insert(record);
    }
  }
}
