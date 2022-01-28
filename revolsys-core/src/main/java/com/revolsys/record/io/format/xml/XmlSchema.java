package com.revolsys.record.io.format.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.xml.namespace.QName;

import org.jeometry.common.data.type.DataType;

public class XmlSchema {
  private final XmlNamespace namespace;

  private final Map<QName, XmlType> typeByName = new HashMap<>();

  private final Map<QName, XmlElement> elementByName = new HashMap<>();

  public XmlSchema(final XmlNamespace namespace) {
    this.namespace = namespace;
  }

  public XmlElement addElement(final String localPart, final XmlType type) {
    final XmlName xmlName = this.namespace.getName(localPart);
    return addElement(type, xmlName);
  }

  public XmlElement addElement(final XmlType type, final XmlName xmlName) {
    final XmlElement element = new XmlElement(xmlName, type);
    this.elementByName.put(xmlName, element);
    return element;
  }

  public XmlSchema addType(final XmlType type) {
    final XmlName xmlName = type.getXmlName();
    this.typeByName.put(xmlName, type);
    return this;
  }

  public XmlComplexType createComplexType(final String localPart) {
    final XmlComplexType type = new XmlComplexType(this.namespace, localPart);
    addType(type);
    return type;
  }

  public XmlSimpleTypeDataType createSimpleType(final String localPart, final DataType dataType) {
    final XmlSimpleTypeDataType type = new XmlSimpleTypeDataType(this.namespace, localPart,
      dataType);
    addType(type);
    return type;
  }

  public <V> XmlSimpleTypeFunction<V> createSimpleType(final String localPart,
    final Function<String, V> converter) {
    final XmlSimpleTypeFunction<V> type = new XmlSimpleTypeFunction<V>(this.namespace, localPart,
      converter);
    addType(type);
    return type;
  }

  @Override
  public String toString() {
    return this.namespace.toString();
  }

}