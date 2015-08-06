package com.revolsys.swing.dnd.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.format.csv.Csv;

public class RecordReaderTransferable implements Transferable {

  public static final DataFlavor DATA_OBJECT_READER_FLAVOR = new DataFlavor(RecordReader.class,
    "Record List");

  private static final DataFlavor[] DATA_FLAVORS = {
    DATA_OBJECT_READER_FLAVOR, DataFlavor.stringFlavor
  };

  private final RecordReader reader;

  public RecordReaderTransferable(final RecordReader reader) {
    this.reader = reader;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (this.reader == null) {
      return null;
    } else
      if (DATA_OBJECT_READER_FLAVOR.equals(flavor) || MapTransferable.MAP_FLAVOR.equals(flavor)) {
      return this.reader;
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      final StringWriter out = new StringWriter();
      final Collection<String> attributeNames = this.reader.getRecordDefinition().getFieldNames();
      Csv.writeColumns(out, attributeNames, '\t', '\n');
      for (final Record object : this.reader) {
        if (object != null) {
          final Collection<Object> values = object.values();
          Csv.writeColumns(out, values, '\t', '\n');
        }
      }
      final String text = out.toString();
      return text;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return DATA_FLAVORS;
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor dataFlavor) {
    return Arrays.asList(DATA_FLAVORS).contains(dataFlavor);
  }

}
