package com.revolsys.gis.data.model.filter;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import com.revolsys.data.record.Record;

public class DataObjectAccessor implements PropertyAccessor {

  @SuppressWarnings("serial")
  private static class DataObjectAccessException extends AccessException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String key;

    public DataObjectAccessException(final String key) {
      super(null);
      this.key = key;
    }

    @Override
    public String getMessage() {
      return "DataObject does not contain a value for key '" + this.key + "'";
    }
  }

  @Override
  public boolean canRead(final EvaluationContext context, final Object target, final String name)
    throws AccessException {
    final Record object = (Record)target;
    return object.hasAttribute(name);
  }

  @Override
  public boolean canWrite(final EvaluationContext context, final Object target, final String name)
    throws AccessException {
    return true;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Class[] getSpecificTargetClasses() {
    return new Class[] {
      Record.class
    };
  }

  @Override
  public TypedValue read(final EvaluationContext context, final Object target, final String name)
    throws AccessException {
    final Record object = (Record)target;
    final Object value = object.getValue(name);
    if (value == null && !object.hasAttribute(name)) {
      throw new DataObjectAccessException(name);
    }
    return new TypedValue(value);
  }

  @Override
  public void write(final EvaluationContext context, final Object target, final String name,
    final Object newValue) throws AccessException {
    final Record object = (Record)target;
    object.setValue(name, newValue);
  }

}
