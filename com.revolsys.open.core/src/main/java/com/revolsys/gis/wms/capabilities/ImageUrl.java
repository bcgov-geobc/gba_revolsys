package com.revolsys.gis.wms.capabilities;

public class ImageUrl extends FormatUrl {
  private int width;

  private int height;

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }

  public void setHeight(final int height) {
    this.height = height;
  }

  public void setWidth(final int width) {
    this.width = width;
  }

}
