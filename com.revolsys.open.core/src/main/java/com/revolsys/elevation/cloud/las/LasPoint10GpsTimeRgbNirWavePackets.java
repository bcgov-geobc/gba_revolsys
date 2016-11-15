package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint10GpsTimeRgbNirWavePackets extends LasPoint7GpsTimeRgb
  implements LasPointWavePackets {
  private static final long serialVersionUID = 1L;

  public static LasPoint10GpsTimeRgbNirWavePackets newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint10GpsTimeRgbNirWavePackets(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private short wavePacketDescriptorIndex;

  private long byteOffsetToWaveformData;

  private long waveformPacketSizeInBytes;

  private float returnPointWaveformLocation;

  private float xT;

  private float yT;

  private float zT;

  private int nir;

  public LasPoint10GpsTimeRgbNirWavePackets(final LasPointCloud pointCloud, final double x,
    final double y, final double z) {
    super(pointCloud, x, y, z);
  }

  public LasPoint10GpsTimeRgbNirWavePackets(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) throws IOException {
    super(pointCloud, recordDefinition, in);
  }

  @Override
  public long getByteOffsetToWaveformData() {
    return this.byteOffsetToWaveformData;
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
  protected void read(final LasPointCloud pointCloud, final EndianInput in) throws IOException {
    super.read(pointCloud, in);
    this.nir = in.readLEUnsignedShort();
    this.wavePacketDescriptorIndex = in.readByte();
    this.byteOffsetToWaveformData = in.readLEUnsignedLong();
    this.waveformPacketSizeInBytes = in.readLEUnsignedInt();
    this.returnPointWaveformLocation = in.readLEFloat();
    this.xT = in.readLEFloat();
    this.yT = in.readLEFloat();
    this.zT = in.readLEFloat();
  }

  @Override
  protected void write(final LasPointCloud pointCloud, final EndianOutput out) {
    super.write(pointCloud, out);
    out.writeLEUnsignedShort(this.nir);
    out.write(this.wavePacketDescriptorIndex);
    out.writeLEUnsignedLong(this.byteOffsetToWaveformData);
    out.writeLEUnsignedInt(this.waveformPacketSizeInBytes);
    out.writeLEFloat(this.returnPointWaveformLocation);
    out.writeLEFloat(this.xT);
    out.writeLEFloat(this.yT);
    out.writeLEFloat(this.zT);
  }
}