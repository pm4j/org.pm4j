package org.pm4j.core.pm.impl;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains algorithms for reading PM annotations.
 *
 * @author olaf boede
 */
public class AnnotationUtil {

  /**
   * Searches for an annotation within the inheritance tree of a class.
   * <p>
   * TODO: Annotations declared in interfaces are not yet considered.
   *
   * @param <A>
   *          The annotation type.
   * @param classToAnalyze
   *          In this class or one of its super classes the annotation will be
   *          searched.
   * @param annotationClass
   *          The runtime class of the annotation.
   * @return The found annotation or <code>null</code>.
   */
  public static <A extends Annotation> A findAnnotationInClassTree(Class<?> classToAnalyze, Class<A> annotationClass) {
    A a = null;
    Class<?> c = classToAnalyze;
    while (a == null) {
      a = c.getAnnotation(annotationClass);
      if (a == null) {
        c = c.getSuperclass();
        if (c == null || c.equals(Object.class)) {
          break;
        }
      }
    }

    return a;
  }

  /**
   * Just finds an annotation for this class.
   * <p>
   * Subclasses provide different implementations e.g. to find annotations that
   * are attached to a matching field declaration of the parent model.
   *
   * @param <T>
   *          The annotation type.
   * @param annotationClass
   *          The annotation class to find.
   * @return The annotation instance of <code>null</code> when not found.
   */
  public static <T extends Annotation> T findAnnotation(PmObjectBase pm, Class<? extends T> annotationClass) {
    return pm.getPmMetaDataWithoutPmInitCall().isPmField
              ? (T) findAnnotation(pm, annotationClass, pm.getPmParent().getClass())
              : (T) findAnnotationInClassTree(pm.getClass(), annotationClass);
  }

  /**
   * Imperative version of {@link #findAnnotation(PmObjectBase, Class)}.<br>
   * Throws a {@link PmRuntimeException} if the annotation can't be found.
   */
  public static <T extends Annotation> T getAnnotation(PmObjectBase pm, Class<? extends T> annotationClass) {
    T t = findAnnotation(pm, annotationClass);
    if (t == null) {
      throw new PmRuntimeException(pm, "Missing annotation: " + annotationClass);
    }
    return t;
  }

  /**
   * Provides an annotation that is defined for this presentation model class or
   * the field of the parent presentation model class.
   *
   * @param <T>
   *          The annotation type.
   * @param annotationClass
   *          The annotation class to find.
   * @param parentClass
   *          The class the contains this presentation model. E.g. the element
   *          PM class for an attribute PM class.
   * @return The annotation instance of <code>null</code> when not found.
   */
  public static <T extends Annotation> T findAnnotation(PmObjectBase pm, Class<T> annotationClass, Class<?> parentClass) {
    assert parentClass != null;

    T foundAnnotation = null;

    // find it in the field declaration
    try {
      Field field = parentClass.getField(pm.getPmName());
      foundAnnotation = field.getAnnotation(annotationClass);
    } catch (NoSuchFieldException e) {
      // may be OK, because the attribute may use something like getters or
      // xPath.
    }

    // next try if necessary: find it in the attribute presentation model class
    if (foundAnnotation == null) {
      foundAnnotation = findAnnotationInClassTree(pm.getClass(), annotationClass);
    }

    return foundAnnotation;
  }

  /**
   * Finds a set of predefined annotation types for the given PM.
   *
   * @param pm
   *          the PM to analyze.
   * @param annotationClassesToConsider
   *          the annotation types to look for.
   * @return the set of found annotations. Is never <code>null</code>.
   */
  public static List<Annotation> findAnnotations(PmObjectBase pm, Collection<Class<? extends Annotation>> annotationClassesToConsider) {
    List<Annotation> foundAnnotationList = new ArrayList<Annotation>();
    for (Class<? extends Annotation> c : annotationClassesToConsider) {
      Annotation a = AnnotationUtil.findAnnotation(pm, c);
      if (a != null) {
        foundAnnotationList.add(a);
      }
    }
    return foundAnnotationList;
  }

  /**
   * Searches an annotation within the PM hierarchy. Adds
   * all found annotations to the given collection. Adds nothing when no
   * annotation was found in the hierarchy.
   *
   * @param annotationClass
   *          The annotation to find.
   * @param foundAnnotations
   *          The set to add the found annotations to. The lowest level
   *          annotation (e.g. bound to an attribute) is at the first position.
   *          The highest level annotation (e.g. bound to the root session) is
   *          at the last position.
   * @return A reference to the found annotations for inline usage.
   */
  static <T extends Annotation> List<T> findAnnotationsInPmHierarchy(PmObjectBase pm, Class<T> annotationClass, List<T> foundAnnotations) {
    T cfg = AnnotationUtil.findAnnotation(pm, annotationClass);
    if (cfg != null) {
      foundAnnotations.add(cfg);
    }

    PmObjectBase pmParent = (PmObjectBase) pm.getPmParent();
    if (pmParent != null &&
        ! (pm instanceof PmConversation)) {
      findAnnotationsInPmHierarchy(pmParent, annotationClass, foundAnnotations);
    }

    return foundAnnotations;
  }

}
