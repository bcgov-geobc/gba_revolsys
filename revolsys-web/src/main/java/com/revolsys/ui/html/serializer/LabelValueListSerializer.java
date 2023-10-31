/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.serializer;

import com.revolsys.record.io.format.xml.XmlWriter;

public interface LabelValueListSerializer {

  default String getLabelCss(final int index) {
    return null;
  }

  int getSize();

  default String getValueCss(final int index) {
    return null;
  }

  void serializeLabel(XmlWriter out, int index);

  void serializeValue(XmlWriter out, int index);
}