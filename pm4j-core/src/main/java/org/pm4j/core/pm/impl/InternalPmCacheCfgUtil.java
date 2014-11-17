package org.pm4j.core.pm.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.common.util.collection.MapUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmEventListener;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCacheCfg;
import org.pm4j.core.pm.annotation.PmCacheCfg.CacheMode;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Observe;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.cache.CacheStrategyBase;
import org.pm4j.core.pm.impl.cache.CacheStrategyRequest;

/**
 * Contains helper methods to work with {@link PmCacheCfg} and {@link PmCacheCfg} annotation.
 *
 * @author SDOLKE
 *
 */
class InternalPmCacheCfgUtil {
  
  private static final CacheStrategy CACHE_TITLE_LOCAL = new CacheStrategyBase<PmObjectBase>("CACHE_TITLE_LOCAL") {
    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmCachedTitle;
    }
    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmCachedTitle = (String)value;
    }
    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmCachedTitle = null;
    }
  };
  
  private static final CacheStrategy CACHE_VISIBLE_LOCAL = new CacheStrategyBase<PmObjectBase>("CACHE_VISIBLE_LOCAL") {
    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmVisibleCache;
    }
    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmVisibleCache = value;
    }
    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmVisibleCache = null;
    }
  };
  
  private static final CacheStrategy CACHE_ENABLED_LOCAL = new CacheStrategyBase<PmObjectBase>("CACHE_ENABLED_LOCAL") {
    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmEnabledCache;
    }
    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmEnabledCache = value;
    }
    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmEnabledCache = null;
    }
  };

  private static final CacheStrategy CACHE_VALUE_LOCAL = new CacheStrategyBase<PmAttrBase<?,?>>("CACHE_VALUE_LOCAL") {
    @Override protected Object readRawValue(PmAttrBase<?, ?> pm) {
      return (pm.dataContainer != null)
                ? pm.dataContainer.cachedValue
                : null;
    }
    @Override protected void writeRawValue(PmAttrBase<?, ?> pm, Object value) {
      pm.zz_getDataContainer().cachedValue = value;
    }
    @Override protected void clearImpl(PmAttrBase<?, ?> pm) {
      if (pm.dataContainer != null) {
        pm.dataContainer.cachedValue = null;
      }
    }
  };
  
  private static final CacheStrategy CACHE_OPTIONS_LOCAL = new CacheStrategyBase<PmAttrBase<?,?>>("CACHE_OPTIONS_LOCAL") {
    @Override protected Object readRawValue(PmAttrBase<?, ?> pm) {
      return (pm.dataContainer != null)
                ? pm.dataContainer.cachedOptionSet
                : null;
    }
    @Override protected void writeRawValue(PmAttrBase<?, ?> pm, Object value) {
      pm.zz_getDataContainer().cachedOptionSet = value;
    }
    @Override protected void clearImpl(PmAttrBase<?, ?> pm) {
      if (pm.dataContainer != null) {
        pm.dataContainer.cachedOptionSet = null;
      }
    }
  };
  
  @SuppressWarnings("deprecation")
  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_TITLE =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
      CacheMode.ON,       CACHE_TITLE_LOCAL,
      CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_TITLE_IN_REQUEST", "ti")
    );
  
  @SuppressWarnings("deprecation")
  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_ENABLEMENT =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
      CacheMode.ON,       CACHE_ENABLED_LOCAL,
      CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_ENABLED_IN_REQUEST", "en")
    );

  @SuppressWarnings("deprecation")
  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_VISIBILITY =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
      CacheMode.ON,       CACHE_VISIBLE_LOCAL,
      CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_VISIBLE_IN_REQUEST", "vi")
    );
  
  @SuppressWarnings("deprecation")
  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_VALUE =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
      CacheMode.ON,       CACHE_VALUE_LOCAL,
      CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_VALUE_IN_REQUEST", "v")
    );

  @SuppressWarnings("deprecation")
  private static final Map<CacheMode, CacheStrategy> CACHE_STRATEGIES_FOR_OPTIONS =
    MapUtil.makeFixHashMap(
      CacheMode.OFF,      CacheStrategyNoCache.INSTANCE,
      CacheMode.ON,       CACHE_OPTIONS_LOCAL,
      CacheMode.REQUEST,  new CacheStrategyRequest("CACHE_OPTIONS_IN_REQUEST", "os")
    );
  
  @SuppressWarnings("deprecation")
  private static final Map<CacheKind, Map<CacheMode, CacheStrategy>> MODE_TO_STRATEGY_MAP_FOR_CACHE_KIND =
    MapUtil.makeFixHashMap(
      CacheKind.ENABLEMENT, CACHE_STRATEGIES_FOR_ENABLEMENT,
      CacheKind.OPTIONS,    CACHE_STRATEGIES_FOR_OPTIONS,
      CacheKind.TITLE,      CACHE_STRATEGIES_FOR_TITLE,
      CacheKind.VALUE,      CACHE_STRATEGIES_FOR_VALUE,
      CacheKind.VISIBILITY, CACHE_STRATEGIES_FOR_VISIBILITY
    );
  
  @SuppressWarnings("deprecation")
  private static final Map<CacheKind, String> ATTR_CONSTANT_FOR_CACHE_KIND =
    MapUtil.makeFixHashMap(
      CacheKind.ENABLEMENT, PmCacheCfg.ATTR_ENABLEMENT,
      CacheKind.OPTIONS,    PmCacheCfg.ATTR_OPTIONS,
      CacheKind.TITLE,      PmCacheCfg.ATTR_TITLE,
      CacheKind.VALUE,      PmCacheCfg.ATTR_VALUE,
      CacheKind.VISIBILITY, PmCacheCfg.ATTR_VISIBILITY
    );      
  
  /**
   * Finds all {@link PmCacheCfg} and {@link PmCacheCfg2} annotations in the
   * hierarchy of the given PM. Asserts that either the first or the latter
   * annotation type is used but never both.
   *
   * @param pm the PM in question
   * @param foundAnnotations a list to collect the found annotations
   * @return the filled foundAnnotation parameter list
   * @throws IllegalStateException if the PM hierarchy contains mixed
   *         annotations e.g. old and new
   */
  @SuppressWarnings({"rawtypes", "deprecation"})
  static List findCacheCfgsInPmHierarchy(PmObjectBase pm, List foundAnnotations) {
    PmCacheCfg2 cfg = AnnotationUtil.findAnnotation(pm, PmCacheCfg2.class);
    if (cfg != null) {
      addToListAndAssertNoMixedMode(pm, cfg, foundAnnotations);
    }
    PmCacheCfg cfgOld = AnnotationUtil.findAnnotation(pm, PmCacheCfg.class);
    if (cfgOld != null) {
      addToListAndAssertNoMixedMode(pm, cfgOld, foundAnnotations);
    }

    PmObjectBase pmParent = (PmObjectBase) pm.getPmParent();
    if (pmParent != null &&
        ! (pm instanceof PmConversation)) {
      findCacheCfgsInPmHierarchy(pmParent, foundAnnotations);
    }

    return foundAnnotations;
  }

  /**
   * Finds a PMs cache definition for a particular cacheable property
   * within the PMs hierarchy.
   *
   * @param pm the PM in question
   * @param property the cacheable property to find a cache definition for
   * @param cacheAnnotations all cache annotations of the PM hierarchy,
   *        @see #findCacheCfgsInPmHierarchy(PmObjectBase, List)
   * @return the cache definition for the cacheable property in question
   *         or <code>null</code>
   */
  static Cache findCacheForPropertyInPmHierarchy(
      PmObjectBase pm,
      CacheKind property,
      Collection<PmCacheCfg2> cacheAnnotations)
  {
    PmCacheCfg2 localCfg =  AnnotationUtil.findAnnotation(pm, PmCacheCfg2.class);
    if (localCfg != null) {
      return getCacheByProperty(localCfg, property);
    }

    for (PmCacheCfg2 parentCfg : cacheAnnotations) {
      // consider all parent annotations that are
      // flagged as cascaded
      Cache parentCache = getCacheByProperty(parentCfg, property);
      if (parentCache != null && parentCache.cascade()) {
        return parentCache;
      }
    }

    return null;
  }
  
  @SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
  static CacheMetaData readCacheMetaData(PmObjectBase pmObjectBase, CacheKind property, List cacheAnnotations) {
    if (PmCacheCfg2.class.isAssignableFrom(cacheAnnotations.get(0).getClass())) {
      Cache cache = findCacheForPropertyInPmHierarchy(pmObjectBase, property, cacheAnnotations);
      return (cache == null)
          ? CacheMetaData.NO_CACHE
          : new CacheMetaData(InternalCacheStrategyFactory.create(property, cache), cache);
    } else {
      return new CacheMetaData(AnnotationUtil.evaluateCacheStrategy(pmObjectBase, ATTR_CONSTANT_FOR_CACHE_KIND.get(property), cacheAnnotations, MODE_TO_STRATEGY_MAP_FOR_CACHE_KIND.get(property)));
    }
  }

  /**
   * Registers value change listeners on the PMs defined by the given {@link Observe}
   * annotations. The value change listeners resets the particular cacheable property
   * of the given PM object if the observed target PM values are changed.
   *
   * @param pmObject the PM object to reset the cache for
   * @param property the cacheable property that shall be reseted
   * @param clearOns defines a set of PMs that shall be observed for value changes
   */
  static void registerClearOnListeners(final PmObject pmObject, final CacheKind property, Observe[] clearOns) {
    if (clearOns == null || clearOns.length == 0) {
      return;
    }

    PmEventListener e = new PmEventListener() {
      @Override
      public void handleEvent(PmEvent event) {
        PmCacheApi.clearPmCache(pmObject, property);
      }
    };
    
    // Cause we use week references we store a hard reference in the
    // pm property map to define the lifecycle of the listener
    setClearOnListenerAsProperty(pmObject, e, property);

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
   * Adds a cache cfg annotation to the list and asserts that all cfg's are
   * from the same annotation type.
   *
   * @param pm the PM the cache cfg was taken from, needed for the error message
   * @param cfg the cache cfg to add to the list
   * @param foundAnnotations all annotations found so far
   */
  @SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
  private static void addToListAndAssertNoMixedMode(PmObjectBase pm, Annotation cfg, List foundAnnotations) {
    if (!foundAnnotations.isEmpty()) {
      Annotation a = (Annotation) foundAnnotations.get(0);
      // Watch out: the annotations are proxied so a class equals will not work!
      if (PmCacheCfg.class.isAssignableFrom(a.getClass()) && !(PmCacheCfg.class.isAssignableFrom(cfg.getClass()))) {
        throw new PmRuntimeException(pm, "Mixed cache annotations are not supported. We are currently searching for '"+PmCacheCfg.class.getSimpleName()+
            "' annotaions but the PM is annotated with '" + PmCacheCfg2.class.getSimpleName() + "'.\n" +
            " Please make sure that you either use the old cache cfg annotation or the new one, but not both!");
      }
      if (PmCacheCfg2.class.isAssignableFrom(a.getClass()) && !(PmCacheCfg2.class.isAssignableFrom(cfg.getClass()))) {
        throw new PmRuntimeException(pm, "Mixed cache annotations are not supported. We are currently searching for '"+PmCacheCfg2.class.getSimpleName()+
            "' annotaions but the PM is annotated with '" + PmCacheCfg.class.getSimpleName() + "'.\n" +
            " Please make sure that you either use the old cache cfg annotation or the new one, but not both!");
      }
    }

    foundAnnotations.add(cfg);
  }
  
  private static void setClearOnListenerAsProperty(PmObject pmObject, PmEventListener e, CacheKind property) {
    String propertyName = "_pmCacheClearListener" + property;
    if (pmObject.getPmProperty(propertyName) != null) {
      throw new PmRuntimeException(pmObject, "There is already a listener stored under property name '"+propertyName+"'");
    }
    pmObject.setPmProperty(propertyName, e);
  }

  /**
   * Gets the cache definition for the given property from the given cache configuration.
   *
   * @param cfg the cache configuration
   * @param property the property to get the cache definition for
   * @return the cache definition for the given property or <code>null</code>
   */
  private static Cache getCacheByProperty(PmCacheCfg2 cfg, CacheKind property) {
    for (Cache cache : cfg.value()) {
      List<CacheKind> propertyList = Arrays.asList(cache.property());
      if (propertyList.contains(property) || propertyList.contains(CacheKind.ALL)) {
        return cache;
      }
    }

    return null;
  }
  
  static class CacheMetaData {
    static final CacheMetaData NO_CACHE = new CacheMetaData(CacheStrategyNoCache.INSTANCE);

    final CacheStrategy cacheStrategy;
    final Observe[] cacheClearOn;

    public CacheMetaData(CacheStrategy cacheStrategy) {
      this(cacheStrategy, (Cache) null);
    }

    public CacheMetaData(CacheStrategy cacheStrategy, Cache cacheCfg) {
      this(cacheStrategy, cacheCfg != null ? cacheCfg.clearOn() : null);
    }

    public CacheMetaData(CacheStrategy cacheStrategy, Observe... cacheClearOn) {
      this.cacheStrategy = cacheStrategy;
      this.cacheClearOn = cacheClearOn;
    }
  }
  
}
