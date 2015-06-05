package com.revolsys.data.record.io;

import java.io.File;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;

public interface RecordReaderFactory extends IoFactory {

  RecordReader createRecordReader(Resource resource);

  RecordReader createRecordReader(Resource resource,
    RecordFactory factory);

  Reader<Record> createDirectoryRecordReader();

  Reader<Record> createDirectoryRecordReader(File file);

  Reader<Record> createDirectoryRecordReader(File file,
    RecordFactory factory);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isBinary();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);
}
