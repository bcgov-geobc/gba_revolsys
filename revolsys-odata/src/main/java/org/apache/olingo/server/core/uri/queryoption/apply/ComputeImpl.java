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
package org.apache.olingo.server.core.uri.queryoption.apply;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.uri.queryoption.apply.Compute;
import org.apache.olingo.server.api.uri.queryoption.apply.ComputeExpression;

/**
 * Represents the compute transformation.
 */
public class ComputeImpl implements Compute {

  private final List<ComputeExpression> expressions = new ArrayList<>();

  public ComputeImpl addExpression(final ComputeExpressionImpl expression) {
    this.expressions.add(expression);
    return this;
  }

  @Override
  public List<ComputeExpression> getExpressions() {
    return this.expressions;
  }

  @Override
  public Kind getKind() {
    return Kind.COMPUTE;
  }
}