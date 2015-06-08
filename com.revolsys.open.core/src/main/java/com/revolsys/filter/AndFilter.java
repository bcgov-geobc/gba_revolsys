package com.revolsys.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AndFilter<T> implements Filter<T> {
  private final List<Filter<T>> filters = new ArrayList<Filter<T>>();

  public AndFilter() {
  }

  public AndFilter(final Collection<Filter<T>> filters) {
    this.filters.addAll(filters);
  }

  public AndFilter(final Filter<T>... filters) {
    this(Arrays.asList(filters));
  }

  @Override
  public boolean accept(final T object) {
    for (final Filter<T> filter : this.filters) {
      final boolean accept = filter.accept(object);
      if (!accept) {

        return false;
      }
    }
    return true;
  }

  public void addFilter(final Filter<T> filter) {
    this.filters.add(filter);
  }

  @Override
  public String toString() {
    return "AND" + this.filters;
  }
}
