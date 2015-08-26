/*
 * $URL:$
 * $Author:$
 * $Date:$
 * $Revision:$

 * Copyright 2004-2007 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.data.record.filter;

import com.revolsys.data.record.Record;
import java.util.function.Predicate;
import com.vividsolutions.jts.geom.Geometry;

public class RecordGeometryFilter<G extends Geometry> implements Predicate<Record> {
  private Predicate<G> predicate;

  public RecordGeometryFilter() {
  }

  public RecordGeometryFilter(final Predicate<G> filter) {
    this.predicate = filter;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean test(final Record object) {
    final G geometry = (G)object.getGeometry();
    if (this.predicate.test(geometry)) {
      return true;
    } else {
      return false;
    }
  }

  public Predicate<G> getFilter() {
    return this.predicate;
  }

  public void setFilter(final Predicate<G> filter) {
    this.predicate = filter;
  }

}
