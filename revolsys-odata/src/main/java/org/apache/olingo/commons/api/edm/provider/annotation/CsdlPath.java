/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.commons.api.edm.provider.annotation;

/**
 *  The edm:Path expression enables a value to be obtained by traversing an object graph.
 *  It can be used in annotations that target entity containers, entity sets, entity types, complex types,
 *  navigation properties of structured types, and properties of structured types.
 */
public class CsdlPath extends CsdlDynamicExpression {

  private String value;

  @Override
  public boolean equals(final Object obj) {
    if (obj == null || !(obj instanceof CsdlPath)) {
      return false;
    }
    final CsdlPath csdlPath = (CsdlPath)obj;
    return this.getValue() == null ? csdlPath.getValue() == null
      : this.getValue().equals(csdlPath.getValue());
  }

  /**
   * Returns the target value of the expression
   *
   * @return target value of the expression
   */
  public String getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.value == null ? 0 : this.value.hashCode());
    return result;
  }

  public CsdlPath setValue(final String value) {
    this.value = value;
    return this;
  }
}
