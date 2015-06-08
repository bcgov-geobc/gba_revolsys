package com.revolsys.visitor;

import java.util.Comparator;

import com.revolsys.collection.Visitor;
import com.revolsys.comparator.ComparatorProxy;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterProxy;

public abstract class AbstractVisitor<T> implements Visitor<T>, FilterProxy<T>, ComparatorProxy<T> {
  private Filter<T> filter;

  private Comparator<T> comparator;

  public AbstractVisitor() {
  }

  public AbstractVisitor(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public AbstractVisitor(final Filter<T> filter) {
    this.filter = filter;
  }

  public AbstractVisitor(final Filter<T> filter, final Comparator<T> comparator) {
    this.filter = filter;
    this.comparator = comparator;
  }

  @Override
  public Comparator<T> getComparator() {
    return this.comparator;
  }

  @Override
  public Filter<T> getFilter() {
    return this.filter;
  }

  public void setComparator(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public void setFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  public void setFilters(final Filter<T>... filters) {
    this.filter = new AndFilter<T>(filters);
  }
}
