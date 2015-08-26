package com.revolsys.visitor;

import java.util.Comparator;

import java.util.function.Predicate;

public class BaseVisitor<T> extends AbstractVisitor<T> {

  public BaseVisitor() {
  }

  public BaseVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public BaseVisitor(final Predicate<T> filter) {
    super(filter);
  }

  public BaseVisitor(final Predicate<T> filter, final Comparator<T> comparator) {
    super(filter, comparator);
  }

  protected boolean doVisit(final T object) {
    return true;
  }

  @Override
  public boolean visit(final T object) {
    final Predicate<T> filter = getFilter();
    if (filter == null || filter.test(object)) {
      return doVisit(object);
    } else {
      return true;
    }
  }
}
