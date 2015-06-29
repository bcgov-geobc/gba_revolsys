package com.revolsys.data.record.io;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.parallel.process.AbstractProcess;

public class RecordStoreQueryTask extends AbstractProcess {

  private final RecordStore dataStore;

  private final BoundingBox boundingBox;

  private List<Record> objects;

  private final String path;

  public RecordStoreQueryTask(final RecordStore dataStore, final String path,
    final BoundingBox boundingBox) {
    this.dataStore = dataStore;
    this.path = path;
    this.boundingBox = boundingBox;
  }

  public void cancel() {
    this.objects = null;
  }

  @Override
  public String getBeanName() {
    return getClass().getName();
  }

  @Override
  public void run() {
    this.objects = new ArrayList<Record>();
    final Query query = new Query(this.path);
    query.setBoundingBox(this.boundingBox);
    final Reader<Record> reader = this.dataStore.query(query);
    try {
      for (final Record object : reader) {
        try {
          this.objects.add(object);
        } catch (final NullPointerException e) {
          return;
        }
      }
    } finally {
      reader.close();
    }
  }

  @Override
  public void setBeanName(final String name) {
  }
}