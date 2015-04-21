package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.data.record.Record;
import com.revolsys.gis.data.model.RecordDefinition;
import com.revolsys.gis.data.model.comparator.DataObjectMetaDataNameComparator;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class SortByType extends BaseInOutProcess<Record, Record> {

  private final Map<RecordDefinition, Collection<Record>> objectsByType = new TreeMap<RecordDefinition, Collection<Record>>(
    new DataObjectMetaDataNameComparator());

  @Override
  protected void postRun(final Channel<Record> in,
    final Channel<Record> out) {
    for (final Collection<Record> objects : objectsByType.values()) {
      for (final Record object : objects) {
        out.write(object);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    final RecordDefinition metaData = object.getMetaData();
    Collection<Record> objects = objectsByType.get(metaData);
    if (objects == null) {
      objects = new ArrayList<Record>();
      objectsByType.put(metaData, objects);
    }
    objects.add(object);
  }
}
