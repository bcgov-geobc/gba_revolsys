package com.revolsys.io;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import com.revolsys.util.Property;

public interface ObjectWithProperties {

  @SuppressWarnings("unchecked")
  static <C> C getProperty(final ObjectWithProperties object, final Map<String, Object> properties,
    final String name) {
    if (properties == null) {
      return null;
    } else {
      Object value = properties.get(name);
      if (value instanceof Reference) {
        final Reference<C> reference = (Reference<C>)value;
        if (reference.isEnqueued()) {
          value = null;
        } else {
          value = reference.get();
        }
        if (value == null) {
          properties.remove(name);
        }
      }
      // if (value instanceof ObjectPropertyProxy) {
      // final ObjectPropertyProxy<C, Object> proxy = (ObjectPropertyProxy<C,
      // Object>)value;
      // value = proxy.getValue(object);
      // }
      return (C)value;
    }
  }

  default void clearProperties() {
    final Map<String, Object> properties = getProperties();
    properties.clear();
  }

  @PreDestroy
  default void close() {
    clearProperties();
  }

  Map<String, Object> getProperties();

  default <C> C getProperty(final String name) {
    final Map<String, Object> properties = getProperties();
    return getProperty(this, properties, name);
  }

  @SuppressWarnings("unchecked")
  default <C> C getProperty(final String name, final C defaultValue) {
    final C value = (C)getProperty(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default boolean hasProperty(final String name) {
    final Object value = getProperty(name);
    return Property.hasValue(value);
  }

  default void removeProperty(final String propertyName) {
    final Map<String, Object> properties = getProperties();
    properties.remove(propertyName);
  }

  default void setProperties(final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String name = entry.getKey();
        final Object value = entry.getValue();
        setProperty(name, value);
      }
    }
  }

  default void setProperty(final String name, final Object value) {
    final Map<String, Object> properties = getProperties();
    properties.put(name, value);
  }

  default void setPropertySoft(final String name, final Object value) {
    setProperty(name, new SoftReference<Object>(value));
  }

  default void setPropertyWeak(final String name, final Object value) {
    setProperty(name, new WeakReference<Object>(value));
  }
}
