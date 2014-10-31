package org.pm4j.core.pm.impl;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.CacheMode;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Clear;
import org.pm4j.core.pm.impl.cache.CacheStrategyBase;
import org.pm4j.core.pm.impl.cache.CacheStrategyRequest;

final class CacheStrategyFactory {
  
  private CacheStrategyFactory() {
  }
  
  static CacheStrategy createStrategyForValue(Cache cache) {
    CacheMode mode = (cache != null) ? cache.mode() : CacheMode.NOT_SPECIFIED;
    switch (mode) {
    case NOT_SPECIFIED:
    case OFF:
      return CacheStrategyNoCache.INSTANCE;
    case ON:
      return new CacheStrategyValue(cache.clear());
    case REQUEST:
      return new CacheStrategyRequest("CACHE_VALUE_IN_REQUEST", "vl", cache.clear());
    default:
      throw noStrategyForCacheModeFailure(mode);
    }
  }
  
  static CacheStrategy createStrategyForOptions(Cache cache) {
    CacheMode mode = (cache != null) ? cache.mode() : CacheMode.NOT_SPECIFIED;
    switch (mode) {
    case NOT_SPECIFIED:
    case OFF:
      return CacheStrategyNoCache.INSTANCE;
    case ON:
      return new CacheStrategyOptions(cache.clear());
    case REQUEST:
      return new CacheStrategyRequest("CACHE_OPTIONS_IN_REQUEST", "os", cache.clear());
    default:
      throw noStrategyForCacheModeFailure(mode);
    }
  }

  static CacheStrategy createStrategyForEnablement(Cache cache) {
    CacheMode mode = (cache != null) ? cache.mode() : CacheMode.NOT_SPECIFIED;
    switch (mode) {
    case NOT_SPECIFIED:
    case OFF:
      return CacheStrategyNoCache.INSTANCE;
    case ON:
      return new CacheStrategyEnablement(cache.clear());
    case REQUEST:
      return new CacheStrategyRequest("CACHE_ENABLED_IN_REQUEST", "en", cache.clear());
    default:
      throw noStrategyForCacheModeFailure(mode);
    }
  }
  
  static CacheStrategy createStrategyForVisibillity(Cache cache) {
    CacheMode mode = (cache != null) ? cache.mode() : CacheMode.NOT_SPECIFIED;
    switch (mode) {
    case NOT_SPECIFIED:
    case OFF:
      return CacheStrategyNoCache.INSTANCE;
    case ON:
      return new CacheStrategyVisibility(cache.clear());
    case REQUEST:
      return new CacheStrategyRequest("CACHE_VISIBLE_IN_REQUEST", "vi", cache.clear());
    default:
      throw noStrategyForCacheModeFailure(mode);
    }
  }
  
  static CacheStrategy createStrategyForTitle(Cache cache) {
    CacheMode mode = (cache != null) ? cache.mode() : CacheMode.NOT_SPECIFIED;
    switch (mode) {
    case NOT_SPECIFIED:
    case OFF:
      return CacheStrategyNoCache.INSTANCE;
    case ON:
      return new CacheStrategyTitle(cache.clear());
    case REQUEST:
      return new CacheStrategyRequest("CACHE_TITLE_IN_REQUEST", "ti", cache.clear());
    default:
      throw noStrategyForCacheModeFailure(mode);
    }
  }
  
  private static PmRuntimeException noStrategyForCacheModeFailure(CacheMode cacheMode) {
    return new PmRuntimeException("Unable to find cache strategy for CacheMode '" + cacheMode + "'.");
  }
  
  private static class CacheStrategyValue extends CacheStrategyBase<PmAttrBase<?,?>> {
    private CacheStrategyValue(Clear cacheClear) {
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
  
  private static class CacheStrategyOptions extends CacheStrategyBase<PmAttrBase<?,?>> {
    private CacheStrategyOptions(Clear cacheClear) {
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
  
  private static class CacheStrategyVisibility extends CacheStrategyBase<PmObjectBase> {
    private CacheStrategyVisibility(Clear cacheClear) {
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
  
  private static class CacheStrategyTitle extends CacheStrategyBase<PmObjectBase> {
    private CacheStrategyTitle(Clear cacheClear) {
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
  
  private static class CacheStrategyEnablement extends CacheStrategyBase<PmObjectBase> {
    private CacheStrategyEnablement(Clear cacheClear) {
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
