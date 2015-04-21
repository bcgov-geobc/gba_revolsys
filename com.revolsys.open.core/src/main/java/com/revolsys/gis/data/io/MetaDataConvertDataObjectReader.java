package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.gis.data.model.RecordDefinition;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class MetaDataConvertDataObjectReader extends AbstractReader<Record>
  implements DataObjectReader, Iterator<Record> {

  private final RecordDefinition metaData;

  private final Reader<Record> reader;

  private boolean open;

  private Iterator<Record> iterator;

  public MetaDataConvertDataObjectReader(final RecordDefinition metaData,
    final Reader<Record> reader) {
    this.metaData = metaData;
    this.reader = reader;
  }

  @Override
  public void close() {
    reader.close();
  }

  @Override
  public RecordDefinition getMetaData() {
    return metaData;
  }

  @Override
  public boolean hasNext() {
    if (!open) {
      open();
    }
    return iterator.hasNext();
  }

  @Override
  public Iterator<Record> iterator() {
    return this;
  }

  @Override
  public Record next() {
    if (hasNext()) {
      final Record source = iterator.next();
      final Record target = new ArrayRecord(metaData);
      for (final FieldDefinition attribute : metaData.getAttributes()) {
        final String name = attribute.getName();
        final Object value = source.getValue(name);
        if (value != null) {
          final DataType dataType = metaData.getAttributeType(name);
          final Object convertedValue = StringConverterRegistry.toObject(
            dataType, value);
          target.setValue(name, convertedValue);
        }
      }
      return target;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void open() {
    open = true;
    this.iterator = reader.iterator();
  }

  @Override
  public void remove() {
    iterator.remove();
  }
}
