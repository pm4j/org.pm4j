package org.pm4j.common.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A utility to retrieve Java-generics parameter information from a
 * {@link Class} instance.
 *
 * @author mhoennig, oboede
 */
public class GenericsUtil {

  /**
   * Reads the first Generics parameter from the given {@link Class}.
   * <p>
   * Example:<br>
   * For subclasses of class MyClass<T1, T2> this method will return T1.class.
   *
   * @param cls
   *          The class to analyze.
   * @return The found generics parameter. <code>null</code> if none was found.
   */
  public static Type findFirstSuperClassParameterType(Class<?> cls) {
    if (cls.getGenericSuperclass() instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) cls.getGenericSuperclass();
      Type[] typeArray = pt.getActualTypeArguments();
      return (typeArray.length > 0)
          ? typeArray[0]
          : null;
    } else {
    return findFirstSuperClassParameterType(cls.getSuperclass());
    }
  }

}
