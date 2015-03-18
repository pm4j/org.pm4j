package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Clear;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Observe;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.PmObjectBase.CacheStrategyFactory;

/**
 * Contains helper methods to work {@link PmCacheCfg2} annotation.
 *
 * @author SDOLKE, jhetmans
 *
 */
class InternalPmCacheCfgUtil {

  /**
   * Finds all {@link PmCacheCfg} and {@link PmCacheCfg2} annotations in the
   * hierarchy of the given PM. Asserts that either the first or the latter
   * annotation type is used but never both.
   *
   * @param pm
   *          the PM in question
   * @param foundAnnotations
   *          a list to collect the found annotations
   * @return the filled foundAnnotation parameter list.<br>
   *         The first annotations are more relevant than the following ones.
   *         First annotation is 'nearer' defined e.g. directly on the field
   *         instance. An annotation on a grand-parent PM may appear at the
   *         end of the list.
   * @throws IllegalStateException
   *           if the PM hierarchy contains mixed annotations e.g. old and new
   */
  static List<PmCacheCfg2> findCacheCfgsInPmHierarchy(PmObjectBase pm, List<PmCacheCfg2> foundAnnotations) {
    
    PmCacheCfg2 cfg = AnnotationUtil.findAnnotation(pm, PmCacheCfg2.class);
    if (cfg != null) {
      foundAnnotations.add(cfg);
    }

    PmObjectBase pmParent = (PmObjectBase) pm.getPmParent();
    if (pmParent != null &&
        ! (pm instanceof PmConversation)) {
      findCacheCfgsInPmHierarchy(pmParent, foundAnnotations);
    }

    return foundAnnotations;
  }

  /**
   * Finds a PMs cache definition for a particular cache aspect
   * within the PMs hierarchy.
   *
   * @param pm the PM in question
   * @param aspect the cache aspect to find a cache definition for
   * @param cacheAnnotations all cache annotations of the PM hierarchy,
   *        @see #findCacheCfgsInPmHierarchy(PmObjectBase, List)
   * @return the cache definition for the cache aspect in question
   *         or <code>null</code>
   */
  static Cache findCacheForAspectInPmHierarchy(
      PmObjectBase pm,
      CacheKind aspect,
      Collection<PmCacheCfg2> cacheAnnotations)
  {
    PmCacheCfg2 localCfg =  AnnotationUtil.findAnnotation(pm, PmCacheCfg2.class);
    if (localCfg != null) {
      return getCacheByAspect(localCfg, aspect);
    }

    for (PmCacheCfg2 parentCfg : cacheAnnotations) {
      // consider all parent annotations that are
      // flagged as cascaded
      Cache parentCache = getCacheByAspect(parentCfg, aspect);
      if (parentCache != null && parentCache.cascade()) {
        return parentCache;
      }
    }

    return null;
  }

  static CacheMetaData readCacheMetaData(PmObjectBase pm, CacheKind aspect, CacheStrategyFactory factory) {
    List<PmCacheCfg2> cacheAnnotations = InternalPmCacheCfgUtil.findCacheCfgsInPmHierarchy(pm, new ArrayList<PmCacheCfg2>());
    return (!cacheAnnotations.isEmpty())
        ? InternalPmCacheCfgUtil.readCacheMetaData(pm, aspect, cacheAnnotations, factory)
        : CacheMetaData.NO_CACHE;

  }

  static CacheMetaData readCacheMetaData(PmObjectBase pm, CacheKind aspect, List<PmCacheCfg2> cacheAnnotations, CacheStrategyFactory factory) {
      Cache cache = findCacheForAspectInPmHierarchy(pm, aspect, cacheAnnotations);
      return (cache == null)
          ? CacheMetaData.NO_CACHE
          : new CacheMetaData(factory.create(aspect, cache), cache);
  }

  /**
   * Registers value change listeners on the PMs defined by the given {@link Observe}
   * annotations. The value change listeners resets the particular cache aspect
   * of the given PM object if the observed target PM values are changed.
   *
   * @param pmObject the PM object to reset the cache for
   * @param aspect the cache  aspect that shall be reseted
   * @param clearOns defines a set of PMs that shall be observed for value changes
   */
  static void registerClearOnListeners(final PmObject pmObject, final CacheKind aspect, Observe[] clearOns) {
    if (clearOns == null || clearOns.length == 0) {
      return;
    }

    PmEventListener e = new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        PmCacheApi.clearPmCache(pmObject, aspect);
      }
    };

    // Cause we use week references we store a hard reference in the
    // PM property map to define the life cycle of the listener
    String propertyName = "_pmCacheClearListener" + aspect;
    if (pmObject.getPmProperty(propertyName) != null) {
      throw new PmRuntimeException(pmObject, "There is already a listener stored under property name '"+propertyName+"'");
    }
    pmObject.setPmProperty(propertyName, e);

    for (Observe clearOn : clearOns) {
      for (String expression : clearOn.pm()) {
        PmObject pmToObserve = PmExpressionApi.getByExpression(pmObject, expression, PmObject.class);
        if (clearOn.observePmTree()) {
          PmEventApi.addWeakHierarchyListener(pmToObserve, PmEvent.VALUE_CHANGE, e);
        } else {
          PmEventApi.addWeakPmEventListener(pmToObserve, PmEvent.VALUE_CHANGE, e);
        }
      }
    }
  }


  /**
   * Gets the cache definition for the given aspect from the given cache configuration.
   *
   * @param cfg the cache configuration
   * @param aspect the aspect to get the cache definition for
   * @return the cache definition for the given aspect or <code>null</code>
   */
  private static Cache getCacheByAspect(PmCacheCfg2 cfg, CacheKind aspect) {
    for (Cache cache : cfg.value()) {
      List<CacheKind> aspectList = Arrays.asList(cache.property());
      if (aspectList.contains(aspect) || aspectList.contains(CacheKind.ALL)) {
        return cache;
      }
    }

    return null;
  }

  static class CacheMetaData {
    static final CacheMetaData NO_CACHE = new CacheMetaData(CacheStrategyNoCache.INSTANCE);

    final CacheStrategy cacheStrategy;
    final Observe[] cacheClearOn;
    final Clear clear;

    public CacheMetaData(CacheStrategy cacheStrategy) {
      this(cacheStrategy, (Cache) null);
    }

    public CacheMetaData(CacheStrategy cacheStrategy, Cache cacheCfg) {
      this.cacheStrategy = cacheStrategy;
      this.cacheClearOn = cacheCfg != null ? cacheCfg.clearOn() : null;
      this.clear = cacheCfg != null ? cacheCfg.clear() : null;
    }
  }

}

