package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;

public class FileGdbQueryIterator extends AbstractIterator<Record> {

  private BoundingBox boundingBox;

  private final String catalogPath;

  private int count;

  private String fields;

  private int limit = Integer.MAX_VALUE;

  private int offset;

  private RecordDefinition recordDefinition;

  private RecordFactory recordFactory;

  private FileGdbRecordStore recordStore;

  private EnumRows rows;

  private String sql;

  private Statistics statistics;

  private Table table;

  private boolean closed = false;

  FileGdbQueryIterator(final FileGdbRecordStore recordStore, final String catalogPath) {
    this(recordStore, catalogPath, "*", "", null, 0, -1);
  }

  FileGdbQueryIterator(final FileGdbRecordStore recordStore, final String catalogPath,
    final String whereClause) {
    this(recordStore, catalogPath, "*", whereClause, null, 0, -1);
  }

  FileGdbQueryIterator(final FileGdbRecordStore recordStore, final String catalogPath,
    final String whereClause, final BoundingBox boundingBox, final Query query, final int offset,
    final int limit) {
    this(recordStore, catalogPath, "*", whereClause, boundingBox, offset, limit);
    final RecordFactory factory = query.getProperty("recordFactory");
    if (factory != null) {
      this.recordFactory = factory;
    }
  }

  FileGdbQueryIterator(final FileGdbRecordStore recordStore, final String catalogPath,
    final String fields, final String sql, final BoundingBox boundingBox, final int offset,
    final int limit) {
    this.catalogPath = catalogPath;
    this.sql = sql;
    this.recordDefinition = recordStore.getRecordDefinition(catalogPath);
    if (this.recordDefinition == null) {
      this.closed = true;
    } else {
      this.recordStore = recordStore;
      this.table = recordStore.getTable(catalogPath);
      if ("*".equals(fields)) {
        this.fields = CollectionUtil.toString(this.recordDefinition.getFieldNames());
      } else {
        this.fields = fields;
      }
      setBoundingBox(boundingBox);
      this.recordFactory = recordStore.getRecordFactory();
      this.offset = offset;
      if (limit >= 0) {
        this.limit = limit;
      }
    }
  }

  @Override
  protected void doClose() {
    boolean close = true;
    if (this.closed || this.recordStore == null) {
      close = false;
    } else {
      this.closed = true;
    }
    if (close) {
      synchronized (this) {
        if (this.recordDefinition != null) {
          this.recordDefinition = null;
          try {
            try {
              this.recordStore.closeEnumRows(this.rows);
            } finally {
              this.recordStore.releaseTable(this.catalogPath);
            }
          } catch (final Throwable e) {
            ExceptionUtil.log(getClass(), "Error closing query: " + this.catalogPath, e);
          } finally {
            this.boundingBox = null;
            this.recordStore = null;
            this.fields = null;
            this.rows = null;
            this.sql = null;
            this.table = null;
          }
        }
      }
    }
  }

  @Override
  protected synchronized void doInit() {
    if (!this.closed) {
      synchronized (this.recordStore.getApiSync()) {
        if (this.boundingBox == null) {
          if (this.sql.startsWith("SELECT")) {
            this.rows = this.recordStore.query(this.sql, true);
          } else {
            this.rows = this.recordStore.search(this.catalogPath, this.table, this.fields, this.sql,
              true);
          }
        } else {
          BoundingBox boundingBox = this.boundingBox;
          if (boundingBox.getWidth() == 0) {
            boundingBox = boundingBox.expand(1, 0);
          }
          if (boundingBox.getHeight() == 0) {
            boundingBox = boundingBox.expand(0, 1);
          }
          final com.revolsys.gis.esri.gdb.file.capi.swig.Envelope envelope = GeometryConverter
            .toEsri(boundingBox);
          String sql = this.sql;
          if ("1 = 1".equals(sql)) {
            sql = "";
          }
          this.rows = this.recordStore.search(this.catalogPath, this.table, this.fields, sql,
            envelope, true);
        }
      }
    }
  }

  @Override
  protected synchronized Record getNext() throws NoSuchElementException {
    final FileGdbRecordStore recordStore = this.recordStore;
    final EnumRows rows = this.rows;
    if (rows == null || this.closed) {
      throw new NoSuchElementException();
    } else {
      Row row = null;
      while (this.offset > 0 && this.count < this.offset) {
        row = recordStore.nextRow(rows);
        if (row == null) {
          throw new NoSuchElementException();
        } else {
          recordStore.closeRow(row);
        }
        this.count++;
        if (this.closed) {
          throw new NoSuchElementException();
        }
      }
      if (this.count - this.offset >= this.limit) {
        throw new NoSuchElementException();
      }
      row = recordStore.nextRow(rows);
      this.count++;
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        try {
          final Record record = this.recordFactory.createRecord(this.recordDefinition);
          if (this.statistics == null) {
            recordStore.addStatistic("query", record);
          } else {
            this.statistics.add(record);
          }
          record.setState(RecordState.Initalizing);
          for (final FieldDefinition field : this.recordDefinition.getFields()) {
            final String name = field.getName();
            final AbstractFileGdbFieldDefinition esriFieldDefinition = (AbstractFileGdbFieldDefinition)field;
            final Object value;
            synchronized (recordStore) {
              value = esriFieldDefinition.getValue(row);
            }
            record.setValue(name, value);
            if (this.closed) {
              throw new NoSuchElementException();
            }
          }
          record.setState(RecordState.Persisted);
          if (this.closed) {
            throw new NoSuchElementException();
          }
          return record;
        } finally {
          recordStore.closeRow(row);
        }
      }
    }
  }

  protected RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      hasNext();
    }
    return this.recordDefinition;
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = this.recordDefinition;
    if (recordDefinition != null) {
      this.boundingBox = boundingBox;
      if (boundingBox != null) {
        final FieldDefinition geometryField = recordDefinition.getGeometryField();
        if (geometryField != null) {
          final GeometryFactory geometryFactory = geometryField
            .getProperty(FieldProperties.GEOMETRY_FACTORY);
          if (geometryFactory != null) {
            this.boundingBox = boundingBox.convert(geometryFactory);
          }
        }
      }
    }
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public String toString() {
    return this.catalogPath + " " + this.sql;
  }
}
