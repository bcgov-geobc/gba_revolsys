package com.revolsys.data.record.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class TypeNamesFilter implements Predicate<Record> {

  private final Set<String> typePaths = new HashSet<String>();

  public TypeNamesFilter() {
  }

  public TypeNamesFilter(final String typePath) {
    this.typePaths.add(typePath);
  }

  /**
   * @param typePaths the typePaths to set
   */
  public void setTypeNames(final Set<Object> typePaths) {
    for (final Object name : typePaths) {
      final String typePath = name.toString();
      this.typePaths.add(typePath);
    }
  }

  @Override
  public boolean test(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    return this.typePaths.contains(typePath);
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    if (this.typePaths.size() == 1) {
      return "typePath=" + this.typePaths.iterator().next();
    } else {
      return "typePath in " + this.typePaths;
    }
  }

}
