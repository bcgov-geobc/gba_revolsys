package com.revolsys.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.revolsys.collection.list.Lists;

public class AndPredicate<T> implements Predicate<T> {
  private final List<Predicate<T>> predicates = new ArrayList<>();

  public AndPredicate() {
  }

  public AndPredicate(final Iterable<Predicate<T>> predicates) {
    Lists.addAll(this.predicates, predicates);
  }

  @SuppressWarnings("unchecked")
  public AndPredicate(final Predicate<T>... predicates) {
    this(Arrays.asList(predicates));
  }

  public void addFilter(final Predicate<T> predicate) {
    this.predicates.add(predicate);
  }

  @Override
  public boolean test(final T object) {
    for (final Predicate<T> predicate : this.predicates) {
      final boolean test = predicate.test(object);
      if (!test) {

        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "AND" + this.predicates;
  }
}
