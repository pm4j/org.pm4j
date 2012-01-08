package org.pm4j.core.pm.impl.cache;

import org.pm4j.core.pm.PmObject;

public final class PmCacheStrategyNoCache implements PmCacheStrategy {

  public static final PmCacheStrategy INSTANCE = new PmCacheStrategyNoCache();

  @Override
  public Object getCachedValue(PmObject pm) {
    return NO_CACHE_VALUE;
  }

  @Override
  public Object setAndReturnCachedValue(PmObject pm, Object value) {
    return value;
  }

  @Override
  public void clear(PmObject pm) {
  }

  @Override
  public boolean isCaching() {
    return false;
  }

}
