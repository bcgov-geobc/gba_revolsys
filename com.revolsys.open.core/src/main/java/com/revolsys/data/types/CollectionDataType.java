package com.revolsys.data.types;

public class CollectionDataType extends SimpleDataType {

  private final DataType contentType;

  public CollectionDataType(final String name, final Class<?> javaClass, final DataType contentType) {
    super(name, javaClass);
    this.contentType = contentType;
  }

  public DataType getContentType() {
    return this.contentType;
  }

}
