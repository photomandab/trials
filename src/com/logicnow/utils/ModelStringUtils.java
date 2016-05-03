package com.logicnow.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelStringUtils {

  /**
   * Append <code>value</code> to <code>buffer</code> <code>count</code> times.  A negative value of <code>count</code>
   * is silently treated as zero. */
  public static StringBuilder repeat(StringBuilder buffer, String value, int count) {
    for (int i = 0; i < count; ++i) {
      buffer.append(value);
    }
    return buffer;
  }

  public static boolean isEmpty(String s) {
    return s == null || s.length() == 0;
  }

  public static boolean isBlank(String s) {
    return s == null || s.trim().length() == 0;
  }

  /**
   * Joins an array of ints into a string with a given separator.
   *
   * @param ints the integers
   * @param sep the separator
   * @param count only join this many ints
   * @return the joined string
   */
  public static String join(int[] ints, final char sep, final int count) {
    return join(ints, sep, 0, count);
  }

  /**
   * Joins an array of longs into a string with a given separator.
   *
   * @param longs the longs
   * @param sep the separator
   * @return the joined string
   */
  public static String join(long[] longs, String sep) {
    return join(longs, sep, 0, longs.length);
  }

  /**
   * Joins an array of ints into a string with a given separator.
   *
   * @param ints the integers
   * @param sep the separator
   * @param count only join this many ints
   * @return the joined string
   */
  public static String join(int[] ints, final char sep, final int start, final int count) {
    if (ints == null || count == 0) {
      return "";
    } else {
      // Assume each integer is 3 digits (plus sep)
      final StringBuilder sb = new StringBuilder(count * 4);
      if (count > 0) {
        sb.append(ints[start]);
      }
      final int end = start + count;
      for (int i = start + 1; i < end; ++i) {
        sb.append(sep);
        sb.append(ints[i]);
      }
      return sb.toString();
    }
  }

  /**
   * Joins an array of longs into a string with a given separator.
   *
   * @param longs the longs
   * @param sep the separator
   * @param count only join this many ints
   * @return the joined string
   */
  public static String join(long[] longs, final String sep, final int start, final int count) {
    if (longs == null || count == 0) {
      return "";
    } else {
      // Assume each integer is 3 digits (plus sep)
      final StringBuilder sb = new StringBuilder(count * 4);
      if (count > 0) {
        sb.append(longs[start]);
      }
      final int end = start + count;
      for (int i = start + 1; i < end; ++i) {
        sb.append(sep);
        sb.append(longs[i]);
      }
      return sb.toString();
    }
  }

  /**
   * Joins input string list with separator
   * @param strList string list to join
   * @param separator to use in between values
   * @return strings joined by separator, or a zero length string if strList is null or empty
   */
  public static String join(List<String> strList, String separator) {
    if (strList == null || strList.size() == 0) return "";
    return join(strList.toArray(new String[strList.size()]), separator);
  }

  /**
   * Joins an array of ints into a string with a given separator.
   *
   * @param ints the integers
   * @param sep the separator
   * @return the joined string
   */
  public static String join(int[] ints, String sep) {
    return join(ints, sep, ints.length);
  }

  /**
   * Joins an array of ints into a string with a given separator.
   *
   * @param ints the integers
   * @param sep the separator
   * @return the joined string
   */
  public static String join(int[] ints, final String sep, final int count) {
    return join(ints, sep, 0, count);
  }

  /**
   * Joins an array of ints into a string with a given separator.
   *
   * @param ints the integers
   * @param sep the separator
   * @param count only join this many ints
   * @return the joined string
   */
  public static String join(int[] ints, final String sep, final int start, final int count) {
    if (ints == null || count == 0) {
      return "";
    } else {
      // Assume each integer is 3 digits (plus sep)
      final StringBuilder sb = new StringBuilder(count * (3 + sep.length()));
      if (count > 0) {
        sb.append(ints[start]);
      }
      final int end = start + count;
      for (int i = start + 1; i < end; ++i) {
        sb.append(sep);
        sb.append(ints[i]);
      }
      return sb.toString();
    }
  }

  /**
   * Join 2 strings on a separator character.
   */
  public static String join(String a, String b, char sep) {
    final StringBuilder builder = new StringBuilder(a.length() + b.length() + 1);
    return builder.append(a).append(sep).append(b).toString();
  }

  /**
   * Join slice of <code>array</code> identified by <code>startIndex</code> and <code>endIndex</code> on <code>sep</code>.
   * Throws <code>NegativeArraySizeException</code> if <code>startIndex</code> is less than or equal to <code>endIndex</code>.
   */
  public static String join(final String[] array, final char sep, final int startIndex, final int endIndex) {
    // Find size (requires StringBuilder to not have to grow)
    int length = endIndex - startIndex - 1; // start with number of sep chars we're going to add
    for (int i = startIndex; i < endIndex; ++i) {
      length += array[i].length();
    }

    // Create the string
    final StringBuilder buffer = new StringBuilder(length);
    buffer.append(array[startIndex]);
    for (int i = startIndex + 1; i < endIndex; ++i) {
      buffer.append(sep).append(array[i]);
    }

    // Return the joined string
    return buffer.toString();
  }

  /**
   * Wrap <code>value</code> in double quotes.
   *
   * <p>Any double quotes in <code>value</code> will be escaped by a backslash.  Any backslashes in <code>value</code> will be escaped as double backslashes.
   * No other escaping is done; for example a tab character is returned as simply a tab character, not backslash-t.</p>
   */
  public static String quote(String value) {
    final StringBuilder buffer = new StringBuilder(value.length() + 10);
    quote(buffer, value);
    return buffer.toString();
  }

  /**
   * Write the quoted string form of <code>value</code> to <code>buffer</code>.
   *
   * <p>Any double quotes and backslashes in <code>value</code> will be escaped by a backslash.  No other escaping is done; for
   * example a tab character is returned as simply a tab character, not backslash-t.</p>
   */
  public static void quote(StringBuilder buffer, String value) {
    // Make sure only one allocation of memory will be needed
    buffer.ensureCapacity(buffer.length() + value.length() + 10);

    // Write the string out
    buffer.append('"');
    escape(buffer, value);
    buffer.append('"');
  }

  /**
   * Write <code>value</code> to <code>buffer</code>, escaping double quote and backslash with a backslash.
   */
  public static void escape(StringBuilder buffer, String value) {
    // Make sure only one allocation of memory will be needed
    buffer.ensureCapacity(buffer.length() + value.length() + 10);

    // Escape value into buffer
    for (int i = 0; i < value.length(); ++i) {
      final char c = value.charAt(i);
      if ((c == '"') || (c == '\\')) {
        buffer.append('\\');
      }
      buffer.append(c);
    }
  }

  public static String join(Iterator<? extends Object> iterator, String separator) {
    if (iterator == null) return null;
    if (separator == null) separator = "";
    StringBuilder sb = new StringBuilder();
    for (; iterator.hasNext();) {
      sb.append(iterator.next());
      if (iterator.hasNext()) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  public static String join(Object[] params, String separator) {
    if (params == null) return null;
    if (separator == null) separator = "";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < params.length - 1; i++) {
      sb.append(params[i]);
      sb.append(separator);
    }
    sb.append(params[params.length - 1]);
    return sb.toString();
  }

  public static String[] split(String s, char c) {
    return split(s, c, -1);
  }

  public static String[] split(String s, char c, int max) {
    if (s == null) return null;
    return s.split(String.valueOf(c), max);
  }

  /**
   * Parses a standard uri query string.  Handles null query strings.
   * @param qs
   * @param encoding
   * @return a map of query parameter to values.  guaranteed to return non-null.
   * @throws UnsupportedEncodingException
   */
  public static Map<String,String[]> parseQueryString(String qs, String encoding) throws UnsupportedEncodingException {
    final Map<String, String[]> params = new LinkedHashMap<String, String[]>();
    if (qs == null) return params;

    for (String param : ModelStringUtils.split(qs, '&')) {
      final String[] pair = ModelStringUtils.split(param, '=', 2);
      String key;
      String value;

      // Extract the key and value
      if (pair.length == 2) {
        key   = URLDecoder.decode(pair[0], encoding);
        value = URLDecoder.decode(pair[1], encoding);
      } else if (pair.length == 1) {
        key   = URLDecoder.decode(pair[0], encoding);
        value = "";
      } else {
        continue;
      }

      // Update params map
      final String[] previous = params.get(key);
      if (previous == null) {
        params.put( key, new String[] { value } );
      } else {
        final String[] newValue = new String[ previous.length + 1 ] ;
        for (int i = 0; i < previous.length; ++i) {
          newValue[i] = previous[i];
        }
        newValue[previous.length] = value;
        params.put(key, newValue);
      }
    }
    return params;
  }

}
