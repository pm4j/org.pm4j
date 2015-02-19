package org.pm4j.core.pm.annotation.customize;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.pm4j.core.pm.PmObject;

/**
 * An pm4j internal utility with some custom annotation related implementations.
 * 
 * @author olaf boede
 */
public class CustomizedAnnotationUtil {

  static final Map<Class<? extends Annotation>, PermissionAnnotationHandler<?>> annotationClassToPermissionHandlerMap = new HashMap<Class<? extends Annotation>, PermissionAnnotationHandler<?>>();

  /**
   * Checks the permission annotation handlers for their additional editable
   * restrictions.
   *
   * @param pm
   *          the PM to check the permission annotations for.
   * @param annotations
   *          the set of permission annotations configured for the given PM.
   * @return <code>true</code> if the permission annotations did not add an
   *         editability restriction for the given PM.
   */
  public static boolean isEnabled(PmObject pm, Annotation... annotations) {
    for (Annotation a : annotations) {
      @SuppressWarnings("unchecked")
      PermissionAnnotationHandler<Annotation> h = (PermissionAnnotationHandler<Annotation>) annotationClassToPermissionHandlerMap.get(getAnnotationClass(a));
      if (h != null &&
          !h.isEnabled(pm, a)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks the permission annotation handlers for their additional visibilty
   * restrictions.
   *
   * @param pm
   *          the PM to check the permission annotations for.
   * @param annotations
   *          the set of permission annotations configured for the given PM.
   * @return <code>true</code> if the permission annotations did not add an
   *         visibility restriction for the given PM.
   */
  public static boolean isVisible(PmObject pm, Annotation... annotations) {
    for (Annotation a : annotations) {
      @SuppressWarnings("unchecked")
      PermissionAnnotationHandler<Annotation> h = (PermissionAnnotationHandler<Annotation>) annotationClassToPermissionHandlerMap.get(getAnnotationClass(a));
      if (h != null &&
          !h.isVisible(pm, a)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks the permission annotation handlers for their additional readonly
   * restrictions.
   *
   * @param pm
   *          the PM to check the permission annotations for.
   * @param annotations
   *          the set of permission annotations configured for the given PM.
   * @return <code>true</code> if the permission annotations did add a readonly
   *          restriction for the given PM.
   */
  public static boolean isReadonly(PmObject pm, Annotation... annotations) {
    for (Annotation a : annotations) {
      @SuppressWarnings("unchecked")
      PermissionAnnotationHandler<Annotation> h = (PermissionAnnotationHandler<Annotation>) annotationClassToPermissionHandlerMap.get(getAnnotationClass(a));
      if (h != null &&
          h.isReadonly(pm, a)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Annotation> getAnnotationClass(Annotation a) {
    Class<?>[] interfaces = a.getClass().getInterfaces();
    assert interfaces.length == 1 : "Customize annotations with multiple interfaces are not supported: " + a.getClass();
    return (Class<? extends Annotation>)interfaces[0];

  }

}
