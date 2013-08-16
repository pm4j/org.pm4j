package org.pm4j.core.pm.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;

public class AnnotationUtil {

  private static final Object[] EMPTY_OBJ_ARRAY = new Object[0];

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
   * Searches for the first {@link CacheMode} property with the given
   * name within from the given annotation set.<br>
   * The first annotation property that has not the value
   * {@link CacheMode#NOT_SPECIFIED} will be returned. When there is no match,
   * the provided default will be returned.
   *
   * @param propertyName name of the {@link CacheMode} property to handle. E.g. 'all' or 'title'.
   * @param annotations  the set of found found {@link CacheMode} annotations to analyze.
   * @param defaultValue the default value to be used in case of not finding a non-default
   *                     definition.
   * @return The first found annotation value for the given property or the specified default.
   */
  public static CacheMode getCacheModeFromCacheAnnotations(
          String propertyName,
          Collection<PmCacheCfg> annotations,
          CacheMode defaultValue) {
    CacheMode value = defaultValue;

    try {
      Method m = null;
      for (PmCacheCfg cfg : annotations) {
        // use reflection method finder only once:
        if (m == null) {
          m = cfg.getClass().getMethod(propertyName);
        }
        CacheMode v = (CacheMode)m.invoke(cfg, EMPTY_OBJ_ARRAY);
        if (v == CacheMode.NOT_SPECIFIED) {
          v = cfg.all();
        }

        if (v != CacheMode.NOT_SPECIFIED) {
          value = v;
          break;
        }
      }
    } catch (Exception e) {
      CheckedExceptionWrapper.throwAsRuntimeException(e);
    }

    return value;
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
   */
  static <T extends Annotation> void findAnnotationsInPmHierarchy(PmObjectBase pm, Class<T> annotationClass, Collection<T> foundAnnotations) {
    T cfg = AnnotationUtil.findAnnotation(pm, annotationClass);
    if (cfg != null) {
      foundAnnotations.add(cfg);
    }

    // the first case will be removed as soon as on project will be ported to the current implementation state.
    if (pm.getPmConversation().getPmDefaults().isElementsInheritAnnotationsOnlyFromSession() &&
        pm instanceof PmElementBase) {
      PmConversationImpl c = pm.getPmConversationImpl();
      if (c != pm) {
        findAnnotationsInPmHierarchy(c, annotationClass, foundAnnotations);
      }
    }
    else {
      PmObjectBase pmParent = (PmObjectBase) pm.getPmParent();
      if (pmParent != null &&
          ! (pm instanceof PmConversation)) {
        findAnnotationsInPmHierarchy(pmParent, annotationClass, foundAnnotations);
      }
    }
  }

}
