package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmDefaults;

/** Internal helper methods for DI resolution. */
public class DiResolverUtil  {

  private static final DiResolver[] EMPTY_ARRAY = new DiResolver[] {};

  public static DiResolver[] getDiResolvers(Class<?> forClass) {
    List<DiResolver> resolverList = new ArrayList<DiResolver>();
    for (DiResolverFactory f : PmDefaults.getInstance().getDiResolverFactories()) {
      DiResolver r = f.makeDiResolver(forClass);
      if (r != null) {
        resolverList.add(r);
      }
    }
    return resolverList.isEmpty()
        ? EMPTY_ARRAY
        : resolverList.toArray(new DiResolver[resolverList.size()]);
  }

  static void ensureAccessibility(Field f) {
    // TODO olaf: Check if there is a public setter to prevent some trouble
    //            in case of enabled security manager...
    if (! f.isAccessible()) {
      f.setAccessible(true);
    }
  }

  static void ensureAccessibility(Method m) {
    // TODO olaf: Check if there is a public setter to prevent some trouble
    //            in case of enabled security manager...
    if (! m.isAccessible()) {
      m.setAccessible(true);
    }
  }

}
