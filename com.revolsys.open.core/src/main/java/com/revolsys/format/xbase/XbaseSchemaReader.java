package com.revolsys.format.xbase;

import java.io.IOException;
import java.util.List;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.io.EndianInput;

public class XbaseSchemaReader {
  private final List<FieldDefinition> fieldDefinitions;

  private final EndianInput in;

  private RecordDefinitionImpl metaData;

  private final String typePath;

  public XbaseSchemaReader(final EndianInput in, final String typePath,
    final List<FieldDefinition> fieldDefinitions) {
    this.in = in;
    this.typePath = typePath;
    this.fieldDefinitions = fieldDefinitions;
  }

  protected RecordDefinition getMetaData() throws IOException {
    if (this.metaData == null) {
      this.metaData = new RecordDefinitionImpl(this.typePath);
      int b = this.in.read();
      while (b != 0x0D) {
        final StringBuffer fieldName = new StringBuffer();
        boolean endOfName = false;
        for (int i = 0; i < 11; i++) {
          if (!endOfName && b != 0) {
            fieldName.append((char)b);
          } else {

            endOfName = true;
          }
          if (i != 10) {
            b = this.in.read();
          }
        }
        final char fieldType = (char)this.in.read();
        this.in.skipBytes(4);
        final int length = this.in.read();
        this.in.skipBytes(15);
        b = this.in.read();
        final FieldDefinition field = new FieldDefinition(fieldName.toString(),
          fieldName.toString(), fieldType, length);
        if (this.fieldDefinitions != null) {
          this.fieldDefinitions.add(field);
        }
        this.metaData.addField(fieldName.toString(), field.getDataType(), length, true);
      }
    }
    return this.metaData;
  }

}
