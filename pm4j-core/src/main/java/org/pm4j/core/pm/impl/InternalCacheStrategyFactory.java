package org.pm4j.core.pm.impl;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.common.cache.CacheStrategyNoCache;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmCacheCfg2;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Cache;
import org.pm4j.core.pm.annotation.PmCacheCfg2.CacheMode;
import org.pm4j.core.pm.annotation.PmCacheCfg2.Clear;
import org.pm4j.core.pm.api.PmCacheApi.CacheKind;
import org.pm4j.core.pm.impl.cache.CacheStrategyBase;
import org.pm4j.core.pm.impl.cache.CacheStrategyRequest;

// TODO oboede: distribute as protected embedded classes of related PM classes.
class InternalCacheStrategyFactory {

  public static final InternalCacheStrategyFactory INSTANCE = new InternalCacheStrategyFactory();

  /**
   * Creates a cache strategy for the given cache aspect.
   *
   * @param aspect the cache aspect to create a cache strategy for
   * @param cache the cache definition
   * @return a cache strategy
   */
  public final CacheStrategy create(CacheKind aspect, Cache cache) {
    CacheMode mode = (cache != null) ? cache.mode() : CacheMode.OFF;
    switch (mode) {
    case OFF:
      return CacheStrategyNoCache.INSTANCE;
    case ON:
      return createImpl(aspect, cache);
    case REQUEST:
      return new CacheStrategyRequest("CACHE_"+aspect+"_IN_REQUEST", aspect.toString().substring(0, 2).toLowerCase());
    default:
      throw new PmRuntimeException("Unable to find cache strategy for CacheMode '" + cache.mode() + "'.");
    }
  }

  protected CacheStrategy createImpl(CacheKind aspect, Cache cache) {
      switch (aspect) {
      case ENABLEMENT:
        return new CacheStrategyForEnablement(cache.clear());
      case TITLE:
        return new CacheStrategyForTitle(cache.clear());
      case TOOLTIP:
        return new CacheStrategyForTooltip(cache.clear());
      case VISIBILITY:
        return new CacheStrategyForVisibility(cache.clear());
      case NODES:
        return new CacheStrategyForNodes(cache.clear());
      default:
        return null;
      }

  }

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

  private static class CacheStrategyForTooltip extends CacheStrategyBase<PmObjectBase> {
    private CacheStrategyForTooltip(Clear cacheClear) {
      super("CACHE_TOOLTIP_LOCAL", cacheClear);
    }
    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmCachedTooltip;
    }
    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmCachedTooltip = (String)value;
    }
    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmCachedTooltip = null;
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

  static class CacheStrategyForNodes extends CacheStrategyBase<PmObjectBase> {
    public CacheStrategyForNodes(PmCacheCfg2.Clear cacheClear) {
      super("CACHE_NODES_LOCAL", cacheClear);
    }

    @Override protected Object readRawValue(PmObjectBase pm) {
      return pm.pmChildNodesCache;
    }

    @Override protected void writeRawValue(PmObjectBase pm, Object value) {
      pm.pmChildNodesCache = value;
    }

    @Override protected void clearImpl(PmObjectBase pm) {
      pm.pmChildNodesCache = null;
    }
  };

}


