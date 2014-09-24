package org.pm4j.core.pm.impl.cache;

import org.pm4j.common.cache.CacheStrategy;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmCacheCfg.Clear;
import org.pm4j.core.pm.impl.InternalPmImplUtil;
import org.pm4j.core.pm.impl.PmObjectBase;

//TODO oboede: remove the PM dependency.
public abstract class CacheStrategyBase<PM extends PmObjectBase> implements CacheStrategy {

  private String cacheName;

  public CacheStrategyBase(String cacheName) {
    this.cacheName = cacheName;
  }

  protected abstract Object readRawValue(PM pm);

  protected abstract void writeRawValue(PM pm, Object value);

  protected abstract void clearImpl(PM pm);


  @Override @SuppressWarnings("unchecked")
  public void clear(Object ctxt) {
    PM pm = (PM)ctxt;
    if (InternalPmImplUtil.getPmCacheClear(pm).equals(Clear.DEFAULT)) {
      clearImpl(pm);
    }
  }

  @Override @SuppressWarnings("unchecked")
  public Object getCachedValue(Object ctxt) {
    PM pm = (PM)ctxt;
    Object v = readRawValue(pm);
    if (v == null) {
      return NO_CACHE_VALUE;
    } else {
      logPmCacheHit(pm);
      return (v != NULL_VALUE_OBJECT) ? v : null;
    }
  }

  @Override @SuppressWarnings("unchecked")
  public Object setAndReturnCachedValue(Object ctxt, Object v) {
    PM pm = (PM)ctxt;
    logPmCacheInit(pm);
    writeRawValue(pm, (v != null)
        ? v
        : NULL_VALUE_OBJECT);
    return v;
  }

  @Override
  public boolean isCaching() {
    return true;
  }

  protected void logPmCacheHit(PmObject pm) {
    CacheLog.INSTANCE.logPmCacheHit(pm, cacheName);
  }

  protected void logPmCacheInit(PmObject pm) {
    CacheLog.INSTANCE.logPmCacheInit(pm, cacheName);
  }

}
