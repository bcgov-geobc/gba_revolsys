package com.revolsys.data.record;

import com.revolsys.data.record.schema.RecordDefinition;

/**
 * A record factory
 *
 * @author paustin
 */
public interface RecordFactory {
  /**
   * Create an instance of record implementation supported by this factory
   * using the metadata
   *
   * @param metaData The metadata used to create the instance.
   * @return The record instance.
   */
  Record createRecord(RecordDefinition metaData);
}
