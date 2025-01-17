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
package org.apache.olingo.commons.api.edm.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;

/**
 * The type Csdl parameter.
 */
public class CsdlParameter implements CsdlAbstractEdmItem, CsdlNamed, CsdlAnnotatable {

  private String name;

  private String type;

  private boolean isCollection;

  private CsdlMapping mapping;

  // Facets
  private boolean nullable = true;

  private Integer maxLength;

  private Integer precision;

  private Integer scale;

  private SRID srid;

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * Gets mapping.
   *
   * @return the mapping
   */
  public CsdlMapping getMapping() {
    return this.mapping;
  }

  /**
   * Gets max length.
   *
   * @return the max length
   */
  public Integer getMaxLength() {
    return this.maxLength;
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Gets precision.
   *
   * @return the precision
   */
  public Integer getPrecision() {
    return this.precision;
  }

  /**
   * Gets scale.
   *
   * @return the scale
   */
  public Integer getScale() {
    return this.scale;
  }

  /**
   * Gets srid.
   *
   * @return the srid
   */
  public SRID getSrid() {
    return this.srid;
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public String getType() {
    return this.type;
  }

  /**
   * Gets type fQN.
   *
   * @return the type fQN
   */
  public FullQualifiedName getTypeFQN() {
    return new FullQualifiedName(this.type);
  }

  /**
   * Is collection.
   *
   * @return the boolean
   */
  public boolean isCollection() {
    return this.isCollection;
  }

  /**
   * Is nullable.
   *
   * @return the boolean
   */
  public boolean isNullable() {
    return this.nullable;
  }

  /**
   * Sets a list of annotations
   * @param annotations list of annotations
   * @return this instance
   */
  public CsdlParameter setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  /**
   * Sets collection.
   *
   * @param isCollection the is collection
   * @return the collection
   */
  public CsdlParameter setCollection(final boolean isCollection) {
    this.isCollection = isCollection;
    return this;
  }

  /**
   * Sets mapping.
   *
   * @param mapping the mapping
   * @return the mapping
   */
  public CsdlParameter setMapping(final CsdlMapping mapping) {
    this.mapping = mapping;
    return this;
  }

  /**
   * Sets max length.
   *
   * @param maxLength the max length
   * @return the max length
   */
  public CsdlParameter setMaxLength(final Integer maxLength) {
    this.maxLength = maxLength;
    return this;
  }

  /**
   * Sets name.
   *
   * @param name the name
   * @return the name
   */
  public CsdlParameter setName(final String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets nullable.
   *
   * @param nullable the nullable
   * @return the nullable
   */
  public CsdlParameter setNullable(final boolean nullable) {
    this.nullable = nullable;
    return this;
  }

  /**
   * Sets precision.
   *
   * @param precision the precision
   * @return the precision
   */
  public CsdlParameter setPrecision(final Integer precision) {
    this.precision = precision;
    return this;
  }

  /**
   * Sets scale.
   *
   * @param scale the scale
   * @return the scale
   */
  public CsdlParameter setScale(final Integer scale) {
    this.scale = scale;
    return this;
  }

  /**
   * Sets srid.
   *
   * @param srid the srid
   * @return the srid
   */
  public CsdlParameter setSrid(final SRID srid) {
    this.srid = srid;
    return this;
  }

  /**
   * Sets type.
   *
   * @param type the type
   * @return the type
   */
  public CsdlParameter setType(final FullQualifiedName type) {
    this.type = type.getFullQualifiedNameAsString();
    return this;
  }

  /**
   * Sets type.
   *
   * @param type the type
   * @return the type
   */
  public CsdlParameter setType(final String type) {
    this.type = type;
    return this;
  }
}
