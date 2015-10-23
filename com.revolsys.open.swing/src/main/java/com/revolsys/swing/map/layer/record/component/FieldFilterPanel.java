package com.revolsys.swing.map.layer.record.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXSearchField;
import org.slf4j.LoggerFactory;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.equals.Equals;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.BinaryCondition;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.Not;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.RightUnaryCondition;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.AbstractRecordQueryField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DateField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.QueryWhereConditionField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class FieldFilterPanel extends JComponent
  implements ActionListener, ItemListener, DocumentListener, PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private final List<String> fieldNames;

  private CodeTable codeTable;

  private final ComboBox codeTableOperatorField = new ComboBox("operator", "=", "<>", "IS NULL",
    "IS NOT NULL");

  private final ComboBox dateOperatorField = new ComboBox("operator", "=", "<>", "IS NULL",
    "IS NOT NULL", "<", "<=", ">", ">=");

  private final boolean eventsEnabled = true;

  private FieldDefinition fieldDefinition;

  private final ComboBox generalOperatorField = new ComboBox("operator", "=", "<>", "Like",
    "IS NULL", "IS NOT NULL");

  private final AbstractRecordLayer layer;

  private final RecordDefinition recordDefinition;

  private final ComboBox nameField;

  private final ComboBox numericOperatorField = new ComboBox("operator", "=", "<>", "IS NULL",
    "IS NOT NULL", "<", "<=", ">", ">=");

  private ComboBox operatorField;

  private final JPanel operatorFieldPanel = new JPanel();

  private String previousSearchFieldName;

  private JComponent searchField;

  private final JPanel searchFieldPanel = new JPanel();

  private final TextField searchTextField;

  private boolean settingFilter = false;

  private final RecordLayerTableModel tableModel;

  private final JLabel whereLabel;

  public FieldFilterPanel(final TablePanel tablePanel, final RecordLayerTableModel tableModel) {
    this.tableModel = tableModel;
    this.layer = tableModel.getLayer();
    this.recordDefinition = this.layer.getRecordDefinition();

    this.whereLabel = new JLabel();
    this.whereLabel.setMaximumSize(new Dimension(100, 250));
    this.whereLabel.setFont(SwingUtil.FONT);
    this.whereLabel.setOpaque(true);
    this.whereLabel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    this.whereLabel.setBackground(WebColors.White);
    add(this.whereLabel);

    this.fieldNames = new ArrayList<String>(this.layer.getFieldNames());
    this.fieldNames.remove(this.recordDefinition.getGeometryFieldName());
    final AttributeTitleStringConveter converter = new AttributeTitleStringConveter(this.layer);
    this.nameField = new ComboBox(converter, false, this.fieldNames.toArray());
    this.nameField.setRenderer(converter);
    this.nameField.addActionListener(this);
    add(this.nameField);

    add(this.operatorFieldPanel);

    this.searchTextField = new TextField(20);
    this.searchField = this.searchTextField;
    this.searchTextField.addActionListener(this);
    this.searchTextField.setPreferredSize(new Dimension(200, 22));
    add(this.searchFieldPanel);
    GroupLayouts.makeColumns(this, 4, false);
    clear();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (this.eventsEnabled) {
      try {
        final Object source = event.getSource();
        if (source == this.searchField) {
          updateCondition();
        } else if (source == this.nameField) {
          setSearchFieldName(getSearchFieldName());
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to search", e);
      }
    }
  }

  private void addListeners(final JComponent component) {
    if (component instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)component;
      queryField.addPropertyChangeListener("selectedRecord", this);
    } else if (component instanceof JXSearchField) {
      final JXSearchField searchTextField = (JXSearchField)component;
      searchTextField.addActionListener(this);
    } else if (component instanceof JTextComponent) {
      final JTextComponent searchTextField = (JTextComponent)component;
      searchTextField.getDocument().addDocumentListener(this);
    } else if (component instanceof DateField) {
      final DateField dateField = (DateField)component;
      dateField.addActionListener(this);
    }
    if (component instanceof Field) {
      final Field field = (Field)component;
      final String fieldName = field.getFieldName();
      Property.addListener(field, fieldName, this);
    }
  }

  @Override
  public void changedUpdate(final DocumentEvent event) {
    updateCondition();
  }

  public void clear() {
    try {
      this.settingFilter = true;
      String searchField = this.previousSearchFieldName;
      if (!Property.hasValue(searchField)) {
        searchField = this.layer.getProperty("searchField");
        if (!Property.hasValue(searchField)) {
          searchField = this.recordDefinition.getFieldNames().get(0);
        }
      }
      setSearchFieldName(searchField);
      setFilter(null);
    } finally {
      this.settingFilter = false;
    }
  }

  public void fireSearchChanged(final String propertyName, final Object oldValue,
    final Object newValue) {
    if (!Equals.equal(oldValue, newValue)) {
      if (SwingUtilities.isEventDispatchThread()) {
        final Method method = JavaBeanUtil.getMethod(getClass(), "fireSearchChanged", String.class,
          Object.class, Object.class);
        Invoke.background("Change search", this, method, propertyName, oldValue, newValue);
      } else {
        firePropertyChange(propertyName, oldValue, newValue);
      }
    }
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public Condition getFilter() {
    return this.tableModel.getFilter();
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  public String getSearchFieldName() {
    return (String)this.nameField.getSelectedItem();
  }

  public final String getSearchOperator() {
    if (this.operatorField == null) {
      return "=";
    } else {
      return (String)this.operatorField.getSelectedItem();
    }
  }

  public Object getSearchValue() {
    if (this.searchField instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)this.searchField;
      return textComponent.getText();
    } else {
      final Object value = SwingUtil.getValue(this.searchField);
      return value;
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent event) {
    updateCondition();
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (!this.settingFilter) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final Object source = e.getSource();
        if (source == this.nameField) {
          final String searchFieldName = getSearchFieldName();
          setSearchFieldName(searchFieldName);
        } else if (source == this.operatorField) {
          final String searchOperator = getSearchOperator();
          setSearchOperator(searchOperator);
        } else {
          updateCondition();
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getPropertyName().equals("filter")) {
      final Condition filter = (Condition)event.getNewValue();
      setFilter(filter);
    } else if (event.getSource() == this.searchField) {
      updateCondition();
    }
  }

  private void removeListeners(final JComponent component) {
    if (component instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)component;
      queryField.removePropertyChangeListener("selectedRecord", this);
    } else if (component instanceof JXSearchField) {
      final JXSearchField searchTextField = (JXSearchField)component;
      searchTextField.removeActionListener(this);
    } else if (component instanceof JComboBox) {
      final JComboBox comboField = (JComboBox)component;
      comboField.removeActionListener(this);
    } else if (component instanceof DateField) {
      final DateField dateField = (DateField)component;
      dateField.removeActionListener(this);
    }
    if (component instanceof Field) {
      final Field field = (Field)component;
      final String fieldName = field.getFieldName();
      Property.removeListener(field, fieldName, this);
    }
  }

  @Override
  public void removeUpdate(final DocumentEvent event) {
    updateCondition();
  }

  public void setFilter(final Condition filter) {
    try {
      this.settingFilter = true;
      setSearchFilter(filter);
      boolean simple = false;
      if (filter == null) {
        setSearchOperator("=");
        final Field searchField = (Field)this.searchField;
        if (searchField != null) {
          searchField.setFieldValue(null);
        }
        simple = true;
      } else if (filter instanceof Not) {
        final Not not = (Not)filter;
        final QueryValue condition = not.getQueryValue();
        if (condition instanceof IsNull) {
          final IsNull isNull = (IsNull)condition;
          final IsNotNull isNotNull = new IsNotNull(isNull.getValue());
          setFilter(isNotNull);
          return;
        }
      } else if (filter instanceof ILike) {
        final ILike equal = (ILike)filter;
        final QueryValue leftCondition = equal.getLeft();
        final QueryValue rightCondition = equal.getRight();

        if (leftCondition instanceof Column && rightCondition instanceof Value) {
          final Column column = (Column)leftCondition;
          final String searchFieldName = column.getName();
          setSearchFieldName(searchFieldName);

          if (setSearchOperator("Like")) {
            final Value value = (Value)rightCondition;
            final Object searchValue = value.getValue();
            String searchText = StringConverterRegistry.toString(searchValue);
            if (Property.hasValue(searchText)) {
              setSearchField(this.searchTextField);
              searchText = searchText.replaceAll("%", "");
              final String previousSearchText = this.searchTextField.getText();

              if (!Equals.equal(searchText, previousSearchText)) {
                this.searchTextField.setFieldValue(searchText);
              }
              simple = true;
            } else {
              setSearchFilter(null);
            }
          }
        }
      } else if (filter instanceof BinaryCondition) {
        final BinaryCondition condition = (BinaryCondition)filter;
        final QueryValue leftCondition = condition.getLeft();
        final QueryValue rightCondition = condition.getRight();

        if (leftCondition instanceof Column && rightCondition instanceof Value) {
          final Column column = (Column)leftCondition;
          final String searchFieldName = column.getName();
          setSearchFieldName(searchFieldName);

          final String searchOperator = condition.getOperator();
          if (setSearchOperator(searchOperator)) {
            final Value value = (Value)rightCondition;
            final Object searchValue = value.getValue();

            final String searchText = StringConverterRegistry.toString(searchValue);

            final Field searchField = (Field)this.searchField;
            final Object oldValue = searchField.getFieldValue();
            if (!searchText.equalsIgnoreCase(StringConverterRegistry.toString(oldValue))) {
              searchField.setFieldValue(searchText);
            }
            simple = true;
          }
        }
      } else if (filter instanceof RightUnaryCondition) {
        final RightUnaryCondition condition = (RightUnaryCondition)filter;
        final String operator = condition.getOperator();
        if (filter instanceof IsNull || filter instanceof IsNotNull) {
          final QueryValue leftValue = condition.getValue();
          if (leftValue instanceof Column) {
            final Column column = (Column)leftValue;
            final String searchFieldName = column.getName();
            setSearchFieldName(searchFieldName);

            if (setSearchOperator(operator)) {
              simple = true;
            }
          }
        }
      }
      if (simple) {
        this.whereLabel.setVisible(false);
        this.nameField.setVisible(true);
        this.operatorField.setVisible(true);
        this.searchFieldPanel.setVisible(true);
      } else {
        String filterText = filter.toString();
        if (filterText.length() > 40) {
          filterText = filterText.substring(0, 40) + "...";
        }
        this.whereLabel.setText(filterText);
        this.whereLabel.setToolTipText(filterText);
        this.whereLabel.setVisible(true);
        this.nameField.setVisible(false);
        this.operatorField.setVisible(false);
        this.searchFieldPanel.setVisible(false);
      }
    } finally {
      this.settingFilter = false;
    }
  }

  private void setOperatorField(final ComboBox field) {
    if (field != this.operatorField) {
      final String operator = getSearchOperator();
      if (this.operatorField != null) {
        this.operatorField.removeItemListener(this);
      }
      this.operatorFieldPanel.removeAll();
      this.operatorField = field;
      if (field != null) {
        this.operatorField.setSelectedIndex(0);
        if (operator != null) {
          this.operatorField.setSelectedItem(operator);
        }
        this.operatorField.addItemListener(this);
        this.operatorFieldPanel.add(field);
        GroupLayouts.makeColumns(this.operatorFieldPanel, 1, false);
      }
    }
  }

  private void setSearchField(final JComponent searchField) {
    this.searchFieldPanel.removeAll();
    removeListeners(this.searchField);
    this.searchField = searchField;
    if (searchField == null) {
      this.searchFieldPanel.setVisible(false);
    } else {
      this.searchFieldPanel.setVisible(true);
      addListeners(searchField);
      if (this.searchField instanceof AbstractRecordQueryField) {
        final AbstractRecordQueryField recordQueryField = (AbstractRecordQueryField)this.searchField;
        recordQueryField.setPreferredSize(new Dimension(200, 22));
      } else if (this.searchField instanceof JXSearchField) {
        final JXSearchField searchTextField = (JXSearchField)this.searchField;
        searchTextField.setPreferredSize(new Dimension(200, 22));
      }
      this.searchFieldPanel.add(this.searchField);
      GroupLayouts.makeColumns(this.searchFieldPanel, 1, false);
    }
  }

  private void setSearchFieldName(final String searchFieldName) {
    if (!Equals.equal(searchFieldName, this.previousSearchFieldName)) {
      this.previousSearchFieldName = searchFieldName;
      this.layer.setProperty("searchField", searchFieldName);
      final RecordDefinition recordDefinition = this.tableModel.getRecordDefinition();
      this.fieldDefinition = recordDefinition.getField(searchFieldName);
      final Class<?> attributeClass = this.fieldDefinition.getTypeClass();
      if (!Equals.equal(searchFieldName, this.nameField.getSelectedItem())) {
        this.nameField.setFieldValue(searchFieldName);
      }
      if (searchFieldName.equals(recordDefinition.getIdFieldName())) {
        this.codeTable = null;
      } else {
        this.codeTable = this.recordDefinition.getCodeTableByFieldName(searchFieldName);
      }

      ComboBox operatorField;
      if (this.codeTable != null) {
        operatorField = this.codeTableOperatorField;
      } else if (Number.class.isAssignableFrom(attributeClass)) {
        operatorField = this.numericOperatorField;
      } else if (Date.class.isAssignableFrom(attributeClass)) {
        operatorField = this.dateOperatorField;
      } else {
        operatorField = this.generalOperatorField;
      }
      setOperatorField(operatorField);

      setSearchOperator("=");
      if (!this.settingFilter) {
        setSearchFilter(null);
      }
    }
  }

  private boolean setSearchFilter(final Condition filter) {
    return this.tableModel.setFilter(filter);
  }

  private boolean setSearchOperator(final String searchOperator) {
    final Object currentSearchOperator = this.operatorField.getSelectedItem();
    if (!Equals.equal(searchOperator, currentSearchOperator)) {
      this.operatorField.setSelectedItem(searchOperator);
    }
    if (this.operatorField.getSelectedIndex() < 0) {
      return false;
    } else {
      JComponent searchField = null;
      if ("Like".equals(searchOperator)) {
        this.codeTable = null;
        searchField = this.searchTextField;
      } else if ("IS NULL".equals(searchOperator)) {
        if (!this.settingFilter) {
          setFilter(Q.isNull(this.fieldDefinition));
        }
        this.codeTable = null;
      } else if ("IS NOT NULL".equals(searchOperator)) {
        if (!this.settingFilter) {
          setFilter(Q.isNotNull(this.fieldDefinition));
        }
        this.codeTable = null;
      } else {
        searchField = QueryWhereConditionField.createSearchField(this.layer, this.fieldDefinition,
          this.codeTable);
      }

      setSearchField(searchField);
      return true;
    }
  }

  public void showAdvancedFilter() {
    final Condition filter = getFilter();
    final QueryWhereConditionField advancedFilter = new QueryWhereConditionField(this.layer, this,
      filter);
    advancedFilter.showDialog(this);
  }

  public void updateCondition() {
    if (this.eventsEnabled) {
      final Object searchValue = getSearchValue();
      Condition condition = null;
      final String searchOperator = getSearchOperator();
      if ("IS NULL".equalsIgnoreCase(searchOperator)) {
        condition = Q.isNull(this.fieldDefinition);
      } else if ("IS NOT NULL".equalsIgnoreCase(searchOperator)) {
        condition = Q.isNotNull(this.fieldDefinition);
      } else if (this.fieldDefinition != null) {
        if (Property.hasValue(StringConverterRegistry.toString(searchValue))) {
          if ("Like".equalsIgnoreCase(searchOperator)) {
            final String searchText = StringConverterRegistry.toString(searchValue);
            if (Property.hasValue(searchText)) {
              condition = Q.iLike(this.fieldDefinition, "%" + searchText + "%");
            }
          } else {
            Object value = null;
            if (this.codeTable == null) {
              try {
                final Class<?> attributeClass = this.fieldDefinition.getTypeClass();
                value = StringConverterRegistry.toObject(attributeClass, searchValue);
              } catch (final Throwable t) {
                return;
              }
            } else {
              value = this.codeTable.getIdValue(searchValue);
              if (value == null) {
                return;
              }
            }
            if (value != null) {
              condition = Q.binary(this.fieldDefinition, searchOperator, value);
            }
          }
        }
      }
      setSearchFilter(condition);
    }
  }
}
