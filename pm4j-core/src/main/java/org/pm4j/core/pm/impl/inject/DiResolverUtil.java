package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;

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

  /** 
   * @throws PmRuntimeException if the field can't be access or already has a value.
   * 
   * @param pm the instance to validate
   * @param field the field to validate
   */
  static void validateFieldIsNull(PmObject pm, Field f) {
    Object value = null;
    try {
      value = f.get(pm);
    } catch (Exception ex) {
      throw new PmRuntimeException(pm, "Can't read field '" + f.getName() + "' in class '"
          + pm.getClass().getName() + "'.", ex);
    }
    if (value != null) {
      throw new PmRuntimeException(pm, "Can't initialize field '" + f.getName() + "' in class '"
          + pm.getClass().getName() + "'.  Already has value: " + value );
    }
  }

  /** 
   * @throws PmRuntimeException if the getter can't be access or returns anything other than null
   * 
   * @param pm the instance to validate
   * @param setter the setter for which a getter is to be found and used for validation
   */
  static void validateGetterReturnsNull(PmObject pm, Method setter) {
    String getterName = setter.getName().replaceFirst("^set", "get");
    Object value = null;
    try {
      Method getter = setter.getDeclaringClass().getMethod(getterName);
      value = getter.invoke(pm);
    } catch (Exception ex) {
      throw new PmRuntimeException(pm, "Can't invoke getter for '" + setter.getName() + "' in class '"
          + pm.getClass().getName() + "'.", ex);
    }
    if (value != null) {
      throw new PmRuntimeException(pm, "Can't initialize setter '" + setter.getName() + "' in class '"
          + pm.getClass().getName() + "'.  Already has value: " + value );
    }
  }

  /**
   * @throws PmRuntimeException if 'value' is null but null not allowed
   * @param pm the related Pm instance
   * @param nullAllowed whether a null value is allowed or not
   * @param target related field or method
   * @param value the value to validate
   */
  static void validateValidValue(PmObject pm, boolean nullAllowed, AccessibleObject target, Object value) {
    if (value == null && !nullAllowed) {
      throw new PmRuntimeException(pm, "Found value for dependency injection of '" + target +
          "' was null. But null value is not allowed. " +
          "You may configure null-value handling using @PmInject(nullAllowed=...).");
    }
  }

  /**
   * Sets the given field in the given Pm instance to a new value.
   * 
   * @param pm the Pm instance
   * @param field the field to set
   * @param value the value to set into the field
   */
  static void setValue(PmObject pm, Field field, Object value) {
    try {
      field.set(pm, value);
    } catch (Exception ex) {
      throw new PmRuntimeException(pm, "Can't initialize field '" + field.getName() + "' in class '"
          + field.getDeclaringClass().getName() + "'.", ex);
    }
  }

  /**
   * Calls the given setter in the given Pm instance with the given value.
   * 
   * @param pm the Pm instance
   * @param method the setter method to call
   * @param value the value to set into the field
   */
  static void setValue(PmObject pm, Method method, Object value) {
    try {
      method.invoke(pm, value);
    } catch (Exception ex) {
      throw new PmRuntimeException(pm, "Can't invoke method '" + method.getName() + "' in class '" + pm.getClass().getName() + "'.", ex);
    }
  }

  /**
   * @return the value as resolved by the given resolver on the given Pm 
   * @param pm the Pm instance
   * @param target the target field or setter (just for the error message)
   * @param resolver the resolver to use
   */
  static Object resolveValue(PmObject pm, AccessibleObject target, PathResolver resolver) {
    Object value = null;
    try {
      value = resolver.getValue(pm);
    } catch (RuntimeException ex) {
      throw new PmRuntimeException(pm, "Unable to resolve dependency injection reference to '" + resolver + "' for: " + target, ex);
    }
    return value;
  }
}
