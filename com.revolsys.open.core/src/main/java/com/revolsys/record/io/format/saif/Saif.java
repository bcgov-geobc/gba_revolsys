package com.revolsys.record.io.format.saif;

import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.Resource;

public class Saif extends AbstractRecordIoFactory {

  public Saif() {
    super("SAIF");
    addMediaTypeAndFileExtension("zip/x-saif", "saf");
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public RecordReader newRecordReader(final Resource resource, final RecordFactory recordFactory) {
    final SaifReader reader = new SaifReader(resource);
    return reader;
  }
}