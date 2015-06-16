package com.revolsys.swing.table.record.editor;

import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.revolsys.awt.WebColors;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.AbstractRecordQueryField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.listener.Listener;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.record.model.AbstractRecordTableModel;

public class RecordTableCellEditor extends AbstractCellEditor implements TableCellEditor,
  KeyListener, MouseListener, TableModelListener {

  private static final long serialVersionUID = 1L;

  private JComponent editorComponent;

  private String attributeName;

  private final BaseJxTable table;

  private int rowIndex;

  private Object oldValue;

  private int columnIndex;

  private PopupMenu popupMenu = null;

  private DataType dataType;

  private MouseListener mouseListener;

  public RecordTableCellEditor(final BaseJxTable table) {
    this.table = table;
    table.getModel().addTableModelListener(this);
  }

  public synchronized void addMouseListener(final MouseListener l) {
    if (l != null) {
      this.mouseListener = AWTEventMulticaster.add(this.mouseListener, l);
    }
  }

  protected Field createField(final String fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return SwingUtil.createField(recordDefinition, fieldName, true);
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  @Override
  public Object getCellEditorValue() {
    final Object value = SwingUtil.getValue(this.editorComponent);
    return value;
  }

  public int getColumnIndex() {
    return this.columnIndex;
  }

  public JComponent getEditorComponent() {
    return this.editorComponent;
  }

  public Object getOldValue() {
    return this.oldValue;
  }

  protected RecordDefinition getRecordDefinition() {
    final AbstractRecordTableModel tableModel = getTableModel();
    return tableModel.getRecordDefinition();
  }

  public int getRowIndex() {
    return this.rowIndex;
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, int rowIndex, int columnIndex) {
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      rowIndex = jxTable.convertRowIndexToModel(rowIndex);
      columnIndex = jxTable.convertColumnIndexToModel(columnIndex);
    }
    this.oldValue = value;
    final AbstractRecordTableModel model = (AbstractRecordTableModel)table.getModel();
    this.attributeName = model.getFieldName(rowIndex, columnIndex);
    final RecordDefinition metaData = model.getRecordDefinition();
    this.dataType = metaData.getFieldType(this.attributeName);
    this.editorComponent = (JComponent)SwingUtil.createField(metaData, this.attributeName, true);
    if (this.editorComponent instanceof JTextField) {
      final JTextField textField = (JTextField)this.editorComponent;
      textField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(WebColors.LightSteelBlue),
        BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    } else if (this.editorComponent instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)this.editorComponent;
      queryField.setSearchFieldBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(WebColors.LightSteelBlue),
        BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    }
    this.editorComponent.setOpaque(false);
    SwingUtil.setFieldValue(this.editorComponent, value);

    this.rowIndex = rowIndex;
    this.columnIndex = columnIndex;
    this.editorComponent.addKeyListener(this);
    this.editorComponent.addMouseListener(this);
    if (this.editorComponent instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)this.editorComponent;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component comboEditorComponent = editor.getEditorComponent();
      comboEditorComponent.addKeyListener(this);
      comboEditorComponent.addMouseListener(this);
    }
    if (this.popupMenu != null) {
      this.popupMenu.addToComponent(this.editorComponent);
    }
    return this.editorComponent;
  }

  protected AbstractRecordTableModel getTableModel() {
    return (AbstractRecordTableModel)this.table.getModel();
  }

  @Override
  public boolean isCellEditable(final EventObject event) {
    if (event == null) {
      return true;
    } else {
      if (event instanceof MouseEvent) {
        final MouseEvent mouseEvent = (MouseEvent)event;
        if (SwingUtil.isLeftButtonAndNoModifiers(mouseEvent)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ENTER) {
      if (SwingUtil.isShiftDown(event)) {
        this.table.editCell(this.rowIndex - 1, this.columnIndex);
      } else {
        this.table.editCell(this.rowIndex + 1, this.columnIndex);
      }
      event.consume();
    } else if (keyCode == KeyEvent.VK_TAB) {
      if (SwingUtil.isShiftDown(event)) {
        this.table.editCell(this.rowIndex, this.columnIndex - 1);
      } else {
        this.table.editCell(this.rowIndex, this.columnIndex + 1);
      }
      event.consume();
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    Listener.mouseEvent(this.mouseListener, e);
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    Listener.mouseEvent(this.mouseListener, e);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    Listener.mouseEvent(this.mouseListener, e);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    Listener.mouseEvent(this.mouseListener, e);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    Listener.mouseEvent(this.mouseListener, e);
  }

  public synchronized void removeMouseListener(final MouseListener l) {
    if (l != null) {
      this.mouseListener = AWTEventMulticaster.remove(this.mouseListener, l);
    }
  }

  public void setPopupMenu(final MenuFactory menu) {
    setPopupMenu(new PopupMenu(menu));
  }

  public void setPopupMenu(final PopupMenu popupMenu) {
    this.popupMenu = popupMenu;
    popupMenu.setAutoCreateDnd(false);
  }

  @Override
  public boolean stopCellEditing() {
    boolean stopped = false;
    try {
      if (this.editorComponent instanceof Field) {
        final Field field = (Field)this.editorComponent;
        field.updateFieldValue();
      }
      stopped = super.stopCellEditing();
    } catch (final IndexOutOfBoundsException e) {
      return true;
    } catch (final Throwable t) {
      t.printStackTrace();
      final int result = JOptionPane.showConfirmDialog(this.editorComponent, "<html><p><b>'"
        + getCellEditorValue() + "' is not a valid " + this.dataType
        + ".</b></p><p>Discard changes (Yes) or edit field (No).</p></html>", "Invalid value",
        JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
      if (result == JOptionPane.YES_OPTION) {
        cancelCellEditing();
        return true;
      } else {
        return false;
      }
    } finally {
      if (stopped) {
        if (this.editorComponent != null) {
          this.editorComponent.removeMouseListener(this);
          this.editorComponent.removeKeyListener(this);
          if (this.editorComponent instanceof JComboBox) {
            final JComboBox comboBox = (JComboBox)this.editorComponent;
            final ComboBoxEditor editor = comboBox.getEditor();
            final Component comboEditorComponent = editor.getEditorComponent();
            comboEditorComponent.removeKeyListener(this);
            comboEditorComponent.removeMouseListener(this);
          }
          if (this.popupMenu != null) {
            PopupMenu.removeFromComponent(this.editorComponent);
          }
        }
      }
    }
    return stopped;
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    if (e.getFirstRow() <= this.rowIndex && this.rowIndex <= e.getLastRow()) {
      if (e.getColumn() == TableModelEvent.ALL_COLUMNS) {
        cancelCellEditing();
      }
    }
  }
}
