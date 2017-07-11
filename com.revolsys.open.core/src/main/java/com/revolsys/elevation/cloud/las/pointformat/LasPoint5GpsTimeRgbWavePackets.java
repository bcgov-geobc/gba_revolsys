package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;

public class LasPoint5GpsTimeRgbWavePackets extends LasPoint3GpsTimeRgb
  implements LasPointWavePackets {
  private static final long serialVersionUID = 1L;

  private short wavePacketDescriptorIndex;

  private long byteOffsetToWaveformData;

  private long waveformPacketSizeInBytes;

  private float returnPointWaveformLocation;

  private float xT;

  private float yT;

  private float zT;

  public LasPoint5GpsTimeRgbWavePackets(final LasPointCloud pointCloud) {
    super(pointCloud);
  }

  @Override
  public long getByteOffsetToWaveformData() {
    return this.byteOffsetToWaveformData;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.GpsTimeRgbWavePackets;
  }

  @Override
  public float getReturnPointWaveformLocation() {
    return this.returnPointWaveformLocation;
  }

  @Override
  public long getWaveformPacketSizeInBytes() {
    return this.waveformPacketSizeInBytes;
  }

  @Override
  public short getWavePacketDescriptorIndex() {
    return this.wavePacketDescriptorIndex;
  }

  @Override
  public float getXT() {
    return this.xT;
  }

  @Override
  public float getYT() {
    return this.yT;
  }

  @Override
  public float getZT() {
    return this.zT;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final ChannelReader reader) {
    super.read(pointCloud, reader);
    this.wavePacketDescriptorIndex = reader.getByte();
    this.byteOffsetToWaveformData = reader.getUnsignedLong();
    this.waveformPacketSizeInBytes = reader.getUnsignedInt();
    this.returnPointWaveformLocation = reader.getFloat();
    this.xT = reader.getFloat();
    this.yT = reader.getFloat();
    this.zT = reader.getFloat();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "wavePacketDescriptorIndex", this.wavePacketDescriptorIndex);
    addToMap(map, "byteOffsetToWaveformData", this.byteOffsetToWaveformData);
    addToMap(map, "waveformPacketSizeInBytes", this.waveformPacketSizeInBytes);
    addToMap(map, "returnPointWaveformLocation", this.returnPointWaveformLocation);
    addToMap(map, "xT", this.xT);
    addToMap(map, "yT", this.yT);
    addToMap(map, "zT", this.zT);
    return map;
  }

  @Override
  public void write(final EndianOutput out) {
    super.write(out);
    out.write(this.wavePacketDescriptorIndex);
    out.writeLEUnsignedLong(this.byteOffsetToWaveformData);
    out.writeLEUnsignedInt(this.waveformPacketSizeInBytes);
    out.writeLEFloat(this.returnPointWaveformLocation);
    out.writeLEFloat(this.xT);
    out.writeLEFloat(this.yT);
    out.writeLEFloat(this.zT);
  }

}
