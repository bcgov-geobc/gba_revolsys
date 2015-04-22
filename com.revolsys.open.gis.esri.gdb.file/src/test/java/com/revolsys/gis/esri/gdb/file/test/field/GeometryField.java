package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.data.types.DataType;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.FieldProperties;
import com.revolsys.gis.esri.gdb.file.test.FgdbReader;
import com.revolsys.io.EndianInput;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryField extends FgdbField {
  public GeometryField(final String name, final DataType type,
    final boolean required, final GeometryFactory geometryFactory) {
    super(name, type, required);
    setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final Geometry geometry = null;
    final long length = FgdbReader.readVarUInt(in);
    in.read(new byte[(int)length]);
    return (T)geometry;
  }
}
