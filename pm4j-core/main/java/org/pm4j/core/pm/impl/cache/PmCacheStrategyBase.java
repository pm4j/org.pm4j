package org.pm4j.core.pm.impl.cache;

import org.pm4j.core.pm.PmObject;

public abstract class PmCacheStrategyBase<PM extends PmObject> implements PmCacheStrategy {

  private String cacheName;

  public PmCacheStrategyBase(String cacheName) {
    this.cacheName = cacheName;
  }

  protected abstract Object readRawValue(PM pm);

  protected abstract void writeRawValue(PM pm, Object value);

  protected abstract void clearImpl(PM pm);


  @Override @SuppressWarnings("unchecked")
  public void clear(PmObject pm) {
    clearImpl((PM)pm);
  }

  @Override @SuppressWarnings("unchecked")
  public Object getCachedValue(PmObject pm) {
    Object v = readRawValue((PM)pm);
    if (v == null) {
      return NO_CACHE_VALUE;
    } else {
      logPmCacheHit(pm);
      return (v != NULL_VALUE_OBJECT) ? v : null;
    }
  }

  @Override @SuppressWarnings("unchecked")
  public Object setAndReturnCachedValue(PmObject pm, Object v) {
    logPmCacheInit(pm);
    writeRawValue((PM)pm, (v != null)
        ? v
        : NULL_VALUE_OBJECT);
    return v;
  }

  @Override
  public boolean isCaching() {
    return true;
  }

  protected void logPmCacheHit(PmObject pm) {
    PmCacheLog.INSTANCE.logPmCacheHit(pm, cacheName);
  }

  protected void logPmCacheInit(PmObject pm) {
    PmCacheLog.INSTANCE.logPmCacheInit(pm, cacheName);
  }

}
