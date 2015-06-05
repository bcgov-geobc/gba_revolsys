package com.revolsys.io.kml;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.core.io.Resource;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.SpringUtil;
import com.vividsolutions.jts.geom.Geometry;

public class KmzGeometryIterator extends AbstractIterator<Geometry> {

  private KmlGeometryIterator kmlIterator;

  private ZipInputStream zipIn;

  public KmzGeometryIterator(final Resource resource) {
    try {
      final InputStream in = SpringUtil.getInputStream(resource);
      zipIn = new ZipInputStream(in);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to reade KMZ file", e);
    }
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(kmlIterator, zipIn);
    kmlIterator = null;
    zipIn = null;
  }

  @Override
  protected void doInit() {
    try {
      for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
        final String name = entry.getName();
        final String extension = FileUtil.getFileNameExtension(name);
        if ("kml".equals(extension)) {
          kmlIterator = new KmlGeometryIterator(zipIn);
          return;
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read KML file inside KMZ file", e);
    }
  }

  @Override
  protected Geometry getNext() {
    if (kmlIterator == null) {
      throw new NoSuchElementException();
    } else {
      return kmlIterator.getNext();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
