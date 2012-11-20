package org.pm4j.common.util.collection;

import org.apache.commons.lang.ObjectUtils;

public class ArrayUtil {

  /**
   * Checks if all items of the given arrays are equal.
   * 
   * @param <T>
   * @param a1
   * @param a2
   * @return
   */
  public static <T> boolean equals(T[] a1, T[] a2) {
    return equals(a1, a2, -1);
  }

  /**
   * Checks if all items of the given arrays are equal.
   * 
   * @param <T>
   * @param a1
   * @param a2
   * @return
   */
  public static <T> boolean equals(T[] a1, T[] a2, int numOfItemsToCompare) {
    if (a1 == a2)
      return true;

    if (a1 == null || a2 == null) 
      return false;
    
    if (a1.length != a2.length)
      return false;
    
    int compItemCount = numOfItemsToCompare != -1 ? numOfItemsToCompare : a1.length;
    
    for (int i=0; i<compItemCount; ++i) {
      if (! ObjectUtils.equals(a1[i], a2[i]))
        return false;
    }
    
    // everything is equal:
    return true;
  }
  
  /**
   * Generates an copy of the array with some size modification.
   * TODOC
   * 
   * @param <T>
   * @param oriArray
   * @param newArraySize
   * @param startOffset
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] copyOf(T[] oriArray, int newArraySize, int startOffset) {
    // In Java 6 this would be a simple: Arrays.copyOf(msgArgs, argCount+1);
    // But the code should stay compatible to Java 5 for now.
    Object[] targetArray = new Object[newArraySize];

    if (oriArray != null) {
      for (int i=startOffset, count=0; i<newArraySize && count<oriArray.length; ++i, ++count) {
        targetArray[i] = oriArray[count];
      }
    }
    
    return (T[]) targetArray;
  }

  public static <T> T[] copyOf(T[] oriArray, int newArraySize) {
    return copyOf(oriArray, newArraySize, 0);
  }
}
