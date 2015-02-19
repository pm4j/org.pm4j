package org.pm4j.core.pm.impl;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

public final class ResKeyUtil {

  public static String shortResKeyForClass(Class<?> cls) {
    // ClassUtils.getShortClassName support also nested class names.
    // Because of that we don't use Class.getSimpleName() here.
    return StringUtils.uncapitalize(ClassUtils.getShortClassName(cls));
  }

  /**
   * Creates a string based on the short class name and the toString result of
   * the given instance.
   * <p>
   * Example: For the value BAR of the enum Foo this method will generate
   * 'Foo.BAR'.
   *
   * @param value
   *          The instance to generate the string for.
   * @return The generated string. <code>null</code> in case of a
   *         <code>null</code> argument.
   */
  public static String classNameAndValue(Object value) {
    if (value == null) {
      return null;
    }
    StringBuilder str = new StringBuilder();
    str.append(ClassUtils.getShortClassName(value.getClass()));
    str.append('.');
    str.append(value.toString());
    return str.toString();
  }

}
