package org.pm4j.core.util.lang;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * Allows to call the clone functionality for objects without having direct knowledge about
 * the subclass specific clone() method declaration.
 *
 * @author olaf boede
 */
public final class CloneUtil {

  /**
   * Clones all items of the given list to a new one.
   *
   * @param srcList The list to clone.
   * @return The cloned list.
   */
  public static <T extends Cloneable> List<T> cloneList(List<T> srcList) {
    return cloneList(srcList, false);
  }

  public static <T> List<T> cloneList(List<T> srcList, boolean deepClone) {
    if (srcList == null) {
      return null;
    }
    List<T> cloneList = new ArrayList<T>(srcList.size());
    for (T srcItem : srcList) {
      @SuppressWarnings("unchecked")
      T item = deepClone
          ? deepClone(srcItem)
          : (T)clone((Cloneable)srcItem);
      cloneList.add(item);
    }
    return cloneList;
  }

  /**
   * Clones all values of the given map.<br>
   * The keys do not get cloned!
   *
   * @param srcMap the source map.
   * @return the cloned map. <code>null</code> if the srcMap was <code>null</code>.
   */
  public static <K, V extends Cloneable> Map<K, V> cloneMap(Map<K, V> srcMap) {
    return cloneMap(srcMap, false);
  }

  public static <K, V> Map<K, V> cloneMap(Map<K, V> srcMap, boolean deepClone) {
    if (srcMap == null) {
      return null;
    }
    // create a map of the same type:
    Map<K, V> map = new HashMap<K, V>();

    for (Map.Entry<K, V> e : srcMap.entrySet()) {
      @SuppressWarnings("unchecked")
      V item = deepClone
          ? deepClone(e.getValue())
          : (V)clone((Cloneable)e.getValue());
      map.put(e.getKey(), item);
    }
    return map;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> T deepClone(T ori) {
    if (ori == null) {
      return null;
    }
    if (ori instanceof List) {
      return (T)cloneList((List)ori, true);
    }
    if (ori instanceof Map) {
      return (T)cloneMap((Map)ori, true);
    }
    if (ori instanceof Cloneable) {
      return (T)clone((Cloneable)ori);
    }

    throw new IllegalArgumentException("Can't clone type: " + ori.getClass());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Cloneable> T clone(T ori) {
    if (ori == null) {
      return null;
    }

    Class<?> objClass = ori.getClass();


    Method cloneMethod = cloneMethodMap.get(objClass);

    if (cloneMethod == null) {
      try {
        cloneMethod = objClass.getMethod("clone");
      } catch (Exception e) {
        CheckedExceptionWrapper.throwAsRuntimeException(e);
      }

      cloneMethodMap.put(objClass, cloneMethod);
    }

    Object clone = null;

    try {
      clone = cloneMethod.invoke(ori);
    } catch (Exception e) {
      CheckedExceptionWrapper.throwAsRuntimeException(e);
    }

    return (T) clone;
  }


  /**
   * A performance helper map for the reflection based generic clone implementation.
   */
  private static Map<Class<?>, Method> cloneMethodMap = new ConcurrentHashMap<Class<?>, Method>();

}
