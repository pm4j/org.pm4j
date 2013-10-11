package org.pm4j.core.pm.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;

/**
 * Contains algorithms for reading PM annotations.
 *
 * @author olaf boede
 */
public class AnnotationUtil {

  /** Helper for reflection based method calls. */
  private static final Object[] EMPTY_OBJ_ARRAY = new Object[0];
  /** Cached cache aspect getter methods. */
  private static Map<String, Method> cacheAspectToGetterMap = new ConcurrentHashMap<String, Method>();

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
  static <T extends Annotation> Collection<T> findAnnotationsInPmHierarchy(PmObjectBase pm, Class<T> annotationClass, Collection<T> foundAnnotations) {
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

  /**
   * Reads and evaluates the cache strategy to use for the given cache aspect of the given PM.
   *
   * @param pm
   * @param cacheCfgAttrName
   * @param modeToStrategyMap
   * @return
   */
  public static CacheStrategy readCacheStrategy(
      PmObjectBase pm,
      String cacheCfgAttrName,
      Map<CacheMode, CacheStrategy> modeToStrategyMap)
  {
    List<PmCacheCfg> cacheAnnotations = new ArrayList<PmCacheCfg>();
    findAnnotationsInPmHierarchy(pm, PmCacheCfg.class, cacheAnnotations);
    return AnnotationUtil.evaluateCacheStrategy(pm, cacheCfgAttrName, cacheAnnotations, modeToStrategyMap);
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
  /**
   * Evaluates the cache strategy to use for the given cache aspect of the given PM.
   *
   * @param pm
   * @param cacheCfgAttrName
   * @param cacheAnnotations
   * @param modeToStrategyMap
   * @return
   */
  public static CacheStrategy evaluateCacheStrategy(
      PmObjectBase pm,
      String cacheCfgAttrName,
      Collection<PmCacheCfg> cacheAnnotations,
      Map<CacheMode, CacheStrategy> modeToStrategyMap)
  {
    CacheMode cacheMode = readCacheModeWithoutParentModes(pm, cacheCfgAttrName);
    if (cacheMode == CacheMode.NOT_SPECIFIED) {
      cacheMode = CacheMode.OFF;
      for (PmCacheCfg cfg : cacheAnnotations) {
        CacheMode v = readCacheMode(cfg, cacheCfgAttrName);
        // Only annotations defined for the children will be considered.
        if ( (v != CacheMode.NOT_SPECIFIED) && cfg.cascade() ) {
          cacheMode = v;
          break;
        }
      }
    }

    CacheStrategy s = modeToStrategyMap.get(cacheMode);
    if (s == null) {
      throw new PmRuntimeException(pm, "Unable to find cache strategy for CacheMode '" + cacheMode + "'.");
    }
    return s;
  }

  /**
   * Reads the specified cache aspect from the given {@link PmCacheCfg}.
   * Considers the {@link PmCacheCfg#all()} definition if the specified cache aspect
   * is not defined explicitly.
   *
   * @param cfg
   * @param cacheAspectName
   * @return The found {@link CacheMode}. Otherwise {@link CacheMode#NOT_SPECIFIED}.
   */
  private static CacheMode readCacheMode(PmCacheCfg cfg, String cacheAspectName) {
    CacheMode cacheMode = CacheMode.NOT_SPECIFIED;
    try {
      // use the slow reflection method finder only once:
      Method getter = cacheAspectToGetterMap.get(cacheAspectName);
      if (getter == null) {
        getter = cfg.getClass().getMethod(cacheAspectName);
        cacheAspectToGetterMap.put(cacheAspectName, getter);
      }

      cacheMode = (CacheMode)getter.invoke(cfg, EMPTY_OBJ_ARRAY);
      if (cacheMode == CacheMode.NOT_SPECIFIED) {
        cacheMode = cfg.all();
      }
    } catch (Exception e) {
      CheckedExceptionWrapper.throwAsRuntimeException(e);
    }
    return cacheMode;
  }

  /**
   * Looks only for the {@link PmCacheCfg} definition assigned to the given PM.
   *
   * @param pm
   * @param cacheCfgAttrName
   * @return The found {@link CacheMode} or {@link CacheMode#NOT_SPECIFIED}.
   */
  private static CacheMode readCacheModeWithoutParentModes(PmObjectBase pm, String cacheCfgAttrName) {
    PmCacheCfg cfg = findAnnotation(pm, PmCacheCfg.class);
    return (cfg != null)
        ? readCacheMode(cfg, cacheCfgAttrName)
        : CacheMode.NOT_SPECIFIED;
  }

}
