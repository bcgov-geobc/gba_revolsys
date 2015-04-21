package com.revolsys.gis.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryBufferProcess extends
  BaseInOutProcess<Record, Record> {

  private int buffer;

  public int getBuffer() {
    return buffer;
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry == null) {
      out.write(object);
    } else {
      final Geometry bufferedGeometry = geometry.buffer(buffer);
      final Record newObject = DataObjectUtil.copy(object, bufferedGeometry);
      out.write(newObject);
    }
  }

  public void setBuffer(final int buffer) {
    this.buffer = buffer;
  }

}
