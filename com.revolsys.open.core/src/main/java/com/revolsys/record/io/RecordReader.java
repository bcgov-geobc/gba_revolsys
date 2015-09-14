package com.revolsys.record.io;

import java.io.File;

import org.springframework.core.io.Resource;

import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;
import com.revolsys.record.ArrayRecordFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.schema.RecordDefinition;

public interface RecordReader extends Reader<Record> {
  /**
   * Create a {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader create(final Object source) {
    final RecordFactory recordFactory = ArrayRecordFactory.INSTANCE;
    return create(source, recordFactory);
  }

  /**
   * Create a {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @param recordFactory The factory used to create records.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader create(final Object source, final RecordFactory recordFactory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, source);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(source, recordFactory);
      return reader;
    }
  }

  static boolean isReadable(final Object source) {
    return IoFactoryRegistry.isAvailable(RecordReaderFactory.class, source);
  }

  RecordDefinition getRecordDefinition();

}
