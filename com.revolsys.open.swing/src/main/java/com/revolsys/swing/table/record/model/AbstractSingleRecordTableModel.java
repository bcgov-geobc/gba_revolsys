package com.revolsys.swing.table.record.model;

import javax.swing.JTable;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.comparator.NumericComparator;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.renderer.SingleRecordTableCellRenderer;

public abstract class AbstractSingleRecordTableModel extends AbstractRecordTableModel {
  private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_NAMES = {
    "#", "Name", "Value"
  };

  public static BaseJxTable createTable(final AbstractSingleRecordTableModel model) {
    final BaseJxTable table = new BaseJxTable(model);
    table.setModel(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateColumnsFromModel(false);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    final SingleRecordTableCellRenderer cellRenderer = new SingleRecordTableCellRenderer();
    final RecordTableCellEditor cellEditor = new RecordTableCellEditor(table);

    final RecordDefinition metaData = model.getRecordDefinition();

    int maxTitleWidth = 100;
    for (final String fieldName : metaData.getFieldNames()) {
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

  public AbstractSingleRecordTableModel(final RecordDefinition metaData, final boolean editable) {
    super(metaData);
    setEditable(editable);
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
  public String getFieldName(final int row, final int column) {
    return getFieldName(row);
  }

  public String getFieldTitle(final String fieldName) {
    final RecordDefinition metaData = getRecordDefinition();
    return metaData.getFieldTitle(fieldName);
  }

  public abstract Object getObjectValue(final int attributeIndex);

  @Override
  public int getRowCount() {
    final RecordDefinition metaData = getRecordDefinition();
    final int attributeCount = metaData.getFieldCount();
    return attributeCount;
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
        return getObjectValue(rowIndex);
      default:
        return null;
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      if (isEditable()) {
        final RecordDefinition metaData = getRecordDefinition();
        if (rowIndex == metaData.getIdFieldIndex()) {
          return false;
        } else {
          final String attributeName = getFieldName(rowIndex);
          return !isReadOnly(attributeName);
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

  protected Object setDisplayValue(final int attributeIndex, final Object displayValue) {
    final Object objectValue = toObjectValue(attributeIndex, displayValue);
    return setObjectValue(attributeIndex, objectValue);
  }

  @Override
  public void setMetaData(final RecordDefinition metaData) {
    super.setMetaData(metaData);
  }

  protected abstract Object setObjectValue(final int attributeIndex, final Object value);

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    if (isCellEditable(rowIndex, columnIndex)) {

      final Object oldValue = setDisplayValue(rowIndex, value);
      final String propertyName = getFieldName(rowIndex);
      firePropertyChange(propertyName, oldValue, value);
    }
  }
}
