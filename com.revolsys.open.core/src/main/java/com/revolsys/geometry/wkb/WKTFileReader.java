/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.geometry.wkb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;

/**
 * Reads a sequence of {@link Geometry}s in WKT format
 * from a text file.
 * The geometries in the file may be separated by any amount
 * of whitespace and newlines.
 *
 * @author Martin Davis
 *
 */
public class WKTFileReader {
  private static final int MAX_LOOKAHEAD = 1000;

  private int count = 0;

  private File file = null;

  private int limit = -1;

  private int offset = 0;

  private Reader reader;

  // private Reader fileReader = new FileReader(file);
  private final WKTReader wktReader;

  /**
   * Creates a new <tt>WKTFileReader</tt> given the <tt>File</tt> to read from
   * and a <tt>WKTReader</tt> to use to parse the geometries.
   *
   * @param file the <tt>File</tt> to read from
   * @param wktReader the geometry reader to use
   */
  public WKTFileReader(final File file, final WKTReader wktReader) {
    this.file = file;
    this.wktReader = wktReader;
  }

  /**
   * Creates a new <tt>WKTFileReader</tt>, given a {@link Reader} to read from.
   *
   * @param reader the reader to read from
   * @param wktReader the geometry reader to use
   */
  public WKTFileReader(final Reader reader, final WKTReader wktReader) {
    this.reader = reader;
    this.wktReader = wktReader;
  }

  /**
   * Creates a new <tt>WKTFileReader</tt>, given the name of the file to read from.
   *
   * @param filename the name of the file to read from
   * @param wktReader the geometry reader to use
   */
  public WKTFileReader(final String filename, final WKTReader wktReader) {
    this(new File(filename), wktReader);
  }

  /**
   * Tests if reader is at EOF.
   */
  private boolean isAtEndOfFile(final BufferedReader bufferedReader) throws IOException {
    bufferedReader.mark(MAX_LOOKAHEAD);

    final StreamTokenizer tokenizer = new StreamTokenizer(bufferedReader);
    final int type = tokenizer.nextToken();

    if (type == StreamTokenizer.TT_EOF) {
      return true;
    }
    bufferedReader.reset();
    return false;
  }

  private boolean isAtLimit(final List geoms) {
    if (this.limit < 0) {
      return false;
    }
    if (geoms.size() < this.limit) {
      return false;
    }
    return true;
  }

  /**
   * Reads a sequence of geometries.
   * If an offset is specified, geometries read up to the offset count are skipped.
   * If a limit is specified, no more than <tt>limit</tt> geometries are read.
   *
   * @return the list of geometries read
   * @throws IOException if an I/O exception was encountered
   * @throws ParseException if an error occured reading a geometry
   */
  public List read() throws IOException, ParseException {
    // do this here so that constructors don't throw exceptions
    if (this.file != null) {
      this.reader = new FileReader(this.file);
    }

    this.count = 0;
    try {
      final BufferedReader bufferedReader = new BufferedReader(this.reader);
      try {
        return read(bufferedReader);
      } finally {
        bufferedReader.close();
      }
    } finally {
      this.reader.close();
    }
  }

  private List read(final BufferedReader bufferedReader) throws IOException, ParseException {
    final List geoms = new ArrayList();
    while (!isAtEndOfFile(bufferedReader) && !isAtLimit(geoms)) {
      final Geometry g = this.wktReader.read(bufferedReader);
      if (this.count >= this.offset) {
        geoms.add(g);
      }
      this.count++;
    }
    return geoms;
  }

  /**
   * Sets the maximum number of geometries to read.
   *
   * @param limit the maximum number of geometries to read
   */
  public void setLimit(final int limit) {
    this.limit = limit;
  }

  /**
   * Sets the number of geometries to skip before storing.
   *
   * @param offset the number of geometries to skip
   */
  public void setOffset(final int offset) {
    this.offset = offset;
  }
}
