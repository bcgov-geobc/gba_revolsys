package com.revolsys.gis.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;

public class SynchronizationProcess extends
  AbstractInOutProcess<Record, Record> {
  private int count = 0;

  @Override
  public synchronized Channel<Record> getIn() {
    count++;
    return super.getIn();
  }

  @Override
  protected void run(final Channel<Record> in, final Channel<Record> out) {
    do {
      for (Record object = in.read(); object != null; object = in.read()) {
        out.write(object);
      }
      count--;
    } while (count > 0);
  }
}
