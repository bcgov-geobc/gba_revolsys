package com.revolsys.data.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class And extends AbstractMultiCondition {

  public And(final Collection<? extends Condition> conditions) {
    super("AND", conditions);
  }

  public And(final Condition... conditions) {
    this(Arrays.asList(conditions));
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public boolean accept(final Map<String, Object> object) {
    final List<Condition> conditions = (List)getQueryValues();
    for (final Condition condition : conditions) {
      if (!condition.accept(object)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public And clone() {
    return (And)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof And) {
      return super.equals(obj);
    }
    return false;
  }
}
