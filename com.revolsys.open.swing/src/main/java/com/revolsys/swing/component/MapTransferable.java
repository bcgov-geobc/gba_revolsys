package com.revolsys.swing.component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.revolsys.io.csv.CsvUtil;

public class MapTransferable implements Transferable {

  public static final DataFlavor MAP_FLAVOR = new DataFlavor(Map.class,
    "Java Map");

  private final Map<String, Object> map;

  private static final DataFlavor[] DATA_FLAVORS = {
    MAP_FLAVOR, DataFlavor.stringFlavor
  };

  public MapTransferable(final Map<String, Object> map) {
    this.map = map;
  }

  public Map<String, Object> getMap() {
    return map;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (MAP_FLAVOR.equals(flavor)) {
      return map;
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      final StringWriter out = new StringWriter();
      final Collection<String> attributeNames = map.keySet();
      CsvUtil.writeColumns(out, attributeNames, '\t', '\n');
      final Collection<Object> values = map.values();
      CsvUtil.writeColumns(out, values, '\t', '\n');
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
