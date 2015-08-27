/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/com.revolsys/trunk/com.revolsys.gis/com.revolsys.gis.core/src/main/java/com/revolsys/gis/data/io/AbstractReader.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2010-04-08 08:43:16 -0700 (Thu, 08 Apr 2010) $
 * $Revision: 2377 $

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
package com.revolsys.io;

import java.util.function.Consumer;

import com.revolsys.properties.BaseObjectWithProperties;

/**
 * The AbstracteReader is an implementation of the {@link Reader} interface,
 * which provides implementations of {@link #read()} and {@link #visit(Consumer)}
 * which use the {@link Reader#iterator()} method which must be implemented by
 * subclasses.
 *
 * @author Paul Austin
 * @param <T> The type of object being read.
 */
public abstract class AbstractReader<T> extends BaseObjectWithProperties implements Reader<T> {
}
