package org.pm4j.common.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.pm4j.common.util.reflection.GenericTypeUtil;

/**
 * A utility to retrieve Java-generics parameter information from a
 * {@link Class} instance.
 *
 * @author mhoennig, oboede
 * @deprecated Please use {@link GenericTypeUtil}.
 */
@Deprecated
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
    return findSuperClassParameterType(cls, 0);
  }

  /**
   * Reads the Java-Generics parameter having the given index position from the given {@link Class}.
   * <p>
   * Example:<br>
   * For subclasses of class MyClass<T1, T2> this findSuperClassParameterType(MyClass.class, 1) will return T2.class.
   *
   * @param cls
   *          The class to analyze.
   * @param paramIdx The parameter position index. Starts with zero for the first parameter.
   * @return The found Java-Generics parameter. <code>null</code> if none was found.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Type> T findSuperClassParameterType(Class<?> cls, int paramIdx) {
    if (cls.getGenericSuperclass() instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) cls.getGenericSuperclass();
      Type[] typeArray = pt.getActualTypeArguments();
      return (typeArray.length > paramIdx)
          ? (T) typeArray[paramIdx]
          : null;
    } else {
      Class<?> superCls = cls.getSuperclass();
      return (superCls != null)
        ? (T) findSuperClassParameterType(superCls, paramIdx)
        : null;
    }
  }

  /**
   * Imperative version of {@link #findSuperClassParameterType(Class, int)}.
   */
  public static <T extends Type> T getSuperClassParameterType(Class<?> cls, int paramIdx) {
    T t = findSuperClassParameterType(cls, paramIdx);
    if (t == null) {
      throw new RuntimeException("Unable to find a generics parameter with (zero based) index '" + paramIdx +
          "' for class " + cls);
    }
    return t;
  }
}
