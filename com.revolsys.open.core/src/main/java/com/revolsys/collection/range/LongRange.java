package com.revolsys.collection.range;

import com.revolsys.util.Numbers;

/**
 *
 * Ranges are immutable
 */
public class LongRange extends AbstractRange<Long> {
  private long from;

  private long to;

  public LongRange(final long value) {
    this(value, value);
  }

  public LongRange(final long from, final long to) {
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
  }

  @Override
  protected LongRange createNew(final Object from, final Object to) {
    return new LongRange((Long)from, (Long)to);
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    final Long longValue = Numbers.toLong(value);
    if (longValue == null) {
      return null;
    } else {
      return super.expand(longValue);
    }
  }

  @Override
  public Long getFrom() {
    return this.from;
  }

  @Override
  public Long getTo() {
    return this.to;
  }

  @Override
  public Long next(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Long longValue = Numbers.toLong(value);
      if (longValue == null) {
        return null;
      } else {
        final long number = longValue.longValue();
        if (number == Long.MAX_VALUE) {
          return null;
        } else {
          return number + 1;
        }
      }
    }
  }

  @Override
  public Long previous(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Long longValue = Numbers.toLong(value);
      if (longValue == null) {
        return null;
      } else {
        final long number = longValue.longValue();
        if (number == Long.MIN_VALUE) {
          return null;
        } else {
          return number - 1;
        }
      }
    }
  }

  @Override
  public long size() {
    return this.to - this.from + 1;
  }
}
