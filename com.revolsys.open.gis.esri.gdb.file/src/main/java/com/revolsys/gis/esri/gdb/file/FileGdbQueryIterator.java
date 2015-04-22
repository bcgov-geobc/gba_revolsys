package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.FieldProperties;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.util.CollectionUtil;

public class FileGdbQueryIterator extends AbstractIterator<Record> {

  private RecordFactory recordFactory;

  private Table table;

  private String fields;

  private String sql;

  private BoundingBox boundingBox;

  private RecordDefinition recordDefinition;

  private CapiFileGdbRecordStore recordStore;

  private EnumRows rows;

  private final String typePath;

  private final int offset;

  private final int limit;

  private int count;

  private Statistics statistics;

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath) {
    this(recordStore, typePath, "*", "", null, 0, -1);
  }

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath, final String whereClause) {
    this(recordStore, typePath, "*", whereClause, null, 0, -1);
  }

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath, final String whereClause,
    final BoundingBox boundingBox, final Query query, final int offset,
    final int limit) {
    this(recordStore, typePath, "*", whereClause, boundingBox, offset, limit);
    final RecordFactory factory = query.getProperty("recordFactory");
    if (factory != null) {
      this.recordFactory = factory;
    }
  }

  FileGdbQueryIterator(final CapiFileGdbRecordStore recordStore,
    final String typePath, final String fields, final String sql,
    final BoundingBox boundingBox, final int offset, final int limit) {
    this.recordStore = recordStore;
    this.typePath = typePath;
    this.recordDefinition = recordStore.getRecordDefinition(typePath);
    this.table = recordStore.getTable(typePath);
    if (this.recordDefinition != null && "*".equals(fields)) {
      this.fields = CollectionUtil.toString(this.recordDefinition.getFieldNames());
    } else {
      this.fields = fields;
    }
    this.sql = sql;
    setBoundingBox(boundingBox);
    this.recordFactory = recordStore.getRecordFactory();
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  protected void doClose() {
    if (this.recordStore != null) {
      try {
        this.recordStore.closeEnumRows(this.rows);
        this.recordStore.closeTable(this.typePath);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Error closing query: " + this.typePath);
      } finally {
        this.boundingBox = null;
        this.recordStore = null;
        this.fields = null;
        this.recordDefinition = null;
        this.rows = null;
        this.sql = null;
        this.table = null;
      }
    }
  }

  @Override
  protected void doInit() {
    if (this.recordDefinition != null) {
      synchronized (this.recordStore) {
        if (this.boundingBox == null) {
          if (this.sql.startsWith("SELECT")) {
            this.rows = this.recordStore.query(this.sql, true);
          } else {
            this.rows = this.recordStore.search(this.table, this.fields,
              this.sql, true);
          }
        } else {
          BoundingBox boundingBox = this.boundingBox;
          if (boundingBox.getWidth() == 0) {
            boundingBox = boundingBox.expand(1, 0);
          }
          if (boundingBox.getHeight() == 0) {
            boundingBox = boundingBox.expand(0, 1);
          }
          final com.revolsys.gis.esri.gdb.file.capi.swig.Envelope envelope = GeometryConverter.toEsri(boundingBox);
          String sql = this.sql;
          if ("1 = 1".equals(sql)) {
            sql = "";
          }
          this.rows = this.recordStore.search(this.table, this.fields, sql,
            envelope, true);
        }
      }
    }
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.rows == null || this.recordDefinition == null) {
      throw new NoSuchElementException();
    } else {
      Row row = null;
      while (this.offset > 0 && this.count < this.offset) {
        row = this.recordStore.nextRow(this.rows);
        if (row == null) {
          throw new NoSuchElementException();
        } else {
          this.recordStore.closeRow(row);
        }
        this.count++;
      }
      if (this.limit > -1 && this.count >= this.offset + this.limit) {
        throw new NoSuchElementException();
      }
      row = this.recordStore.nextRow(this.rows);
      this.count++;
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        try {
          final Record record = this.recordFactory.createRecord(this.recordDefinition);
          if (this.statistics == null) {
            this.recordStore.addStatistic("query", record);
          } else {
            this.statistics.add(record);
          }
          record.setState(RecordState.Initalizing);
          for (final FieldDefinition attribute : this.recordDefinition.getFields()) {
            final String name = attribute.getName();
            final AbstractFileGdbFieldDefinition esriAttribute = (AbstractFileGdbFieldDefinition)attribute;
            final Object value;
            synchronized (this.recordStore) {
              value = esriAttribute.getValue(row);
            }
            record.setValue(name, value);
          }
          record.setState(RecordState.Persisted);
          return record;

        } finally {
          this.recordStore.closeRow(row);
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
          final GeometryFactory geometryFactory = geometryField.getProperty(FieldProperties.GEOMETRY_FACTORY);
          if (geometryFactory != null) {
            this.boundingBox = boundingBox.convert(geometryFactory);
          }
        }
      }
    }
  }

  public void setSql(final String whereClause) {
    this.sql = whereClause;
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public String toString() {
    return this.typePath.toString();
  }

}
