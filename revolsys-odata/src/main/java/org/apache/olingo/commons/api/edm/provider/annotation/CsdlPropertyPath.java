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
 * The edm:PropertyPath expression provides a value for terms or term properties that specify the built-in
 * abstract type Edm.PropertyPath.
 */
public class CsdlPropertyPath extends CsdlDynamicExpression {

  private String value;

  @Override
  public boolean equals(final Object obj) {
    if (obj == null || !(obj instanceof CsdlPropertyPath)) {
      return false;
    }
    final CsdlPropertyPath csdlPropPath = (CsdlPropertyPath)obj;
    return this.getValue() == null ? csdlPropPath.getValue() == null
      : this.getValue().equals(csdlPropPath.getValue());
  }

  /**
   * Returns the property path itself.
   * @return the property path itself
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

  public CsdlPropertyPath setValue(final String value) {
    this.value = value;
    return this;
  }
}
