package com.revolsys.data.types;

public class SimpleDataType implements DataType {
  private final Class<?> javaClass;

  private final String name;

  public SimpleDataType(final String name, final Class<?> javaClass) {
    this.name = name;
    this.javaClass = javaClass;
  }

  @Override
  public Class<?> getJavaClass() {
    return this.javaClass;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public String toString() {
    return this.name.toString();
  }

}
