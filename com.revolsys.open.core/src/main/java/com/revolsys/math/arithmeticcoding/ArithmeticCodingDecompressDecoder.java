/*
 * Copyright 2005-2014, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.math.arithmeticcoding;

import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.AC__MaxLength;
import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.AC__MinLength;
import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.BM__LengthShift;
import static com.revolsys.math.arithmeticcoding.ArithmeticCodingDecompressModel.DM__LengthShift;
import static java.lang.Integer.compareUnsigned;

import com.revolsys.io.channels.ChannelReader;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// Fast arithmetic coding implementation                                     -
// -> 32-bit variables, 32-bit product, periodic updates, table decoding     -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// Version 1.00  -  April 25, 2004                                           -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
//                                  WARNING                                  -
//                                 =========                                 -
//                                                                           -
// The only purpose of this program is to demonstrate the basic principles   -
// of arithmetic coding. It is provided as is, without any express or        -
// implied warranty, without even the warranty of fitness for any particular -
// purpose, or that the implementations are correct.                         -
//                                                                           -
// Permission to copy and redistribute this code is hereby granted, provided -
// that this warning and copyright notices are not removed or altered.       -
//                                                                           -
// Copyright (c) 2004 by Amir Said (said@ieee.org) &                         -
//                       William A. Pearlman (pearlw@ecse.rpi.edu)           -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// A description of the arithmetic coding method used here is available in   -
//                                                                           -
// Lossless Compression Handbook, ed. K. Sayood                              -
// Chapter 5: Arithmetic Coding (A. Said), pp. 101-152, Academic Press, 2003 -
//                                                                           -
// A. Said, Introduction to Arithetic Coding Theory and Practice             -
// HP Labs report HPL-2004-76  -  http://www.hpl.hp.com/techreports/         -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public class ArithmeticCodingDecompressDecoder {

  private ChannelReader reader;

  private int value;

  private int length;

  public ArithmeticCodingDecompressDecoder() {
    this.reader = null;
  }

  public int decodeBit(final ArithmeticCodingBitModel model) {
    final int x = model.bit0Probability * (this.length >>> BM__LengthShift); // product
    // l x
    // p0
    final int sym = compareUnsigned(this.value, x) >= 0 ? 1 : 0; // decision
    // update & shift interval
    if (sym == 0) {
      this.length = x;
      ++model.bit0Count;
    } else {
      this.value -= x; // shifted interval base = 0
      this.length -= x;
    }

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval(); // renormalization
    }
    model.update();

    return sym; // return data bit value
  }

  public int decodeSymbol(final ArithmeticCodingDecompressModel model) {
    int n;
    int sym;
    int x;
    int y = this.length;

    if (model.decoderTable != null) {
      final int dv = Integer.divideUnsigned(this.value, this.length >>>= DM__LengthShift);
      final int t = dv >>> model.tableShift;

      sym = model.decoderTable[t];
      n = model.decoderTable[t + 1] + 1;

      while (compareUnsigned(n, sym + 1) > 0) {
        final int k = sym + n >>> 1;
        if (compareUnsigned(model.distribution[k], dv) > 0) {
          n = k;
        } else {
          sym = k;
        }
      }
      x = model.distribution[sym] * this.length;
      if (sym != model.lastSymbol) {
        y = model.distribution[sym + 1] * this.length;
      }
    } else {
      x = sym = 0;
      this.length >>>= DM__LengthShift;
      int k = (n = model.symbolCount) >>> 1;
      do {
        final int z = this.length * model.distribution[k];
        if (compareUnsigned(z, this.value) > 0) {
          n = k;
          y = z;
        } else {
          sym = k;
          x = z;
        }
      } while ((k = sym + n >>> 1) != sym);
    }

    this.value -= x;
    this.length = y - x;

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval();
    }

    ++model.symbolCounts[sym];
    if (--model.symbolsUntilUpdate == 0) {
      model.update();
    }
    return sym;
  }

  public boolean init(final ChannelReader reader) {
    if (reader == null) {
      return false;
    }
    this.reader = reader;
    this.length = AC__MaxLength;
    this.value = (reader.getByte() & 0xff) << 24;
    this.value |= (reader.getByte() & 0xff) << 16;
    this.value |= (reader.getByte() & 0xff) << 8;
    this.value |= reader.getByte() & 0xff;
    return true;
  }

  public int readBit() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 1);
    this.value -= this.length * sym;

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval();
    }

    if (compareUnsigned(sym, 2) >= 0) {
      throw new IllegalStateException("Error decompressing file");
    }

    return sym;
  }

  public int readBits(int bits) {
    if (bits > 19) {
      final int tmp = readShort();
      bits = bits - 16;
      final int tmp1 = readBits(bits) << 16;
      return tmp1 | tmp;
    }

    final int sym = Integer.divideUnsigned(this.value, this.length >>>= bits);
    this.value -= this.length * sym;

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval();
    }

    if (compareUnsigned(sym, 1 << bits) >= 0) {
      throw new IllegalStateException("Error decompressing file");
    }
    return sym;
  }

  public byte readByte() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 8);
    this.value -= this.length * sym;

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval();
    }

    if (compareUnsigned(sym, 1 << 8) >= 0) {
      throw new IllegalStateException("Error decompressing file");
    }

    return (byte)sym;
  }

  public double readDouble() {
    // danger in float reinterpretation
    return Double.longBitsToDouble(readInt64());
  }

  public float readFloat() {
    // danger in float reinterpretation
    return Float.intBitsToFloat(readInt());
  }

  public int readInt() {
    final int lowerInt = readShort();
    final int upperInt = readShort();
    return upperInt << 16 | lowerInt;
  }

  public long readInt64() {
    final long lowerInt = readInt();
    final long upperInt = readInt();
    return upperInt << 32 | lowerInt;
  }

  public char readShort() {
    final int sym = Integer.divideUnsigned(this.value, this.length >>>= 16);
    this.value -= this.length * sym;

    if (compareUnsigned(this.length, AC__MinLength) < 0) {
      renorm_dec_interval();
    }

    if (compareUnsigned(sym, 1 << 16) >= 0) {
      throw new IllegalStateException("Error decompressing file");
    }

    return (char)sym;
  }

  private void renorm_dec_interval() {
    do {
      this.value = this.value << 8 | this.reader.getByte() & 0xff;
    } while (Integer.compareUnsigned(this.length <<= 8, AC__MinLength) < 0);
  }
}
