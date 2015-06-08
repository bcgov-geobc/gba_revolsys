package com.revolsys.visitor;

import java.util.Comparator;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;

public class DelegatingVisitor<T> extends AbstractVisitor<T> {
  private Visitor<T> visitor;

  public DelegatingVisitor() {
  }

  public DelegatingVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public DelegatingVisitor(final Comparator<T> comparator, final Visitor<T> visitor) {
    super(comparator);
    this.visitor = visitor;
  }

  public DelegatingVisitor(final Filter<T> filter) {
    super(filter);
  }

  public DelegatingVisitor(final Filter<T> filter, final Comparator<T> comparator) {
    super(filter, comparator);
  }

  public DelegatingVisitor(final Filter<T> filter, final Comparator<T> comparator,
    final Visitor<T> visitor) {
    super(filter, comparator);
    this.visitor = visitor;
  }

  public DelegatingVisitor(final Filter<T> filter, final Visitor<T> visitor) {
    super(filter);
    this.visitor = visitor;
  }

  public DelegatingVisitor(final Visitor<T> visitor) {
    this.visitor = visitor;
  }

  public Visitor<T> getVisitor() {
    return this.visitor;
  }

  public void setVisitor(final Visitor<T> visitor) {
    this.visitor = visitor;
  }

  @Override
  public String toString() {
    return this.visitor.toString();
  }

  @Override
  public boolean visit(final T item) {
    final Filter<T> filter = getFilter();
    if (filter == null || filter.accept(item)) {
      return this.visitor.visit(item);
    } else {
      return true;
    }
  }
}
