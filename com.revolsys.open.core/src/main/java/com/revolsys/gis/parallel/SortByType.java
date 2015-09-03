package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.data.comparator.RecordDefinitionNameComparator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class SortByType extends BaseInOutProcess<Record, Record> {

  private final Map<RecordDefinition, Collection<Record>> objectsByType = new TreeMap<RecordDefinition, Collection<Record>>(
    new RecordDefinitionNameComparator());

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    for (final Collection<Record> objects : this.objectsByType.values()) {
      for (final Record object : objects) {
        out.write(object);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final RecordDefinition metaData = object.getRecordDefinition();
    Collection<Record> objects = this.objectsByType.get(metaData);
    if (objects == null) {
      objects = new ArrayList<Record>();
      this.objectsByType.put(metaData, objects);
    }
    objects.add(object);
  }
}
