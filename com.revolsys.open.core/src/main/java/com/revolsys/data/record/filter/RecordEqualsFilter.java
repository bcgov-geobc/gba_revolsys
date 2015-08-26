package com.revolsys.data.record.filter;

import java.util.Collection;
import java.util.HashSet;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Geometry;

public class RecordEqualsFilter implements Filter<Record> {
  private final Collection<String> equalExclude = new HashSet<String>();

  private EqualsRegistry equalsRegistry = EqualsInstance.INSTANCE;

  private final Record searchObject;

  public RecordEqualsFilter(final EqualsRegistry equalsRegistry, final Record searchObject) {
    this(null, searchObject, null);
  }

  public RecordEqualsFilter(final EqualsRegistry equalsRegistry, final Record searchObject,
    final Collection<String> equalExclude) {
    if (equalsRegistry != null) {
      this.equalsRegistry = equalsRegistry;
    }
    this.searchObject = searchObject;
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  public RecordEqualsFilter(final Record searchObject) {
    this(null, searchObject, null);
  }

  public RecordEqualsFilter(final Record searchObject, final Collection<String> equalExclude) {
    this(null, searchObject, equalExclude);
  }

  @Override
  public boolean accept(final Record object) {
    final Geometry serachGeometry = this.searchObject.getGeometry();
    final Geometry geometry = object.getGeometry();

    if (this.equalsRegistry.equals(serachGeometry, geometry, this.equalExclude)) {
      if (this.equalsRegistry.equals(this.searchObject, object, this.equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
