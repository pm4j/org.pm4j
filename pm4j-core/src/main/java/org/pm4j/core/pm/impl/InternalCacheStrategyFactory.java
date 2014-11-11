package org.pm4j.core.pm.impl;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.CacheMode;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Clear;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.impl.cache.CacheStrategyBase;
import org.pm4j.core.pm.impl.cache.CacheStrategyRequest;

final class InternalCacheStrategyFactory {
  
  private InternalCacheStrategyFactory() {
  }
  
  /**
   * Creates a cache strategy for the given cacheable property.
   *
   * @param property the cacheable property to create a cache strategy for
   * @param cache the cache definition
   * @return a cache strategy
   */
  static CacheStrategy create(CacheKind property, Cache cache) {
    CacheMode mode = (cache != null) ? cache.mode() : CacheMode.OFF;
    switch (mode) {
    case OFF:
      return CacheStrategyNoCache.INSTANCE;
    case ON:
      switch (property) {
      case ENABLEMENT:
        return new CacheStrategyForEnablement(cache.clear());
      case OPTIONS:
        return new CacheStrategyForOptions(cache.clear());
      case TITLE:
        return new CacheStrategyForTitle(cache.clear());
      case VALUE:
        return new CacheStrategyForValue(cache.clear());
      case VISIBILITY:
        return new CacheStrategyForVisibility(cache.clear());
      default:
        throw new PmRuntimeException("Unsupported cache property '"+property+"'.");
      }
    case REQUEST:
      return new CacheStrategyRequest("CACHE_"+property+"_IN_REQUEST", property.toString().substring(0, 2).toLowerCase());
    default:
      throw new PmRuntimeException("Unable to find cache strategy for CacheMode '" + cache.mode() + "'.");
    }
  }
  
  private static class CacheStrategyForValue extends CacheStrategyBase<PmAttrBase<?,?>> {
    private CacheStrategyForValue(Clear cacheClear) {
      super("CACHE_VALUE_LOCAL", cacheClear);
    }
    
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
  
  private static class CacheStrategyForOptions extends CacheStrategyBase<PmAttrBase<?,?>> {
    private CacheStrategyForOptions(Clear cacheClear) {
      super("CACHE_OPTIONS_LOCAL", cacheClear);
    }
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
  
  private static class CacheStrategyForVisibility extends CacheStrategyBase<PmObjectBase> {
    private CacheStrategyForVisibility(Clear cacheClear) {
      super("CACHE_VISIBLE_LOCAL", cacheClear);
    }
    
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
  
  private static class CacheStrategyForTitle extends CacheStrategyBase<PmObjectBase> {
    private CacheStrategyForTitle(Clear cacheClear) {
      super("CACHE_TITLE_LOCAL", cacheClear);
    }
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
  
  private static class CacheStrategyForEnablement extends CacheStrategyBase<PmObjectBase> {
    private CacheStrategyForEnablement(Clear cacheClear) {
      super("CACHE_ENABLED_LOCAL", cacheClear);
    }
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
  
}
