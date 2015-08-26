package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.data.equals.Equals;
import com.revolsys.swing.map.form.RecordLayerForm;
import com.revolsys.swing.map.layer.record.table.model.LayerRecordTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.util.Property;

public class FormAllFieldsModifiedPredicate implements HighlightPredicate {

  public static void add(final RecordLayerForm form, final BaseJTable table) {
    final LayerRecordTableModel model = table.getTableModel();
    final FormAllFieldsModifiedPredicate predicate = new FormAllFieldsModifiedPredicate(form,
      model);
    ModifiedAttributePredicate.addModifiedHighlighters(table, predicate);
  }

  private final Reference<RecordLayerForm> form;

  private final LayerRecordTableModel model;

  public FormAllFieldsModifiedPredicate(final RecordLayerForm form,
    final LayerRecordTableModel model) {
    this.form = new WeakReference<>(form);
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final String fieldName = this.model.getFieldName(rowIndex);
      if (fieldName != null) {
        final RecordLayerForm form = this.form.get();
        if (form.isFieldValid(fieldName)) {
          if (form.hasOriginalValue(fieldName)) {
            final Object fieldValue = form.getFieldValue(fieldName);
            final Object originalValue = form.getOriginalValue(fieldName);
            boolean equal = Equals.equal(originalValue, fieldValue);
            if (!equal) {
              if (originalValue == null) {
                if (fieldValue instanceof String) {
                  final String string = (String)fieldValue;
                  if (!Property.hasValue(string)) {
                    equal = true;
                  }
                }
              }
            }
            return !equal;
          }
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;

  }
}
