package com.revolsys.util;

public interface Numbers {
  static boolean between(final int min, final int value, final int max) {
    if (min > max) {
      if (value < max) {
        return false;
      } else if (value > min) {
        return false;
      } else {
        return true;
      }
    } else {
      if (value < min) {
        return false;
      } else if (value > max) {
        return false;
      } else {
        return true;
      }
    }
  }

  static byte digitCount(long value) {
    if (value == 0) {
      return 1;
    } else if (value < 0) {
      value = -value;
    }
    if (value < 10) {
      return 1;
    } else if (value < 100) {
      return 2;
    } else if (value < 1000) {
      return 3;
    } else if (value < 10000) {
      return 4;
    } else if (value < 100000) {
      return 5;
    } else if (value < 1000000) {
      return 6;
    } else if (value < 10000000) {
      return 7;
    } else if (value < 100000000) {
      return 8;
    } else if (value < 1000000000) {
      return 9;
    } else if (value < 10000000000L) {
      return 10;
    } else if (value < 100000000000L) {
      return 11;
    } else if (value < 1000000000000L) {
      return 12;
    } else if (value < 10000000000000L) {
      return 13;
    } else if (value < 100000000000000L) {
      return 14;
    } else if (value < 1000000000000000L) {
      return 15;
    } else if (value < 10000000000000000L) {
      return 16;
    } else if (value < 100000000000000000L) {
      return 17;
    } else if (value < 1000000000000000000L) {
      return 18;
    } else {
      return 19;
    }
  }

  static boolean equal(final Byte value1, final Byte value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      return value1.equals(value2);
    }
  }

  static boolean equal(final Double value1, final Double value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      return value1.equals(value2);
    }
  }

  static boolean equal(final Float value1, final Float value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      return value1.equals(value2);
    }
  }

  static boolean equal(final Integer value1, final Integer value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      return value1.equals(value2);
    }
  }

  static boolean equal(final Long value1, final Long value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      return value1.equals(value2);
    }
  }

  static boolean equal(final Short value1, final Short value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      return value1.equals(value2);
    }
  }

  static boolean greaterThan(final Integer number, final int min) {
    if (number == null) {
      return false;
    } else {
      return number > min;
    }
  }

  static boolean isDigit(final char character) {
    if (character >= '0' && character <= '9') {
      return true;
    } else {
      return false;
    }
  }

  static boolean isDigit(final Character character) {
    if (character == null) {
      return false;
    } else {
      return isDigit(character.charValue());
    }
  }

  static boolean isEven(final int number) {
    return number % 2 == 0;
  }

  static boolean isLong(final String part) {
    return toLong(part) != null;
  }

  static boolean isOdd(final int number) {
    return number % 2 == 1;
  }

  static boolean isPrimitive(final Object object) {
    if (object instanceof Integer) {
      return true;
    } else if (object instanceof Long) {
      return true;
    } else if (object instanceof Short) {
      return true;
    } else if (object instanceof Byte) {
      return true;
    } else if (object instanceof Double) {
      return true;
    } else if (object instanceof Float) {
      return true;
    } else {
      return false;
    }
  }

  static boolean isPrimitiveDecimal(final Object object) {
    if (object instanceof Double) {
      return true;
    } else if (object instanceof Float) {
      return true;
    } else {
      return false;
    }
  }

  static boolean isPrimitiveIntegral(final Object object) {
    if (object instanceof Integer) {
      return true;
    } else if (object instanceof Long) {
      return true;
    } else if (object instanceof Short) {
      return true;
    } else if (object instanceof Byte) {
      return true;
    } else {
      return false;
    }
  }

  static Integer max(final Integer number1, final Integer number2) {
    if (number1 == null) {
      return number2;
    } else if (number2 == null) {
      return number1;
    } else if (number1 >= number2) {
      return number1;
    } else {
      return number2;
    }
  }

  static Integer min(final Integer number1, final Integer number2) {
    if (number1 == null) {
      return number2;
    } else if (number2 == null) {
      return number1;
    } else if (number1 <= number2) {
      return number1;
    } else {
      return number2;
    }
  }

  static boolean overlaps(final int min1, final int max1, final int min2, final int max2) {
    if (min1 > max1) {
      return overlaps(max1, min1, min2, max2);
    } else if (min2 > max2) {
      return overlaps(min1, max1, max2, min2);
    } else {
      if (min1 <= max2 && min2 <= max1) {
        return true;
      } else {
        return false;
      }
    }
  }

  static double ratio(final Number number, final Number from, final Number to) {
    if (number == null) {
      return Double.MAX_VALUE;
    } else {
      final long numberLong = number.longValue();
      if (from == null) {
        if (to == null) {
          return Double.MAX_VALUE;
        } else {
          final long toLong = to.longValue();
          if (toLong < numberLong) {
            return numberLong - toLong;
          } else {
            return 1.0 + (toLong - numberLong);
          }
        }
      } else if (to == null) {
        final long fromLong = from.longValue();
        if (fromLong < numberLong) {
          return numberLong - fromLong;
        } else {
          return 1.0 + (fromLong - numberLong);
        }
      } else {
        final long fromLong = from.longValue();
        final long toLong = to.longValue();
        if (fromLong == numberLong) {
          return 0;
        } else if (to == number) {
          return 1;
        } else {
          final double delta = Math.abs(toLong - fromLong);
          if (fromLong <= toLong) {
            return (numberLong - fromLong) / delta;
          } else {
            return 1 - (numberLong - toLong) / delta;
          }
        }
      }
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Byte toByte(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.byteValue();
    } else {
      final String string = value.toString();
      return toByte(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Byte toByte(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      byte limit = -Byte.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Byte.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              return null;
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        byte result = 0;
        for (; index < length; index++) {
          final char character = string.charAt(index);
          switch (character) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              if (result < multmin) {
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
          }
        }
        if (negative) {
          return result;
        } else {
          return (byte)-result;
        }
      }
    }
  }

  static double[] toDoubleArray(final Number[] numbers) {
    final int length = numbers.length;
    final double[] result = new double[length];
    for (int i = 0; i < length; i++) {
      final Number number = numbers[i];
      result[i] = number.doubleValue();
    }
    return result;
  }

  static double[] toDoubleArray(final Number[] numbers, int offset, int length) {
    if (length > 0) {
      if (offset + length > numbers.length) {
        length = numbers.length - offset;
      }
      final double[] result = new double[length];
      for (int i = 0; i < length; i++) {
        final Number number = numbers[offset++];
        result[i] = number.doubleValue();
      }
      return result;
    } else {
      return new double[0];
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Integer toInteger(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      final String string = value.toString();
      return toInteger(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Integer toInteger(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      int limit = -Integer.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Integer.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              return null;
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        int result = 0;
        for (; index < length; index++) {
          final char character = string.charAt(index);
          switch (character) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              if (result < multmin) {
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
          }
        }
        if (negative) {
          return result;
        } else {
          return -result;
        }
      }
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Long toLong(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String string = value.toString();
      return toLong(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Long toLong(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      long limit = -Long.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Long.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              return null;
            }
            index++;
          break;
        }
        final long multmin = limit / 10;
        long result = 0;
        for (; index < length; index++) {
          final char character = string.charAt(index);
          switch (character) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              if (result < multmin) {
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
          }
        }
        if (negative) {
          return result;
        } else {
          return -result;
        }
      }
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Short toShort(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.shortValue();
    } else {
      final String string = value.toString();
      return toShort(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * null is returned instead of an exception.
   */
  static Short toShort(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      short limit = -Short.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Short.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              return null;
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        short result = 0;
        for (; index < length; index++) {
          final char character = string.charAt(index);
          switch (character) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              if (result < multmin) {
                return null;
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                return null;
              }
              result -= digit;
            break;
            default:
              return null;
          }
        }
        if (negative) {
          return result;
        } else {
          return (short)-result;
        }
      }
    }
  }

  static String toStringPadded(final long value, final int digitCount) {
    final String string = Long.toString(value);
    if (string.length() < digitCount) {
      final StringBuilder builder = new StringBuilder(digitCount);
      for (int i = string.length(); i < digitCount; i++) {
        builder.append('0');
      }
      builder.append(string);
      return builder.toString();
    } else {
      return string;
    }
  }
}
