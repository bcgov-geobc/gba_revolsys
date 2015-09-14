package com.revolsys.swing.table.record.model;

import java.util.List;
import java.util.Map;

import javax.swing.JTable;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.comparator.NumericComparator;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.renderer.SingleRecordTableCellRenderer;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractSingleRecordTableModel extends AbstractRecordTableModel {
  private static final String[] COLUMN_NAMES = {
    "#", "Name", "Value"
  };

  private static final long serialVersionUID = 1L;

  public static BaseJTable createTable(final AbstractSingleRecordTableModel model) {
    final BaseJTable table = new BaseJTable(model);
    table.setModel(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateColumnsFromModel(false);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    final SingleRecordTableCellRenderer cellRenderer = new SingleRecordTableCellRenderer();
    final RecordTableCellEditor cellEditor = new RecordTableCellEditor(table);

    final RecordDefinition recordDefinition = model.getRecordDefinition();

    final List<String> allFieldNames = recordDefinition.getFieldNames();
    int maxTitleWidth = 100;
    for (final String fieldName : allFieldNames) {
      final String title = model.getFieldTitle(fieldName);
      final int titleWidth = Math.max(title.length(), fieldName.length()) * 8;
      if (titleWidth > maxTitleWidth) {
        maxTitleWidth = titleWidth;
      }

    }

    final int columnCount = model.getColumnCount();
    int columnWidth;
    if (columnCount > 3) {
      columnWidth = (740 - maxTitleWidth) / 2;
    } else {
      columnWidth = (740 - maxTitleWidth) / 2;
    }
    for (int i = 0; i < columnCount; i++) {
      final TableColumnExt column = table.getColumnExt(i);
      column.setCellRenderer(cellRenderer);
      if (i == 0) {
        column.setMinWidth(40);
        column.setPreferredWidth(40);
        column.setMaxWidth(40);
        column.setComparator(new NumericComparator());
      } else if (i == 1) {
        column.setMinWidth(maxTitleWidth);
        column.setPreferredWidth(maxTitleWidth);
        column.setMaxWidth(maxTitleWidth);
      } else {
        column.setPreferredWidth(columnWidth);
        if (i == 2) {
          column.setCellEditor(cellEditor);
        }
      }
    }
    return table;
  }

  private List<String> fieldNames;

  public AbstractSingleRecordTableModel(final RecordDefinition recordDefinition,
    final boolean editable) {
    super(recordDefinition);
    setEditable(editable);
    setFieldNames(recordDefinition.getFieldNames());
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public String getFieldName(final int attributeIndex) {
    return this.fieldNames.get(attributeIndex);
  }

  @Override
  public String getFieldName(final int row, final int column) {
    return getFieldName(row);
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public String getFieldTitle(final String fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldTitle(fieldName);
  }

  public abstract <V extends Map<String, Object>> V getMap(int columnIndex);

  public abstract Object getObjectValue(final int attributeIndex, int columnIndex);

  @Override
  public int getRowCount() {
    return this.fieldNames.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return rowIndex;
      case 1:
        final String fieldName = getFieldName(rowIndex);
        final String title = getFieldTitle(fieldName);
        return title;
      case 2:
        return getObjectValue(rowIndex, columnIndex);
      default:
        return null;
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      if (isEditable()) {
        final RecordDefinition recordDefinition = getRecordDefinition();
        if (rowIndex == recordDefinition.getIdFieldIndex()) {
          return false;
        } else {
          final String fieldName = getFieldName(rowIndex);
          if (recordDefinition.getIdFieldNames().contains(fieldName)) {
            return false;
          } else if (recordDefinition.getGeometryFieldNames().contains(fieldName)) {
            return false;
          } else {
            return !isReadOnly(fieldName);
          }
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex, final int columnIndex) {
    return selected;
  }

  protected Object setDisplayValue(final String fieldName, final Object displayValue) {
    final Object objectValue = toObjectValue(fieldName, displayValue);
    return setObjectValue(fieldName, objectValue);
  }

  public void setFieldNames(final List<String> fieldNames) {
    if (fieldNames != null && (this.fieldNames == null || !this.fieldNames.equals(fieldNames))) {
      this.fieldNames = fieldNames;
      fireTableDataChanged();
    }
  }

  protected abstract Object setObjectValue(final String fieldName, final Object value);

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    super.setRecordDefinition(recordDefinition);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {
      final String fieldName = getFieldName(rowIndex);

      final Object oldValue = setDisplayValue(fieldName, value);
      firePropertyChange(fieldName, oldValue, value);
    }
  }

  @Override
  public String toCopyValue(final int rowIndex, final int columnIndex, final Object objectValue) {
    if (objectValue == null) {
      return null;
    } else if (columnIndex < 2) {
      return StringConverterRegistry.toString(objectValue);
    } else {
      String text;
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String idFieldName = recordDefinition.getIdFieldName();
      final String name = getFieldName(rowIndex);
      if (objectValue instanceof Geometry) {
        final Geometry geometry = (Geometry)objectValue;
        return geometry.toString();
      }
      CodeTable codeTable = null;
      if (!name.equals(idFieldName)) {
        codeTable = recordDefinition.getCodeTableByFieldName(name);
      }
      if (codeTable == null) {
        text = StringConverterRegistry.toString(objectValue);
      } else {
        final List<Object> values = codeTable.getValues(Identifier.create(objectValue));
        if (values == null || values.isEmpty()) {
          return null;
        } else {
          text = CollectionUtil.toString(values);
        }
      }
      if (text.length() == 0) {
        return null;
      }
      return text;
    }
  }
}
