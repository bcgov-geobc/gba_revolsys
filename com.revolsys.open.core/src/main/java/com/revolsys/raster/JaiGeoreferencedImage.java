package com.revolsys.raster;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.springframework.core.io.Resource;

public class JaiGeoreferencedImage extends AbstractGeoreferencedImage {

  protected JaiGeoreferencedImage() {
  }

  public JaiGeoreferencedImage(final Resource imageResource) {
    setImageResource(imageResource);

    final PlanarImage jaiImage = JAI.create("fileload", getFile().getAbsolutePath());
    setRenderedImage(jaiImage);

    loadImageMetaData();
    postConstruct();
  }
}
