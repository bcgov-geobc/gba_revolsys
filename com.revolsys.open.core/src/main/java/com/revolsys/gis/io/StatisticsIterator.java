package com.revolsys.gis.io;

import java.util.Iterator;

import com.revolsys.data.record.Record;

public class StatisticsIterator implements Iterator<Record> {
  private final Iterator<Record> iterator;

  private Statistics statistics;

  public StatisticsIterator(final Iterator<Record> iterator,
    final Statistics statistics) {
    this.iterator = iterator;
    setStatistics(statistics);
  }

  /**
   * @return the stats
   */
  public Statistics getStatistics() {
    return statistics;
  }

  @Override
  public boolean hasNext() {
    final boolean hasNext = iterator.hasNext();
    if (!hasNext) {
      statistics.disconnect();
    }
    return hasNext;
  }

  @Override
  public Record next() {
    final Record object = iterator.next();
    if (object != null) {
      statistics.add(object);
    }
    return object;
  }

  @Override
  public void remove() {
    iterator.remove();
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
    statistics.connect();
  }

}
