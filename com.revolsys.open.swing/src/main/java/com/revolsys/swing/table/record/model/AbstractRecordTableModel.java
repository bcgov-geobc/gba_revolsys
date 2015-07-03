package com.revolsys.swing.table.record.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.swing.table.AbstractTableModel;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractRecordTableModel extends AbstractTableModel
  implements PropertyChangeSupportProxy {

  private static final long serialVersionUID = 1L;

  private RecordDefinition recordDefinition;

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private boolean editable;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public AbstractRecordTableModel() {
    this(null);
  }

  public AbstractRecordTableModel(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    Property.addListener(this.propertyChangeSupport, propertyChangeListener);
  }

  public void addReadOnlyFieldNames(final String... readOnlyFieldNames) {
    if (readOnlyFieldNames != null) {
      this.readOnlyFieldNames.addAll(Arrays.asList(readOnlyFieldNames));
    }
  }

  @PreDestroy
  public void dispose() {
    this.recordDefinition = null;
  }

  protected void firePropertyChange(final PropertyChangeEvent event) {
    this.propertyChangeSupport.firePropertyChange(event);
  }

  protected void firePropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    this.propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
  }

  protected void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  public String getFieldName(final int attributeIndex) {
    final RecordDefinition metaData = getRecordDefinition();
    return metaData.getFieldName(attributeIndex);
  }

  public abstract String getFieldName(int rowIndex, int columnIndex);

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public Set<String> getReadOnlyFieldNames() {
    return this.readOnlyFieldNames;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public boolean isEditable() {
    return this.editable;
  }

  public boolean isReadOnly(final String attributeName) {
    return this.readOnlyFieldNames.contains(attributeName);
  }

  public abstract boolean isSelected(boolean selected, int rowIndex, int columnIndex);

  public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
    Property.removeListener(this.propertyChangeSupport, propertyChangeListener);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  protected void setMetaData(final RecordDefinition metaData) {
    if (metaData != this.recordDefinition) {
      this.recordDefinition = metaData;
      fireTableStructureChanged();
    }
  }

  public void setReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    if (readOnlyFieldNames == null) {
      this.readOnlyFieldNames = new HashSet<String>();
    } else {
      this.readOnlyFieldNames = new HashSet<String>(readOnlyFieldNames);
    }
  }

  public String toDisplayValue(final int rowIndex, final int attributeIndex,
    final Object objectValue) {
    String text;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String idFieldName = recordDefinition.getIdFieldName();
    final String name = getFieldName(attributeIndex);
    if (objectValue == null) {
      if (name.equals(idFieldName)) {
        return "NEW";
      } else {
        text = "-";
      }
    } else {
      if (objectValue instanceof Geometry) {
        final Geometry geometry = (Geometry)objectValue;
        return geometry.getGeometryType();
      }
      CodeTable codeTable = null;
      if (!name.equals(idFieldName)) {
        codeTable = recordDefinition.getCodeTableByFieldName(name);
      }
      if (codeTable == null) {
        text = StringConverterRegistry.toString(objectValue);
      } else {
        final List<Object> values = codeTable.getValues(objectValue);
        if (values == null || values.isEmpty()) {
          text = StringConverterRegistry.toString(objectValue);
        } else {
          text = CollectionUtil.toString(values);
        }
      }
      if (text.length() == 0) {
        text = "-";
      }
    }
    return text;
  }

  public Object toObjectValue(final int attributeIndex, final Object displayValue) {
    if (displayValue == null) {
      return null;
    }
    if (displayValue instanceof String) {
      final String string = (String)displayValue;
      if (!Property.hasValue(string)) {
        return null;
      }
    }
    final RecordDefinition metaData = getRecordDefinition();
    final String name = getFieldName(attributeIndex);
    final CodeTable codeTable = metaData.getCodeTableByFieldName(name);
    if (codeTable == null) {
      final Class<?> fieldClass = metaData.getFieldClass(name);
      final Object objectValue = StringConverterRegistry.toObject(fieldClass, displayValue);
      return objectValue;
    } else {
      final Object objectValue = codeTable.getId(displayValue);
      return objectValue;
    }
  }

}
