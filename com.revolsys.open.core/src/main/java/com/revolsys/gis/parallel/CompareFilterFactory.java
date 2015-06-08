package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.data.record.Record;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Factory;
import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.filter.AttributesEqualFilter;
import com.revolsys.gis.data.model.filter.AttributesEqualOrNullFilter;

public class CompareFilterFactory implements Factory<Filter<Record>, Record> {
  private List<String> equalAttributeNames = new ArrayList<String>();

  private List<String> equalOrNullAttributeNames = new ArrayList<String>();

  @Override
  public Filter<Record> create(final Record object) {
    final AndFilter<Record> filters = new AndFilter<Record>();
    if (!this.equalAttributeNames.isEmpty()) {
      final Filter<Record> valuesFilter = new AttributesEqualFilter(object,
        this.equalAttributeNames);
      filters.addFilter(valuesFilter);
    }
    if (!this.equalOrNullAttributeNames.isEmpty()) {
      final Filter<Record> valuesFilter = new AttributesEqualOrNullFilter(object,
        this.equalOrNullAttributeNames);
      filters.addFilter(valuesFilter);
    }

    return filters;
  }

  public List<String> getEqualAttributeNames() {
    return this.equalAttributeNames;
  }

  public List<String> getEqualOrNullAttributeNames() {
    return this.equalOrNullAttributeNames;
  }

  public void setEqualAttributeNames(final List<String> equalAttributeNames) {
    this.equalAttributeNames = equalAttributeNames;
  }

  public void setEqualOrNullAttributeNames(final List<String> equalOrNullAttributeNames) {
    this.equalOrNullAttributeNames = equalOrNullAttributeNames;
  }

}
