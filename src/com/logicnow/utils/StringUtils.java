package com.logicnow.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.WordUtils;

/**
 * Contains utility methods related to strings.
 */
public final class StringUtils extends org.apache.commons.lang.StringUtils {

  /** UTF-8. */
  public static final String UTF8_ENCODING = "UTF-8";

  /**
   * Format <code>value</code> using <code>args</code>.
   *
   * <p>Returns <code>null</code> if <code>value</code> is null.</p>
   */
  public static String format(String value, Object... args) {
    return (value == null) ? null : String.format(value, args);
  }


  /**
   * Ensures that the output String has only valid XML Unicode characters as specified by the XML 1.0 standard. Replaces any invalid characters with the input replacement string; if the replacement
   * string is null, the invalid characters are simply removed.
   *
   * @param in the String whose non-valid characters we want to remove.
   * @return the input String, stripped of invalid characters, or an empty string if the input is null or empty
   * @link http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
   * @link http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
   */
  public static String stripOffInvalidXmlCharacters(String in, String replacement) {
    StringBuilder out = new StringBuilder();
    char current;

    if (!StringUtils.isEmpty(in)) {
      int len = in.length();
      for (int i = 0; i < len; i++) {
        current = in.charAt(i);

        if ( (current == 0x9) || (current == 0xA) || (current == 0xD) ||
            ( (current >= 0x20) && (current <= 0xD7FF) ) ||
            ( (current >= 0xE000) && (current <= 0xFFFD) ) ||
            ( (current >= 0x10000) && (current <= 0x10FFFF) ) ){
          out.append(current);
        } else if( null != replacement ) {
          out.append(replacement);
        }
      }
    }

    return out.toString();
  }

  /**
   * Removes "bad" (non-alpha-numeric) characters from a String.   All UTF-16 alpha-numeric characters are retained.  For example: <CODE>stripOffNonAlphanums("ab$*^ab#%##ab", "%.,")</CODE> yields "ababab".
   *
   * For historical reasons, the underscore character <CODE>_</CODE> is currently not stripped.  This will likely change.
   *
   * @param str the String to process
   *
   * @return the resulting String
   * @see #stripOffNonAlphanums(String,String,String)
   */
  public static String stripOffNonAlphanums(String str) {
    return stripOffNonAlphanums(str, StringUtils.EMPTY, null);
  }

  /**
   * Removes "bad" (non-alpha-numeric) characters from a String.  All UTF-16 alpha-numeric characters are retained.  Replaces non-alphanumeric characters with the passed in replacement string.
   * If the replacement string is null or empty, the bad characters are removed without replacement.
   * For example: <CODE>stripOffNonAlphanums("ab$*^ab#%##ab", "%.,", "_")</CODE> yields "ab_ab_%_ab".
   * This version of the method takes an okNonAlphaNumChars argument which allows the caller to keep some non-alphanumerics in the resulting string.  A series of non-ok characters will be replaced by a
   * single replacement character.
   *
   * For historical reasons, the underscore character <CODE>_</CODE> is currently not stripped.  This will likely change, so we recommend you include underscore in
   * <CODE>okNonAlphaNumChars</CODE> if that's the behavior you want.
   *
   * @param str the String to process
   * @param okNonAlphaNumChars the "OK" characters
   * @param replacement the replacement
   *
   * @return the resulting String
   */
  public static String stripOffNonAlphanums(String str, String okNonAlphaNumChars, String replacement) {
    int len = str.length();
    StringBuilder buf = new StringBuilder(len);
    boolean okChar = false;
    boolean haveReplacement = !StringUtils.isEmpty(replacement);

    // Look at each char
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      boolean isLetterOrDigit = Character.isLetterOrDigit(c);
      // If the char is alphanumeric or is within the "OK" non-alphanumeric set
      if (isLetterOrDigit || c == '_' || (okNonAlphaNumChars != null && okNonAlphaNumChars.indexOf(c) >= 0)) {
        // Keep it
        buf.append(c);
        okChar = false;
      } else {
        // Not an "OK" char? See if we need to replace it
        if (!okChar && haveReplacement) {
          buf.append(replacement);
        }
        okChar = true;
      }
    }

    return buf.toString();
  }

  /**
   * Parses out a string using a delimiter into tokens and returns a string list filled with whitespace-trimmed tokens, if any.
   * Null values are not returned (that is, if two delimiters are separated by only whitespace, the null result is skipped).
   *
   * @param str the string to parse
   * @param delim the delimiter
   * @return string list filled with trimmed tokens or an empty list if none found
   */
  public static List<String> getTrimmedDelimitedStringValueList(String str, String delim) {
    return internalGetDelimitedStringValueList(str, delim, true, true);
  }

  /**
   * Parses out a string using a delimiter into tokens and returns a string list filled with tokens, if any.
   * Completely null values (i.e., no characters between delimiters) are always skipped.
   *
   * @param str the string to parse
   * @param delim the delimiter
   * @param trim whether to trim the leading and trailing whitespace off each token
   * @param skipEmptyValues whether to exclude values that are only whitespace.  Remember that completely null values are always skipped regardless of the value of this parameter.
   * @return string list filled with tokens or an empty list if none found
   */
  public static List<String> getDelimitedStringValueList(String str, String delim, boolean trim, boolean skipEmptyValues) {
    return internalGetDelimitedStringValueList(str, delim, trim, skipEmptyValues);
  }

  /**
   * Parses out a string using a delimiter into tokens and returns a string list filled with tokens, if any.
   * Completely null values (i.e., no characters between delimiters) are always skipped.
   * @param str
   * @param delim
   * @param trim
   * @param skipEmptyValues
   * @return
   */
  private static List<String> internalGetDelimitedStringValueList(String str, String delim, boolean trim, boolean skipEmptyValues) {
    ArrayList<String> valueList = new ArrayList<String>();
    // If there is no delimiter specified
    if (!StringUtils.isEmpty(str) && !StringUtils.isEmpty(delim)) {
      // Extract the values
      StringTokenizer st = new StringTokenizer(str, delim);
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (trim) {
          token = token.trim();
        }
        if (!(skipEmptyValues && StringUtils.isEmpty(token))) {
          valueList.add(token);
        }
      }
    }
    return valueList;
  }

  /**
   * Starting from <code>startIndex</code>, finds the index of the first character of the next line. Blank lines are skipped.
   * The characters <code>\r</code> and <code>\n</code> define new lines.  A line separator is considered to belong to the string
   * after it, so <code>indexOfNextLine("one\rtwo\nthree",3)</code> returns 8 (the beginning of 'three'), not 4
   * (the beginning of 'two').
   * @param str the string
   * @param startIndex the start index
   * @return the index of the next line or -1 if no more lines
   */
  public static int indexOfNextLine(String str, int startIndex) {
    boolean newlineFound = false;
    boolean lineStartFound = false;
    int maxIdx = str.length();

    while (startIndex < maxIdx && !(newlineFound && lineStartFound)) {
      switch (str.charAt(startIndex++)) {
      case '\n':
      case '\r':
        newlineFound = true;
        lineStartFound = false;
        break;
      default:
        lineStartFound = true;
      }
    }

    if (!newlineFound || !lineStartFound)
      return -1;
    else
      return startIndex;
  }

  /**
   * Gets the stack trace for a given throwable as a string
   * @param th the throwable
   * @return the stack trace as a string
   */
  public static String getStackTraceAsString(Throwable th) {
    Writer stackTrace = new StringWriter();
    PrintWriter writer = new PrintWriter(stackTrace);
    th.printStackTrace(writer);
    return stackTrace.toString();
  }

  /**
   * Compresses whitespace.  This uses Java's <CODE>Character.isWhitespace</CODE> method so it specifically does not include non-breaking spaces.
   * @param s String to compress
   * @return string with leading and trailing whitespace removed, and internal runs of whitespace replaced by a single space character
   */
  public static String compressWhitespace(String s) {
    StringBuilder output = new StringBuilder();
    int p = 0;
    boolean inSpace = true;
    for (int i = 0, len = s.length(); i < len; ++i) {
      if (Character.isWhitespace(s.charAt(i))) {
        if (!inSpace) {
          output.append(s.substring(p, i));
          output.append(' ');
          inSpace = true;
        }
      } else {
        if (inSpace) {
          p = i;
          inSpace = false;
        }
      }
    }
    if (!inSpace) {
      output.append(s.substring(p));
    }
    return output.toString().trim();
  }

  /**
   * Converts the passed in string to the title case, i.e. capitalizes all the whitespace separated words in the string.
   * @param s the string to convert
   * @return the capitalized string
   */
  public static String toTitleCase(String s) {
    return WordUtils.capitalize(s);
  }

  /**
   * Strips off any non-ASCII characters from a string
   * @param in the string
   * @return the resulting string
   */
  public static String stripOffNonAscii(String in) {
    StringBuilder out = new StringBuilder(StringUtils.EMPTY);
    char current;
    if (in != null) {
      for (int i = 0; i < in.length(); i++) {
        current = in.charAt(i);
        if (isAsciiChar(current)) {
          out.append(current);
        }
      }
    }
    return out.toString();
  }

  /**
   * Tells whether a given character is ASCII
   * @param c the character to test
   * @return true if the character is ASCII
   */
  public static boolean isAsciiChar(char c) {
    return ((c & 0xff80) == 0);
  }

  /**
   * Finds the matching close (identified by <code>endElem</code>), handling embedded opens (identified by <code>startElem</code>) and closes. <code>startIdx</code> is where the starting elem is located. If 0, it will be searched for in
   * text.
   * @param startElem
   * @param endElem
   * @param text
   * @param startIdx
   */
  public static int findMatchingClose(String startElem, String endElem, String text, int startIdx) {
    if (startIdx <= 0)
      startIdx = text.indexOf(startElem);
    int nextClose = text.indexOf(endElem, startIdx + 2);
    int nextStart = text.indexOf(startElem, startIdx + 2);

    while (nextStart > -1 && nextStart < nextClose) {
      int close = text.indexOf(endElem, nextClose + 2);
      if (close < 0)
        break;
      nextClose = close;
      nextStart = text.indexOf(startElem, nextStart + 2);
    }

    return nextClose;
  }

  /**
   * Finds the last occurrence of <code>lastTarget</code> that appears before an occurrence of <code>beforeStr</code>.
   * @param src
   * @param lastTarget
   * @param beforeStr
   */
  public static int lastIndexOfBefore(String src, String lastTarget, String beforeStr) {
    int beforeIdx = src.indexOf(beforeStr);
    if (beforeIdx == -1)
      return src.lastIndexOf(lastTarget);
    int lastIdx = -1;
    while (lastIdx < beforeIdx - 1) {
      lastIdx = src.indexOf(lastTarget, lastIdx + 1);
      if (lastIdx == -1)
        break;
    }
    return lastIdx;
  }


  /**
   * Join 2 strings on a separator character.
   */
  public static String join(String a, String b, char sep) {
    return ModelStringUtils.join(a, b, sep);
  }

  /**
   * Join slice of <code>array</code> identified by <code>startIndex</code> and <code>endIndex</code> on <code>sep</code>.
   * Throws <code>NegativeArraySizeException</code> if <code>startIndex</code> is less than or equal to <code>endIndex</code>.
   */
  public static String join(final String[] array, final char sep, final int startIndex, final int endIndex) {
    return ModelStringUtils.join(array, sep, startIndex, endIndex);
  }

  /**
   * Joins an array of ints into a string with a given separator.
   *
   * @param ints the integers
   * @param sep the separator
   * @return the joined string
   */
  public static String join(int[] ints, String sep) {
    return ModelStringUtils.join(ints, sep);
  }

  /**
   * Joins an array of longs into a string with a given separator.
   * 
   * @param longs the longs
   * @param sep the separator
   * @return the joined string
   */
  public static String join(long[] longs, String sep) {
    return ModelStringUtils.join(longs, sep);
  }

  /**
   * Joins an array of ints into a string with a given separator.
   * 
   * @param ints the integers
   * @param sep the separator
   * @return the joined string
   */
  public static String join(int[] ints, final String sep, final int count) {
    return ModelStringUtils.join(ints, sep, count);
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
    return ModelStringUtils.join(ints, sep, start, count);
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
    return ModelStringUtils.join(ints, sep, count);
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
    return ModelStringUtils.join(ints, sep, start, count);
  }

  /**
   * Joins input string list with separator
   * @param strList string list to join
   * @param separator to use in between values
   * @return strings joined by separator, or a zero length string if strList is null or empty
   */
  public static String join(List<String> strList, String separator) {
    return ModelStringUtils.join(strList, separator);
  }

  /**
   * Split a String containing <code>count</code> decimal integers separated by <code>sep</code>.
   *
   * <p><b>CAUTION:</b> <code>data</code> is expected to be properly formatted and only contain
   *    characters '0' - '9', ',' and '-'.</p>
   */
  public static int[] splitIntArray(final String data, final char sep, final int count) {
    if (count == 0) {
      return new int[0];
    } else if (data == null) {
      return null;
    } else {
      final int length = data.length();
      final int[] value = new int[count];

      int index = 0;
      int current = 0;
      boolean negative = false;
      for (int i = 0; i < length; ++i) {
        final char c = data.charAt(i);
        if (c == sep) {
          value[index] = (negative) ? current * -1 : current;
          current = 0;
          negative = false;
          ++index;
        } else if (c == '-') {
          negative = true;
        } else if ((c >= '0') && (c <= '9')) {
          current = (current * 10) + (c - '0');
        } else {
          throw new IllegalArgumentException("Unexpected character: " + c);
        }
      }

      // Handle last value
      value[index] = (negative) ? current * -1 : current;

      return value;
    }
  }


  /*** Gets bytes of string in utf8. */
  public static byte[] getUtf8Bytes(String s) {
    try {
      return s.getBytes(UTF8_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("UTF8 not supported");
    }
  }

  /*** Gets string from utf8 bytes. */
  public static String getUtf8String(byte[] bytes) {
    try {
      return new String(bytes, UTF8_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("UTF8 not supported");
    }
  }

  /** Decode URL in UTF-8. */
  public static String urlDecode(String s) {
    try {
      return URLDecoder.decode(s, UTF8_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("UTF8 not supported");
    }
  }

  /** Encode URL in UTF-8. */
  public static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, UTF8_ENCODING);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("UTF8 not supported");
    }
  }

  /**
   * Parse a string representation of an ipv4 ip address into a byte array.
   */
  public static byte[] parseIpAddress(String address) {
    // Split on '.'
    final String[] ints = split(address, '.');
    if (ints == null || ints.length != 4) throw new IllegalArgumentException("invalid ip address");

    // Construct byte array
    try {
      return new byte[] {
          (byte)Integer.parseInt(ints[0]),
          (byte)Integer.parseInt(ints[1]),
          (byte)Integer.parseInt(ints[2]),
          (byte)Integer.parseInt(ints[3]) };
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("invalid ip address");
    }
  }

  /**
   * Parses a standard uri query string.  Handles null query strings.
   * @param qs
   * @param encoding
   * @return a map of query parameter to values.  guaranteed to return non-null.
   * @throws UnsupportedEncodingException
   */
  public static Map<String,String[]> parseQueryString(String qs, String encoding) throws UnsupportedEncodingException {
    return ModelStringUtils.parseQueryString(qs, encoding);
  }

  /**
   * Encodes the value
   * @param l
   * @return the encoded value
   */
  public static String encode(long l) {
    return String.valueOf(l);
  }


  /**
   * Encodes <code>s</code> as JSON, escaping Unicode
   * @param s
   * @return the encoded value
   */
  public static String encode(String s) {
    return encode(s, true);
  }

  /**
   * Encodes a value as JSON, with an option to escape Unicode
   * @param value the value to encode
   * @param escapeUnicode the escape Unicode option
   * @return the encoded string
   */
  public static String encode(String value, boolean escapeUnicode) {
    if (value == null)
      return "null";

    int length = value.length();
    char c;

    // Find the total capacity for our string builder
    int capacity = length;
    for (int i = 0; i < length; ++i) {
      c = value.charAt(i);
      switch (c) {
      case '"':
      case '\n':
      case '\r':
      case '\t':
      case '\f':
      case '\b':
      case '\\':
      case '/':
        ++capacity;
        break;
      default:
        break;
      }
      if ((c < 8) || ((escapeUnicode) && (c > 127))) {
        capacity += 5;
      }
    }

    // Create our new string
    char[] buffer = new char[capacity + 2];
    buffer[0] = '"';

    int pos = 0;
    for (int i = 0; i < length; ++i) {
      c = value.charAt(i);
      if ((c < 8) || ((escapeUnicode) && (c > 127))) {
        int x1, x2, x3, x4;
        x1 = (c / 4096) % 16;
        x2 = (c / 256) % 16;
        x3 = (c / 16) % 16;
        x4 = c % 16;

        /* '7' == 'A' - 10 */
        buffer[++pos] = '\\';
        buffer[++pos] = 'u';
        buffer[++pos] = (char) ((x1 < 10) ? ('0' + x1) : ('7' + x1));
        buffer[++pos] = (char) ((x2 < 10) ? ('0' + x2) : ('7' + x2));
        buffer[++pos] = (char) ((x3 < 10) ? ('0' + x3) : ('7' + x3));
        buffer[++pos] = (char) ((x4 < 10) ? ('0' + x4) : ('7' + x4));
      } else {
        switch (c) {
        case '"':
          buffer[++pos] = '\\';
          buffer[++pos] = '\"';
          break;
        case '\n':
          buffer[++pos] = '\\';
          buffer[++pos] = 'n';
          break;
        case '\r':
          buffer[++pos] = '\\';
          buffer[++pos] = 'r';
          break;
        case '\t':
          buffer[++pos] = '\\';
          buffer[++pos] = 't';
          break;
        case '\f':
          buffer[++pos] = '\\';
          buffer[++pos] = 'f';
          break;
        case '\b':
          buffer[++pos] = '\\';
          buffer[++pos] = 'b';
          break;
        case '\\':
          buffer[++pos] = '\\';
          buffer[++pos] = '\\';
          break;
        case '/':
          buffer[++pos] = '\\';
          buffer[++pos] = '/';
          break;
        default:
          buffer[++pos] = c;
          break;
        }
      }
    }
    buffer[++pos] = '"';

    return new String(buffer);
  }

  /**
   * Wrap <code>value</code> in double quotes.
   *
   * <p>Any double quotes in <code>value</code> will be escaped by a backslash.  Any backslashes in <code>value</code> will be escaped as double backslashes.
   * No other escaping is done; for example a tab character is returned as simply a tab character, not backslash-t.</p>
   */
  public static String quote(String value) {
    return ModelStringUtils.quote(value);
  }

  /**
   * Write the quoted string form of <code>value</code> to <code>buffer</code>.
   *
   * <p>Any double quotes and backslashes in <code>value</code> will be escaped by a backslash.  No other escaping is done; for
   * example a tab character is returned as simply a tab character, not backslash-t.</p>
   */
  public static void quote(StringBuilder buffer, String value) {
    ModelStringUtils.quote(buffer, value);
  }

  /**
   * Write <code>value</code> to <code>buffer</code>, escaping double quote and backslash with a backslash.
   */
  public static void escape(StringBuilder buffer, String value) {
    ModelStringUtils.escape(buffer, value);
  }

  /**
   * Append <code>value</code> to <code>buffer</code> <code>count</code> times.  A negative value of <code>count</code>
   * is silently treated as zero.
   */
  public static StringBuilder repeat(StringBuilder buffer, String value, int count) {
    return ModelStringUtils.repeat(buffer, value, count);
  }

  /**
   * Converts time in milliseconds to a user-friendly <code>String</code> that lists hours, minutes, seconds, and milliseconds.
   *
   * @param time the time in milliseconds.
   * @return the user-friendly string
   */
  public static String millisecondsToString(long time) {
    if (time == 0L) {
      return "0 ms.";
    } else {
      final StringBuilder result = new StringBuilder();
      if (time < 0L) {
        result.append('-');
        time = -time;
      }

      final long milliseconds = time % 1000;
      final long seconds      = (time / 1000L) % 60;
      final long minutes      = (time / 60000L) % 60;
      final long hours        = (time / 3600000L) % 24;
      final long days         = time / 86400000L;

      String sep = timeToString(result, days, "day", "");
      sep = timeToString(result, hours, "hour", sep);
      sep = timeToString(result, minutes, "minute", sep);
      sep = timeToString(result, seconds, "second", sep);
      if (milliseconds > 0) {
        result.append(sep).append(milliseconds).append(" ms.");
      }
      return result.toString();
    }
  }

  /**
   * Helper method for {@link #millisecondsToString(long)}.
   */
  private static final String timeToString(final StringBuilder buffer, final long value, final String label, final String sep) {
    if (value == 1L) {
      buffer.append(sep).append(value).append(' ').append(label);
      return "; ";
    } else if (value > 0L) {
      buffer.append(sep).append(value).append(' ').append(label).append('s');
      return "; ";
    } else {
      return sep;
    }
  }

  /** Gets the first non-blank value from a list of values, according to org.apache.commons.lang.StringUtils.isBlank().
   *  Returns null if no non-blank values are found.
   */
  public static String getFirst(String ... vals) {
    for (String tmp : vals) {
      if (!StringUtils.isBlank(tmp)) {
        return tmp;
      }
    }
    return null;
  }
}
