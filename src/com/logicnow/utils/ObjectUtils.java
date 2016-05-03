package com.logicnow.utils;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains utility functions for working with the Object type
 */
public final class ObjectUtils {
	
  /** Array of strings that are "true" */
  static final String[] BOOLEAN_TRUE = { "true", "on", "yes" };

  /** Array of strings that are "false" */
  static final String[] BOOLEAN_FALSE = { "false", "off", "no" };

  /**
   * Returns <code>defaultValue</code> if <code>value</code> is null, <code>value</code> otherwise.
   */
  public static <T> T getDefault(T value, T defaultValue) {
    return (value == null) ? defaultValue : value;
  }

  /**
   * Intern and return <code>value</code> (handles nulls).
   */
  public static String intern(String value) {
    return (value == null) ? null : value.intern();
  }

  /**
   * Returns <code>defaultValue</code> if <code>value</code> is null, <code>value</code> otherwise.
   */
  public static boolean getDefault(Boolean value, boolean defaultValue) {
    return (value == null) ? defaultValue : value.booleanValue();
  }

  /**
   * Returns <code>defaultValue</code> if <code>value</code> is null, <code>value</code> otherwise.
   */
  public static int getDefault(Integer value, int defaultValue) {
    return (value == null) ? defaultValue : value.intValue();
  }

  /**
   * Returns <code>defaultValue</code> if <code>value</code> is null, <code>value</code> otherwise.
   */
  public static long getDefault(Long value, long defaultValue) {
    return (value == null) ? defaultValue : value.longValue();
  }

  /**
   * Returns <code>defaultValue</code> if <code>value</code> is null, <code>value</code> otherwise.
   */
  public static float getDefault(Float value, float defaultValue) {
    return (value == null) ? defaultValue : value.floatValue();
  }

  /**
   * Returns <code>defaultValue</code> if <code>value</code> is null, <code>value</code> otherwise.
   */
  public static double getDefault(Double value, double defaultValue) {
    return (value == null) ? defaultValue : value.doubleValue();
  }

  /**
   * Fastest possible binary search.
   *
   * <p>WARNING: no bounds checking is performed.</p>
   *
   * @param array the array to search.
   * @param key the key to search for.
   * @param startIndex the start index to start looking at (inclusive).
   * @param endIndex the end index to stop looking at (inclusive).
   * @return the index where key (or the greatest value less than key) is located.
   */
  public static int binarySearch(final int[] array, final int key, final int startIndex, final int endIndex, final int missingOffset) {
    int min = startIndex;
    int max = endIndex;
    int mid;
    int value;

    while (min <= max) {
      mid   = (min + max) >>> 1;
      value = array[mid];
      if (value < key) {
        min = mid + 1;
      } else if (value > key) {
        max = mid - 1;
      } else {
        return mid;
      }
    }
    return min + missingOffset;
  }

  /**
   * Fastest possible binary search.
   *
   * <p>WARNING: no bounds checking is performed.</p>
   *
   * @param array the array to search.
   * @param key the key to search for.
   * @param startIndex the start index to start looking at (inclusive).
   * @param endIndex the end index to stop looking at (inclusive).
   * @return the index where key (or the greatest value less than key) is located.
   */
  public static int binarySearch(final long[] array, final long key, final int startIndex, final int endIndex, final int missingOffset) {
    int min = startIndex;
    int max = endIndex;
    int mid;
    long value;

    while (min <= max) {
      mid = (min + max) >>> 1;
      value = array[mid];
      if (value < key) {
        min = mid + 1;
      } else if (value > key) {
        max = mid - 1;
      } else {
        return mid;
      }
    }
    return min + missingOffset;
  }

  /**
   * Fastest possible binary search.
   *
   * <p>WARNING: no bounds checking is performed.</p>
   *
   * @param array the array to search.
   * @param key the key to search for.
   * @param startIndex the start index to start looking at (inclusive).
   * @param endIndex the end index to stop looking at (inclusive).
   * @return the index where key (or the greatest value less than key) is located.
   */
  public static int binarySearch(final float[] array, final float key, final int startIndex, final int endIndex, final int missingOffset) {
    final int keyBits = Float.floatToIntBits(key);
    int min = startIndex;
    int max = endIndex;
    int mid;
    float value;

    while (min <= max) {
      mid = (min + max) >>> 1;
      value = array[mid];
      if (value < key) {
        min = mid + 1;
      } else if (value > key) {
        max = mid - 1;
      } else {
        final int valueBits = Float.floatToIntBits(value);
        if (valueBits == keyBits) {
          return mid;
        } else if (valueBits < keyBits) {
          min = mid + 1;
        } else {
          max = mid - 1;
        }
      }
    }
    return min + missingOffset;
  }

  /**
   * Fastest possible binary search.
   *
   * <p>WARNING: no bounds checking is performed.</p>
   *
   * @param array the array to search.
   * @param key the key to search for.
   * @param startIndex the start index to start looking at (inclusive).
   * @param endIndex the end index to stop looking at (inclusive).
   * @return the index where key (or the greatest value less than key) is located.
   */
  public static int binarySearch(final double[] array, final double key, final int startIndex, final int endIndex, final int missingOffset) {
    final long keyBits = Double.doubleToLongBits(key);
    int min = startIndex;
    int max = endIndex;
    int mid;
    double value;

    while (min <= max) {
      mid = (min + max) >>> 1;
        value = array[mid];
        if (value < key) {
          min = mid + 1;
        } else if (value > key) {
          max = mid - 1;
        } else {
          final long valueBits = Double.doubleToLongBits(value);
          if (valueBits == keyBits) {
            return mid;
          } else if (valueBits < keyBits) {
            min = mid + 1;
          } else {
            max = mid - 1;
          }
        }
    }
    return min + missingOffset;
  }

  /**
   * Fastest possible binary search.
   *
   * <p>WARNING: no bounds checking is performed.</p>
   *
   * @param array the array to search.
   * @param key the key to search for.
   * @param startIndex the start index to start looking at (inclusive).
   * @param endIndex the end index to stop looking at (inclusive).
   * @return the index where key (or the greatest value less than key) is located.
   */
  public static <T extends Comparable<T>> int binarySearch(final T[] array, final T key, final int startIndex, final int endIndex, final int missingOffset) {
    int min = startIndex;
    int max = endIndex;
    int mid;
    T value;

    while (min <= max) {
      mid   = (min + max) >>> 1;
          value = array[mid];
          final int cmp = value.compareTo(key);
          if (cmp < 0) {
            min = mid + 1;
          } else if (cmp > 0) {
            max = mid - 1;
          } else {
            return mid;
          }
    }
    return min + missingOffset;
  }

  /**
   * Returns the total number of cells in a matrix
   *
   * @param matrix the matrix
   * @return the total number of cells
   */
  @SuppressWarnings("unused")
  public static <T> int size(T[][] matrix) {
    int x = 0;
    for (T[] a : matrix) {
      for (T b : a) {
        ++x;
      }
    }
    return x;
  }

  /**
   * Returns the total number of cells in a matrix
   *
   * @param matrix the matrix
   * @return the total number of cells
   */
  public static <T> int size(List<List<T>> matrix) {
    int x = 0;
    for (List<T> a : matrix) {
      x += a.size();
    }
    return x;
  }

  /**
   * Returns true if a is equivalent to b.
   *
   * @param a left hand side
   * @param b right hand side
   * @return true if a is equivalent to b
   */
  public static boolean equals(Object a, Object b) {
    return (a == null) ? (b == null) : a.equals(b);
  }

  /**
   * Returns true if a is equivalent to b.
   *
   * @param a left hand side
   * @param b right hand side
   * @return true if a is equivalent to b
   */
  public static boolean equals(float a, float b) {
    return Float.floatToIntBits(a) == Float.floatToIntBits(b);
  }

  /**
   * Returns true if a is equivalent to b.
   *
   * @param a left hand side
   * @param b right hand side
   * @return true if a is equivalent to b
   */
  public static boolean equals(double a, double b) {
    return Double.doubleToLongBits(a) == Double.doubleToLongBits(b);
  }

  /**
   * Compares if 2 lists of {@link Throwable}s are equivalent.
   *
   * <p>Uses {@link #equals(Throwable, Throwable)} for comparing elements.</p>
   */
  public static boolean equals(List<? extends Throwable> a, List<? extends Throwable> b) {
    if (a == b) {
      return true;
    } else if ((a == null) || (b == null)) {
      return false;
    } else if (a.size() != b.size()) {
      return false;
    } else {
      final Iterator<? extends Throwable> ai = a.iterator();
      final Iterator<? extends Throwable> bi = b.iterator();
      while (ai.hasNext() && bi.hasNext()) {
        if (!equals(ai.next(), bi.next())) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Compares equality of 2 {@link Throwable}s.
   *
   * <p>Equality check will validate a and b are of the same type and have same message.</p>
   */
  public static boolean equals(Throwable a, Throwable b) {
    if (a == b) {
      return true; // true if both are null or are same object reference
    } else if ((a == null) || (b == null)) {
      return false;
    } else {
      return a.getClass().equals(b.getClass()) && equals(a.getMessage(), b.getMessage());
    }
  }


  /**
   * Return the output of a.compareTo(b)
   *
   * Safe if a or b is null
   *
   * @param a left hand side
   * @param b right hand side
   * @return true if a is equivalent to b
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static int compareTo(Comparable a, Comparable b) {
    if (a == null) return (b == null) ? 0 : 1;
    if (b == null) return -1;
    return a.compareTo(b);
  }

  /**
   * Converts an arbitrary collection into a sorted list.
   * @param collec the collection to convert
   * @return the sorted list
   */
  public static <T extends Comparable<? super T>> List<T> toSortedList(Collection<T> collec) {
    ArrayList<T> list = new ArrayList<T>(collec);
    Collections.sort(list);
    return list;
  }

  /**
   * Converts an arbitrary object to a String.
   *
   * @param value the value to convert
   * @param defaultValue if value is null
   * @return the value as a String, or defaultValue if conversion fails.
   */
  public static String toStringValue(Object value, String defaultValue) {
    if (value == null) {
      return defaultValue;
    } else {
      return value.toString();
    }
  }

  /**
   * Parse an arbitrary object as an integer.
   */
  public static int parseInt(Object value) {
    if (value == null) {
      throw new IllegalArgumentException("Cannot parse null as integer.");
    } else if (value instanceof Number) {
      return ((Number) value).intValue();
    } else {
      return Integer.parseInt(value.toString());
    }
  }

  /**
   * Converts an arbitrary object to a primitive integer.
   *
   * @param value the value to convert
   * @param defaultValue if value cannot be converted, this will be returned
   * @return the value as a int, or defaultValue if conversion fails.
   */
  public static int toIntValue(Object value, int defaultValue) {
    try {
      // WARNING: do not change this to call ObjectUtils.parseInt() thinking its better,
      //          this will result in hotspot since parseInt will throw exception for null case and
      //          null is the most common value passed in here
      if (value == null) {
        return defaultValue;
      } else if (value instanceof Number) {
        return ((Number) value).intValue();
      } else {
        return Integer.parseInt(value.toString());
      }
    } catch (IllegalArgumentException e) {
      return defaultValue;
    }
  }

  /**
   * Converts an arbitrary object to a primitive unsigned integer.
   *
   * @param value the value to convert
   * @param defaultValue if value cannot be parsed, this will be returned
   * @return the value as a int, or defaultValue if conversion fails.
   */
  public static int toUnsignedValue(Object value, int defaultValue) {
    try {
      return parseUnsigned(value);
    } catch (IllegalArgumentException e) {
      return defaultValue;
    }
  }

  /**
   * Parses an arbitrary object into an unsigned integer.
   * @param value the value to convert
   * @return the value as an int
   * @throws NumberFormatException if object cannot be converted to int
   * @throws IllegalArgumentException if integer value is negative
   */
  public static int parseUnsigned(Object value) throws NumberFormatException, IllegalArgumentException {
    final int unsigned;

    // Convert object to integer
    if (value == null) {
      throw new IllegalArgumentException("expected unsigned, got null");
    } else if (value instanceof Number) {
      unsigned = ((Number)value).intValue();
    } else {
      unsigned = Integer.parseInt( value.toString() );
    }

    // Handle negatives
    if (unsigned < 0) {
      throw new IllegalArgumentException(String.format("expected unsigned, got %d", unsigned));
    } else {
      return unsigned;
    }
  }


  /**
   * Converts an arbitrary object to a primitive long.
   *
   * @param value the value to convert
   * @param defaultValue if value cannot be converted, this will be returned
   * @return the value as a long, or defaultValue if conversion fails.
   */
  public static long toLongValue(Object value, long defaultValue) {
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Number) {
      return ((Number) value).longValue();
    } else {
      try {
        return Long.parseLong(value.toString());
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
  }

  /**
   * Converts an arbitrary object to a primitive float.
   *
   * @param value the value to convert
   * @param defaultValue if value cannot be converted, this will be returned
   * @return the value as a float, or defaultValue if conversion fails.
   */
  public static float toFloatValue(Object value, float defaultValue) {
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Number) {
      return ((Number) value).floatValue();
    } else {
      try {
        return Float.parseFloat(value.toString());
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
  }

  /**
   * Parse an arbitrary object as a double.
   */
  public static double parseDouble(Object value) {
    if (value == null) {
      throw new IllegalArgumentException("Cannot parse null as double.");
    } else if (value instanceof Number) {
      return ((Number) value).doubleValue();
    } else {
      return Double.parseDouble(value.toString());
    }
  }

  /**
   * Converts an arbitrary object to a primitive double.
   *
   * @param value the value to convert
   * @param defaultValue if value cannot be converted, this will be returned
   * @return the value as a double, or defaultValue if conversion fails.
   */
  public static double toDoubleValue(Object value, double defaultValue) {
    try {
      return parseDouble(value);
    } catch (IllegalArgumentException e) {
      return defaultValue;
    }
  }

  /**
   * Converts an arbitrary object to a primitive boolean.
   *
   * If value is a Boolean instance, return its value. If value is a Number instance, return true if it is non-zero. true will be returned if the value's lower cased string representation is true,
   * yes, or on. false will be returned if values' lower cased string representation is false, no, or off. non-zero numbers will be return true, 0 returns false. Otherwise, the defaultValue will be
   * returned.
   *
   * @param value the value to convert
   * @param defaultValue if value cannot be converted, this will be returned
   * @return the value as a boolean, or defaultValue if conversion fails.
   */
  public static boolean toBooleanValue(Object value, boolean defaultValue) {
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof Number) {
      return ((Number) value).intValue() != 0;
    } else {
      Boolean b = parseBoolean(value.toString());
      if (b == null)
        return defaultValue;
      else
        return b;
    }
  }


  /**
   * Returns true if the string <code>value</code> is a boolean value indicating <code>true</code>.
   */
  public static boolean isTrue(final String value) {
    for (String t : BOOLEAN_TRUE) {
      if (value.equals(t)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Returns true if the string <code>value</code> is a boolean value indicating <code>false</code>.
   */
  public static boolean isFalse(final String value) {
    for (String t : BOOLEAN_FALSE) {
      if (value.equals(t)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Parse a boolean value (more lenient than Boolean.valueOf()).
   *
   * <p>true will be returned if the value's lower cased string representation is true, yes, or on.
   * false will be returned if values' lower cased string representation is false, no, or off.
   * non-zero numbers will be return true, 0 returns false.</p>
   *
   * @param value
   * @return the value or null if not parseable as a boolean
   */
  public static Boolean parseBoolean(String value) {
    if (value == null)
      return null;
    value = value.toLowerCase();

    if (isTrue(value)) {
      return true;
    }

    if (isFalse(value)) {
      return false;
    }

    // Try to treat string as a number
    try {
      return Integer.parseInt(value) != 0;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Convert <code>value</code> to a Boolean (or null if not parseable).
   */
  public static Boolean parseBoolean(Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else if (value instanceof Number) {
      return Boolean.valueOf( ((Number)value).intValue() != 0 );
    } else {
      return parseBoolean(value.toString());
    }
  }

  /** Pretty prints a 2-D int array */
  public static void prettyPrint(int[][] matrix) {
    final PrintStream out = System.out;

    if (matrix != null) {
      out.println("{");
      for (int i = 0; i < matrix.length; ++i) {
        if (matrix[i] != null) {
          out.print("  { ");
          for (int j = 0; j < matrix[i].length; ++j) {
            out.print(" " + matrix[i][j] + ",");
          }
          out.println(" },");
        } else {
          out.println("  null,");
        }
      }
      out.println("}");
    } else {
      out.println("null");
    }
  }

  /** Returns the exception of 'type' if anywhere in the exception's cause tree a class of 'type' is found, null otherwise*/
  @SuppressWarnings("unchecked")
  public static <T extends Throwable> T getCause(Throwable ex, Class<T> type) {
    if (ex.getCause() == null) {
      // we got all the way down and found no matches
      return null;
    } else {
      if (type.isAssignableFrom(ex.getCause().getClass())) {
        // a match!
        return (T) ex.getCause();
      } else {
        // keep walking
        return getCause(ex.getCause(), type);
      }
    }
  }

  /** Creates a new {@link LinkedHashMap} implementation based on key value pairs specified as paired arguments.
   *
   * The first key/value are used for compiler time type safety and are put into the map.  All of the other arguments
   * are NOT validated for type safety.
   *
   * @throws IllegalArgumentException if an odd number of arguments are specified
   **/
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> newMap(K key, V val, Object ... args) throws IllegalArgumentException {
    if (args.length % 2 != 0) {
      throw new IllegalArgumentException("odd number of arguments, requires paired arguments");
    }
    Map<K, V> map = new LinkedHashMap<K, V>();
    map.put(key, val);
    for (int i=0; i<args.length-1; i=i+2) {
      map.put((K) args[i], (V) args[i+1]);
    }
    return map;
  }

  /** Creates a new {@link List} implementation based on ordered input values.  Same as Arrays.asList() but provides type safety
   *
   * @param <T> the type of generic list to create
   * @param vals the values to add to the list
   */
  @SafeVarargs
  public static <T> List<T> newList(T ... vals) {
    return Arrays.asList(vals);
  }


  // Hash code helper utils

  /**
   * Compute the hash code of a primitive <code>boolean</code>.
   */
  public static int hashCode(final boolean value) {
    return value ? 1231 : 1237;
  }

  /**
   * Compute the hash code of a primitive <code>float</code>.
   */
  public static int hashCode(final float value) {
    return Float.floatToIntBits(value);
  }

  /**
   * Compute the hash code of a primitive <code>double</code>.
   */
  public static int hashCode(final double value) {
    return hashCode(Double.doubleToLongBits(value));
  }

  /**
   * Compute the hash code of a primitive <code>long</code>.
   */
  public static int hashCode(final long value) {
    return (int)(value ^ (value >>> 32));
  }

  /**
   * Compute the hash code of a segment of an int[].
   */
  public static int hashCode(final int[] data, final int start, final int count) {
    if (start < 0) throw new IllegalArgumentException("start index less than 0");
    if (data != null) {
      final int end = Math.min(start + count, data.length);
      int hash = count;
      for (int i = start; i < end; ++i) {
        hash = hashCode(hash, data[i]);
      }
      return hash;
    }
    return 0;
  }

  /**
   * Compute the hash code of a segment of an int[].
   */
  public static int hashCode(final double[] data, final int start, final int count) {
    if (start < 0) throw new IllegalArgumentException("start index less than 0");
    if (data != null) {
      final int end = Math.min(start + count, data.length);
      int hash = count;
      for (int i = start; i < end; ++i) {
        hash = hashCode(hash, hashCode(data[i]));
      }
      return hash;
    }
    return 0;
  }

  /**
   * Add the hashCode for <code>value</code> to <code>hash</code>.
   */
  public static int hashCode(final int hash, final boolean value) {
    return hashCode(hash, hashCode(value));
  }

  /**
   * Add the hashCode for <code>value</code> to <code>hash</code>.
   */
  public static int hashCode(final int hash, final float value) {
    return hashCode(hash, hashCode(value));
  }

  /**
   * Add the hashCode for <code>value</code> to <code>hash</code>.
   */
  public static int hashCode(final int hash, final double value) {
    return hashCode(hash, hashCode(value));
  }

  /**
   * Add the hashCode for <code>value</code> to <code>hash</code>.
   */
  public static int hashCode(final int hash, final long value) {
    return hashCode(hash, hashCode(value));
  }

  /**
   * Add the hashCode for <code>value</code> to <code>hash</code>.
   */
  public static int hashCode(final int hash, final Object value) {
    return hashCode(hash, hashCode(value));
  }

  /** Returns the value's hascode or 0 if null. */
  public static int hashCode(final Object value) {
    return value == null ? 0 : value.hashCode();
  }

  /**
   * Add the hashCode for <code>value</code> to <code>hash</code>.
   */
  public static int hashCode(final int hash, final int value) {
    return hash * 31 + value;
  }


}
