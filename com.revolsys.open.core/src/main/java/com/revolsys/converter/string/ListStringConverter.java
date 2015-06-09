package com.revolsys.converter.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.format.json.JsonParser;

public class ListStringConverter implements StringConverter<List<String>> {
  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public Class<List<String>> getConvertedClass() {
    final Class clazz = List.class;
    return clazz;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public List<String> toObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Collection) {
      final Collection<Object> collection = (Collection)value;
      final List<String> list = new ArrayList<String>();
      for (final Object object : collection) {
        final String stringValue = StringConverterRegistry.toString(object);
        list.add(stringValue);
      }
      return list;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> toObject(final String string) {
    final Object value = JsonParser.read(string);
    if (value instanceof List) {
      return (List<String>)value;
    } else {
      throw new IllegalArgumentException("Value must be a JSON list " + string);
    }
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof List) {
      final List<?> list = (List<?>)value;
      final StringBuffer string = new StringBuffer("[");
      for (final Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
        final Object object = iterator.next();
        final String stringValue = StringConverterRegistry.toString(object);
        string.append(stringValue);
        if (iterator.hasNext()) {
          string.append(", ");
        }
      }
      string.append("]");
      return string.toString();
    } else {
      return value.toString();
    }

  }
}
