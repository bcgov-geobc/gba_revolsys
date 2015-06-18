package com.revolsys.data.equals;

import java.util.Collection;

public class ObjectEquals implements Equals<Object> {
  @Override
  public boolean equals(final Object object1, final Object object2, final Collection<String> exclude) {
    if (object1 == null) {
      return object2 == null;
    } else {
      return object1.equals(object2);
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
