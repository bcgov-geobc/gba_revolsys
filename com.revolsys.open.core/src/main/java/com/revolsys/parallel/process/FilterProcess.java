package com.revolsys.parallel.process;

import com.revolsys.filter.Filter;

public class FilterProcess<T> extends BaseInOutProcess<T, T> {
  private Filter<T> filter;

  private boolean invert = false;

  public Filter<T> getFilter() {
    return filter;
  }

  public boolean isInvert() {
    return invert;
  }

  protected void postAccept(final T object) {
  }

  protected void postReject(final T object) {
  }

  @Override
  protected void process(final com.revolsys.parallel.channel.Channel<T> in,
    final com.revolsys.parallel.channel.Channel<T> out, final T object) {
    boolean accept = filter.accept(object);
    if (invert) {
      accept = !accept;
    }
    if (accept) {
      out.write(object);
      postAccept(object);
    } else {
      postReject(object);
    }
  }

  public void setFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  public void setInvert(final boolean invert) {
    this.invert = invert;
  }

}
