package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;

public class LasPoint7GpsTimeRgb extends LasPoint6GpsTime implements LasPointRgb {
  private static final long serialVersionUID = 1L;

  private int red;

  private int green;

  private int blue;

  public LasPoint7GpsTimeRgb(final LasPointCloud pointCloud) {
    super(pointCloud);
  }

  @Override
  public int getBlue() {
    return this.blue;
  }

  @Override
  public int getGreen() {
    return this.green;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.ExtendedGpsTimeRgb;
  }

  @Override
  public int getRed() {
    return this.red;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final ChannelReader reader) {
    super.read(pointCloud, reader);
    this.red = reader.getUnsignedShort();
    this.green = reader.getUnsignedShort();
    this.blue = reader.getUnsignedShort();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "red", this.red);
    addToMap(map, "green", this.green);
    addToMap(map, "blue", this.blue);
    return map;
  }

  @Override
  public void write(final EndianOutput out) {
    super.write(out);
    out.writeLEUnsignedShort(this.red);
    out.writeLEUnsignedShort(this.green);
    out.writeLEUnsignedShort(this.blue);
  }
}