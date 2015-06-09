package com.revolsys.raster;

import org.springframework.core.io.Resource;

public class PngImage extends JaiGeoreferencedImage {

  public PngImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "pgw";
  }
}
