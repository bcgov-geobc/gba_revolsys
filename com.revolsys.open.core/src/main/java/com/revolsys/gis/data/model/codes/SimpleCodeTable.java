package com.revolsys.gis.data.model.codes;

import java.util.List;

public class SimpleCodeTable extends AbstractCodeTable {
  private int index = 0;

  public void addValue(final Object... values) {
    index++;
    addValue(index, values);
  }

  @Override
  public void addValue(final Object id, final Object... values) {
    super.addValue(id, values);
  }

  @Override
  public SimpleCodeTable clone() {
    return (SimpleCodeTable)super.clone();
  }

  @Override
  public String getIdAttributeName() {
    return null;
  }

  @Override
  protected Object loadId(final List<Object> values, final boolean createId) {
    index++;
    return index;
  }

}
