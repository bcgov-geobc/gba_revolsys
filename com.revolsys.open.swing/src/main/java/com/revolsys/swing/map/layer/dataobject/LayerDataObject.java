package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.gis.model.data.equals.EqualsInstance;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.Property;

public class LayerDataObject extends ArrayRecord {
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  private Map<String, Object> originalValues;

  public LayerDataObject(final AbstractRecordLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
  }

  /**
   * Internal method to revert the records values to the original 
   */
  protected synchronized void cancelChanges() {
    RecordState newState = getState();
    if (newState != RecordState.New) {
      newState = RecordState.Persisted;
    }
    setState(RecordState.Initalizing);

    if (this.originalValues != null) {
      super.setValues(this.originalValues);
    }
    this.originalValues = null;
    setState(newState);
  }

  public void firePropertyChange(final String attributeName,
    final Object oldValue, final Object newValue) {
    final AbstractRecordLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this,
        attributeName, oldValue, newValue);
      layer.propertyChange(event);
    }
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String name) {
    if (this.originalValues != null) {
      if (this.originalValues.containsKey(name)) {
        return (T)this.originalValues.get(name);
      }
    }
    return (T)getValue(name);
  }

  public boolean isDeletable() {
    if (this.layer.isCanDeleteRecords()) {
      return !isDeleted();
    }
    return false;
  }

  public boolean isDeleted() {
    return getState() == RecordState.Deleted;
  }

  public boolean isGeometryEditable() {
    return true;
  }

  @Override
  public boolean isModified() {
    return super.isModified();
  }

  public boolean isModified(final int index) {
    if (this.originalValues == null) {
      return false;
    } else {
      final String attributeName = getRecordDefinition().getFieldName(index);
      return isModified(attributeName);
    }
  }

  public boolean isModified(final String name) {
    if (this.originalValues == null) {
      return false;
    } else {
      return this.originalValues.containsKey(name);
    }
  }

  public boolean isSame(final LayerDataObject record) {
    if (record == null) {
      return false;
    } else if (this == record) {
      return true;
    } else {
      final AbstractRecordLayer layer = getLayer();
      if (layer.isLayerRecord(record)) {
        final Object id = getIdValue();
        final Object otherId = record.getIdValue();
        if (id == null || otherId == null) {
          return false;
        } else if (EqualsRegistry.equal(id, otherId)) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean isValid(final int index) {
    if (getState() == RecordState.Initalizing) {
      return true;
    } else {
      final RecordDefinition metaData = getRecordDefinition();
      final String name = metaData.getFieldName(index);
      return isValid(name);
    }
  }

  @Override
  public boolean isValid(final String name) {
    if (getState() == RecordState.Initalizing) {
      return true;
    } else {
      final FieldDefinition attribute = getRecordDefinition().getField(name);
      if (attribute != null && attribute.isRequired()) {
        final Object value = getValue(name);
        if (value == null || value instanceof String
          && !StringUtils.hasText((String)value)) {
          return false;
        }
      }
      return true;
    }
  }

  public LayerDataObject revertChanges() {
    if (this.originalValues != null || getState() == RecordState.Deleted) {
      cancelChanges();
      final AbstractRecordLayer layer = getLayer();
      layer.revertChanges(this);
      firePropertyChange("state", RecordState.Modified,
        RecordState.Persisted);
    }
    return this;
  }

  public void revertEmptyFields() {
    for (final String fieldName : getRecordDefinition().getFieldNames()) {
      final Object value = getValue(fieldName);
      if (Property.isEmpty(value)) {
        if (!layer.isFieldUserReadOnly(fieldName)) {
          final Object originalValue = getOriginalValue(fieldName);
          if (!Property.isEmpty(originalValue)) {
            setValue(fieldName, originalValue);
          }
        }
      }
    }
  }

  @Override
  public void setValue(final int index, final Object value) {
    final RecordDefinition metaData = getRecordDefinition();
    final String attributeName = metaData.getFieldName(index);

    final Object oldValue = getValue(index);
    if (!EqualsInstance.INSTANCE.equals(oldValue, value)) {
      final AbstractRecordLayer layer = getLayer();
      final RecordState state = getState();
      if (RecordState.Initalizing.equals(state)) {
        // Allow modification on initialization
      } else if (RecordState.New.equals(state)) {
        if (!layer.isCanAddRecords()) {
          throw new IllegalStateException(
            "Adding new objects is not supported for layer " + layer);
        }
      } else if (RecordState.Deleted.equals(state)) {
        throw new IllegalStateException(
          "Cannot edit a deleted object for layer " + layer);
      } else {
        if (layer.isCanEditRecords()) {
          final Object originalValue = getOriginalValue(attributeName);
          if (EqualsRegistry.equal(value, originalValue)) {
            if (this.originalValues != null) {
              this.originalValues.remove(attributeName);
              if (this.originalValues.isEmpty()) {
                this.originalValues = null;
                setState(RecordState.Persisted);
              }
            }
          } else {
            if (this.originalValues == null) {
              this.originalValues = new HashMap<String, Object>();
            }
            this.originalValues.put(attributeName, originalValue);
          }
        } else {
          throw new IllegalStateException(
            "Editing objects is not supported for layer " + layer);
        }
      }
      super.setValue(index, value);
      if (!RecordState.Initalizing.equals(state)) {
        firePropertyChange(attributeName, oldValue, value);
        layer.updateRecordState(this);
      }
    }
  }
}
