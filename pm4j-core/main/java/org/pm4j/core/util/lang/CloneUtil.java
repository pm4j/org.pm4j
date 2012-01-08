package org.pm4j.core.util.lang;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * Allows to call the clone functionality for objects without having direct knowledge about
 * the subclass specific clone() method declaration.
 *
 * @author olaf boede
 */
public final class CloneUtil {

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
  private static Map<Class<?>, Method> cloneMethodMap = Collections.synchronizedMap(new HashMap<Class<?>, Method>());

}
