package com.zbiljic.switchz;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Operations on arrays, primitive arrays (like int[]) and primitive wrapper arrays (like
 * Integer[]).
 * <p>
 * This class tries to handle null input gracefully. An exception will not be thrown for a null
 * array input. However, an Object array that contains a null element may throw an exception. Each
 * method documents its behaviour.
 */
@ThreadSafe
class ArrayUtils {

  private ArrayUtils() { /* No instance methods */ }

  /**
   * An empty immutable {@code char} array.
   */
  public static final char[] EMPTY_CHAR_ARRAY = new char[0];

  // Clone
  //------------------------------------------------------------------------

  /**
   * Shallow clones an array returning a typecast result and handling {@code null}.
   * <p>
   * The objects in the array are not cloned, thus there is no special handling for
   * multi-dimensional arrays.
   * <p>
   * This method returns {@code null} for a {@code null} input array.
   *
   * @param <T>   the component type of the array
   * @param array the array to shallow clone, may be {@code null}
   * @return the cloned array, {@code null} if {@code null} input
   */
  public static <T> T[] clone(final T[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  /**
   * Clones an array returning a typecast result and handling {@code null}.
   * <p>
   * This method returns {@code null} for a {@code null} input array.
   *
   * @param array the array to clone, may be {@code null}
   * @return the cloned array, {@code null} if {@code null} input
   */
  public static char[] clone(final char[] array) {
    if (array == null) {
      return null;
    }
    return array.clone();
  }

  // Subarrays
  //------------------------------------------------------------------------

  /**
   * Produces a new array containing the elements between the start and end indices.
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input produces null output.
   * <p>
   * The component type of the subarray is always the same as that of the input array. Thus, if the
   * input is an array of type {@code Date}, the following usage is envisaged:
   * <p>
   * <pre>
   * Date[] someDates = (Date[]) ArrayUtils.subarray(allDates, 2, 5);
   * </pre>
   *
   * @param <T>                 the component type of the array
   * @param array               the array
   * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *                            (&gt;array.length) results in an empty array.
   * @param endIndexExclusive   elements up to endIndex-1 are present in the returned subarray.
   *                            Undervalue (&lt; startIndex) produces empty array, overvalue
   *                            (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end indices.
   * @see Arrays#copyOfRange(Object[], int, int)
   */
  public static <T> T[] subarray(final T[] array, int startIndexInclusive, int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    final int newSize = endIndexExclusive - startIndexInclusive;
    final Class<?> type = array.getClass().getComponentType();
    if (newSize <= 0) {
      @SuppressWarnings("unchecked") // OK, because array is of type T
      final T[] emptyArray = (T[]) Array.newInstance(type, 0);
      return emptyArray;
    }
    @SuppressWarnings("unchecked") // OK, because array is of type T
    final T[] subarray = (T[]) Array.newInstance(type, newSize);
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  /**
   * Produces a new {@code char} array containing the elements between the start and end indices.
   * <p>
   * The start index is inclusive, the end index exclusive. Null array input produces null output.
   *
   * @param array               the array
   * @param startIndexInclusive the starting index. Undervalue (&lt;0) is promoted to 0, overvalue
   *                            (&gt;array.length) results in an empty array.
   * @param endIndexExclusive   elements up to endIndex-1 are present in the returned subarray.
   *                            Undervalue (&lt; startIndex) produces empty array, overvalue
   *                            (&gt;array.length) is demoted to array length.
   * @return a new array containing the elements between the start and end indices.
   * @see Arrays#copyOfRange(char[], int, int)
   */
  public static char[] subarray(final char[] array, int startIndexInclusive, int endIndexExclusive) {
    if (array == null) {
      return null;
    }
    if (startIndexInclusive < 0) {
      startIndexInclusive = 0;
    }
    if (endIndexExclusive > array.length) {
      endIndexExclusive = array.length;
    }
    final int newSize = endIndexExclusive - startIndexInclusive;
    if (newSize <= 0) {
      return EMPTY_CHAR_ARRAY;
    }

    final char[] subarray = new char[newSize];
    System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
    return subarray;
  }

  //------------------------------------------------------------------------

  /**
   * Adds all the elements of the given arrays into a new array.
   * <p>
   * The new array contains all of the element of {@code array1} followed by all of the elements
   * {@code array2}. When an array is returned, it is always a new array.
   * <p>
   * <pre>
   * ArrayUtils.addAll(null, null)     = null
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * ArrayUtils.addAll([null], [null]) = [null, null]
   * ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
   * </pre>
   *
   * @param <T>    the component type of the array
   * @param array1 the first array whose elements are added to the new array, may be {@code null}
   * @param array2 the second array whose elements are added to the new array, may be {@code null}
   * @return The new array, {@code null} if both arrays are {@code null}. The type of the new array
   * is the type of the first array, unless the first array is null, in which case the type is the
   * same as the second array.
   * @throws IllegalArgumentException if the array types are incompatible
   */
  public static <T> T[] addAll(final T[] array1, final T... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    final Class<?> type1 = array1.getClass().getComponentType();
    @SuppressWarnings("unchecked") // OK, because array is of type T
    final T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    try {
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    } catch (final ArrayStoreException ase) {
      // Check if problem was due to incompatible types
      /*
       * We do this here, rather than before the copy because:
       * - it would be a wasted check most of the time
       * - safer, in case check turns out to be too strict
       */
      final Class<?> type2 = array2.getClass().getComponentType();
      if (!type1.isAssignableFrom(type2)) {
        throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
          + type1.getName(), ase);
      }
      throw ase; // No, so rethrow original
    }
    return joinedArray;
  }

  /**
   * Adds all the elements of the given arrays into a new array.
   * <p>
   * The new array contains all of the element of {@code array1} followed by all of the elements
   * {@code array2}. When an array is returned, it is always a new array.
   * <p>
   * <pre>
   * ArrayUtils.addAll(array1, null)   = cloned copy of array1
   * ArrayUtils.addAll(null, array2)   = cloned copy of array2
   * ArrayUtils.addAll([], [])         = []
   * </pre>
   *
   * @param array1 the first array whose elements are added to the new array.
   * @param array2 the second array whose elements are added to the new array.
   * @return The new char[] array.
   */
  public static char[] addAll(final char[] array1, final char... array2) {
    if (array1 == null) {
      return clone(array2);
    } else if (array2 == null) {
      return clone(array1);
    }
    final char[] joinedArray = new char[array1.length + array2.length];
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    return joinedArray;
  }

  /**
   * Copies the given array and adds the given element at the end of the new array.
   * <p>
   * The new array contains the same elements of the input array plus the given element in the last
   * position. The component type of the new array is the same as that of the input array.
   * <p>
   * If the input array is {@code null}, a new one element array is returned whose component type is
   * the same as the element, unless the element itself is null, in which case the return type is
   * Object[]
   * <p>
   * <pre>
   * ArrayUtils.add(null, null)      = [null]
   * ArrayUtils.add(null, "a")       = ["a"]
   * ArrayUtils.add(["a"], null)     = ["a", null]
   * ArrayUtils.add(["a"], "b")      = ["a", "b"]
   * ArrayUtils.add(["a", "b"], "c") = ["a", "b", "c"]
   * </pre>
   *
   * @param <T>     the component type of the array
   * @param array   the array to "add" the element to, may be {@code null}
   * @param element the object to add, may be {@code null}
   * @return A new array containing the existing elements plus the new element. The returned array
   * type will be that of the input array (unless null), in which case it will have the same type as
   * the element. If both are null, an IllegalArgumentException is thrown
   * @throws IllegalArgumentException if both arguments are null
   */
  public static <T> T[] add(final T[] array, final T element) {
    Class<?> type;
    if (array != null) {
      type = array.getClass().getComponentType();
    } else if (element != null) {
      type = element.getClass();
    } else {
      throw new IllegalArgumentException("Arguments cannot both be null");
    }
    @SuppressWarnings("unchecked") // type must be T
    final T[] newArray = (T[]) copyArrayGrow1(array, type);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * Copies the given array and adds the given element at the end of the new array.
   * <p>
   * The new array contains the same elements of the input array plus the given element in the last
   * position. The component type of the new array is the same as that of the input array.
   * <p>
   * If the input array is {@code null}, a new one element array is returned whose component type is
   * the same as the element.
   * <p>
   * <pre>
   * ArrayUtils.add(null, '0')       = ['0']
   * ArrayUtils.add(['1'], '0')      = ['1', '0']
   * ArrayUtils.add(['1', '0'], '1') = ['1', '0', '1']
   * </pre>
   *
   * @param array   the array to copy and add the element to, may be {@code null}
   * @param element the object to add at the last index of the new array
   * @return A new array containing the existing elements plus the new element
   */
  public static char[] add(final char[] array, final char element) {
    final char[] newArray = (char[]) copyArrayGrow1(array, Character.TYPE);
    newArray[newArray.length - 1] = element;
    return newArray;
  }

  /**
   * Returns a copy of the given array of size 1 greater than the argument.
   * The last value of the array is left to the default value.
   *
   * @param array                 The array to copy, must not be {@code null}.
   * @param newArrayComponentType If {@code array} is {@code null}, create a size 1 array of this
   *                              type.
   * @return A new copy of the array of size 1 greater than the input.
   */
  private static Object copyArrayGrow1(final Object array, final Class<?> newArrayComponentType) {
    if (array != null) {
      final int arrayLength = Array.getLength(array);
      final Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
      System.arraycopy(array, 0, newArray, 0, arrayLength);
      return newArray;
    }
    return Array.newInstance(newArrayComponentType, 1);
  }

}
