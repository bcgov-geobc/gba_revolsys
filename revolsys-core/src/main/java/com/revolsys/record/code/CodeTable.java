package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JComponent;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Code;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.refresh.Refreshable;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public interface CodeTable
  extends Emptyable, Cloneable, Comparator<Object>, BaseCloseable, Refreshable {
  static CodeTable newCodeTable(final Map<String, ? extends Object> config) {
    return new RecordStoreCodeTableProperty(config);
  }

  static CodeTable newCodeTable(final String name, final Object source) {
    try (
      final RecordReader reader = RecordReader.newRecordReader(source)) {
      final int fieldCount = reader.getRecordDefinition().getFieldCount();
      if (fieldCount == 2) {
        return SingleValueCodeTable.newCodeTable(name, reader);
      } else {
        return MultiValueCodeTable.newCodeTable(name, reader);
      }
    }
  }

  default void addValue(final Record record) {
    throw new UnsupportedOperationException("addValue");
  }

  @Override
  default int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (value2 == null) {
      return -1;
    } else {
      final Object codeValue1 = getValue(Identifier.newIdentifier(value1));
      final Object codeValue2 = getValue(Identifier.newIdentifier(value2));
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  default CodeTableEntry getEntry(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      return getEntry(value);
    } else {
      return null;
    }
  }

  default CodeTableEntry getEntry(final Object... values) {
    if (values != null && values.length == 1) {
      final Object value = values[0];
      return getEntry(value);
    } else {
      return CodeTableEntry.EMPTY;
    }
  }

  CodeTableEntry getEntry(Object value);

  default List<String> getFieldNameAliases() {
    return Collections.emptyList();
  }

  default Identifier getIdentifier(final List<Object> values) {
    return getEntry(values).getIdentifier();
  }

  default Identifier getIdentifier(final Map<String, ? extends Object> valueMap) {
    final List<String> valueFieldNames = getValueFieldNames();
    final List<Object> values = new ArrayList<>();
    for (final String name : valueFieldNames) {
      final Object value = valueMap.get(name);
      values.add(value);
    }
    return getEntry(values).getIdentifier();
  }

  default Identifier getIdentifier(final Object... values) {
    return getEntry(values).getIdentifier();
  }

  default Identifier getIdentifier(final Object value) {
    return getEntry(value).getIdentifier();
  }

  Identifier getIdentifierByIndex(final int index);

  List<Identifier> getIdentifiers();

  String getIdFieldName();

  default JsonObject getMap(final CodeTableEntry entry) {
    if (entry == null || entry.isEmpty()) {
      return JsonObject.EMPTY;
    } else if (isSingleValue()) {
      final List<String> keys = getValueFieldNames();
      final var fieldName = keys.get(0);
      final var value = entry.getValue();
      return JsonObject.hash(fieldName, value);
    } else {
      final List<String> keys = getValueFieldNames();
      final List<Object> values = entry.getValues();
      return JsonObject.hash(keys, values);
    }
  }

  default JsonObject getMap(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return JsonObject.EMPTY;
    } else {
      final JsonObject map = JsonObject.hash();
      int i = 0;
      for (final String name : getValueFieldNames()) {
        final Object value = values.get(i);
        map.addValue(name, value);
        i++;
      }
      return map;
    }
  }

  default Map<String, ? extends Object> getMap(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getMap(identifier);
  }

  default String getName() {
    return getClass().getSimpleName();
  }

  default JComponent getSwingEditor() {
    return null;
  }

  <V> V getValue(final Identifier id);

  default <V> V getValue(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getValue(identifier);
  }

  default FieldDefinition getValueFieldDefinition() {
    return null;
  }

  int getValueFieldLength();

  default List<String> getValueFieldNames() {
    return Arrays.asList("VALUE");
  }

  default <V> List<V> getValues() {
    final List<V> values = new ArrayList<>();
    for (final Identifier identifier : getIdentifiers()) {
      final V value = getValue(identifier);
      values.add(value);
    }
    return values;
  }

  default List<Object> getValues(final Consumer<CodeTableEntry> callback, final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getEntry(identifier).getValues();
  }

  default List<Object> getValues(final Identifier id) {
    return getEntry(id).getValues();
  }

  default List<Object> getValues(final Object id) {
    return getEntry(id).getValues();
  }

  default boolean hasValue(final Object value) {
    return getIdentifier(value) != null || getValue(value) != null;
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

  default boolean isLoadAll() {
    return true;
  }

  default boolean isLoaded() {
    return true;
  }

  default boolean isLoading() {
    return false;
  }

  default boolean isMultiValue() {
    return false;
  }

  default boolean isSingleValue() {
    return !isMultiValue();
  }

  @Override
  default void refresh() {
  }

  @Override
  default boolean refreshIfNeeded() {
    refresh();
    return true;
  }

  default CodeTable setLoadMissingCodes(final boolean loadMissingCodes) {
    throw new UnsupportedOperationException("setLoadMissingNodes");
  }

  int size();

  default String toCodeString(final DataType type, final Object value) {
    if (value == null || value instanceof String && !Property.hasValue(value)) {
      return null;
    } else {
      final List<Object> values = getValues(value);
      if (values == null || values.isEmpty()) {
        return type.toString(value);
      } else if (values.size() == 1) {
        final Object codeValue = values.get(0);
        if (codeValue instanceof Code) {
          return ((Code)codeValue).getDescription();
        } else if (codeValue instanceof String) {
          final String string = (String)codeValue;
          if (Property.hasValue(string)) {
            return string;
          } else {
            return null;
          }
        } else {
          return DataTypes.toString(codeValue);
        }
      } else {
        return Strings.toString(values);
      }
    }
  }

  void withEntry(Consumer<CodeTableEntry> callback, Object idOrValue);

}
