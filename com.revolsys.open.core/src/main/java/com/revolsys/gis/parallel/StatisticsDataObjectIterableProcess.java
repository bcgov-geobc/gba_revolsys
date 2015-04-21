package com.revolsys.gis.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;

public class StatisticsDataObjectIterableProcess extends
  IterableProcess<Record> {

  private Statistics statistics;

  public StatisticsDataObjectIterableProcess() {
  }

  @Override
  protected void destroy() {
    super.destroy();
    if (statistics != null) {
      statistics.disconnect();
      statistics = null;
    }
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
    if (statistics != null) {
      statistics.connect();
    }
  }

  @Override
  protected void write(final Channel<Record> out, final Record record) {
    if (record != null) {
      statistics.add(record);
      out.write(record);
    }
  }
}
