package org.pm4j.core.util.lang;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
   * @return The clone list.
   */
  public static <T extends Cloneable> List<T> cloneList(List<T> srcList) {
    if (srcList == null) {
      return null;
    }
    List<T> cloneList = new ArrayList<T>(srcList.size());
    for (T srcItem : srcList) {
      cloneList.add(clone(srcItem));
    }
    return cloneList;
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
//        if (cloneMethod == null) {
//          throw new IllegalArgumentException("Cloneable class '" + objClass.getName() + "' does not implement a public clone() method.");
//        }
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
