package org.pm4j.core.pm.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInit;

/**
 * Core internal utility for common internal tasks.
 *
 * @author olaf boede
 */
final class InternalPmObjectUtil {

  /**
   * @param pm the PM to find an annotated init method for.
   * @return all methods in the class hierarchy of this PM that are annotated with {@link PmInit}.
   *
   */
  static List<Method> findInitMethods(PmObject pm) {
    List<Method> initMethods = AnnotationUtil.findAnnotatedMethodsTopDown(pm.getClass(), PmInit.class);
    Map<String, Method> nameToMethodMap = new HashMap<String, Method>();

    for (Iterator<Method> iter = initMethods.listIterator(); iter.hasNext();) {
      Method method = iter.next();
      // only no-arg methods are allowed
      if (method.getParameterTypes().length > 1) {
        throw new IllegalArgumentException("Methods annotated with '" + PmInit.class
            + "' can not have parameters. This is not true for '" + method + "'. Please rafactore the method!");
      }
      // no static methods are allowed
      if (Modifier.isStatic(method.getModifiers())) {
        throw new IllegalArgumentException("Methods annotated with '" + PmInit.class
            + "' must not be static. This is not true for '" + method
            + "'. Please rafactore the method!");
      }
      // only public and protected methods are allowed
      if (!Modifier.isPublic(method.getModifiers()) && !Modifier.isProtected(method.getModifiers())) {
        throw new IllegalArgumentException("Methods annotated with '" + PmInit.class
            + "' must be public or protected. This is not true for '" + method
            + "'. Please change the method visibility!");
      }
      // If a sub class overrides an annotated super class init method it must
      // be ensured that the init method is only called once.
      if (nameToMethodMap.get(method.getName()) != null) {
        iter.remove();
      } else {
        nameToMethodMap.put(method.getName(), method);
      }
      // if onPmInit is annotated, do not call it twice
      if (method.getName().equals("onPmInit")) {
        iter.remove();
      }
    }

    return initMethods;
  }

}