package com.revolsys.format.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CsvMapIterator implements Iterator<Map<String, Object>> {

  /** The values for each record header type. */
  private List<String> fieldNames = new ArrayList<String>();

  /** The reader to */
  private final CsvIterator in;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   *
   * @param reader
   * @throws IOException
   */
  public CsvMapIterator(final Reader in) throws IOException {
    this.in = new CsvIterator(in);
    readRecordHeader();
  }

  public void close() {
    this.in.close();
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   *
   * @return <tt>true</tt> if the iterator has more elements.
   */
  @Override
  public boolean hasNext() {
    return this.in.hasNext();
  }

  /**
   * Return the next Record from the iterator.
   *
   * @return The Record
   */
  @Override
  public Map<String, Object> next() {
    if (hasNext()) {
      final List<String> record = this.in.next();
      return parseMap(record);
    } else {
      throw new NoSuchElementException("No more elements");
    }
  }

  private Map<String, Object> parseMap(final List<String> record) {
    final Map<String, Object> map = new LinkedHashMap<>();
    for (int i = 0; i < this.fieldNames.size() && i < record.size(); i++) {
      final String fieldName = this.fieldNames.get(i);
      final String value = record.get(i);
      if (value != null) {
        map.put(fieldName, value);
      }
    }
    return map;
  }

  /**
   * Read the record header block.
   *
   * @throws IOException If there was an error reading the header.
   */
  private void readRecordHeader() throws IOException {
    if (hasNext()) {
      this.fieldNames = this.in.next();
    }
  }

  /**
   * Removing items from the iterator is not supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
