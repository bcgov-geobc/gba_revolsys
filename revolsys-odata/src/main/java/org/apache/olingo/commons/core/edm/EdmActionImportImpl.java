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
package org.apache.olingo.commons.core.edm;

import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmActionImport;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;

public class EdmActionImportImpl extends AbstractEdmOperationImport implements EdmActionImport {

  private final CsdlActionImport actionImport;

  public EdmActionImportImpl(final Edm edm, final EdmEntityContainer container,
    final CsdlActionImport actionImport) {

    super(edm, container, actionImport);
    this.actionImport = actionImport;
  }

  @Override
  public EdmAction getUnboundAction() {
    return this.edm.getUnboundAction(this.actionImport.getActionFQN());
  }
}
