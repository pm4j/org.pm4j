package org.pm4j.common.cache;


public final class CacheStrategyNoCache implements CacheStrategy {

  public static final CacheStrategy INSTANCE = new CacheStrategyNoCache();

  @Override
  public Object getCachedValue(Object ctxt) {
    return NO_CACHE_VALUE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object setAndReturnCachedValue(Object ctxt, Object value) {
    return value;
  }

  @Override
  public void clear(Object ctxt) {
  }

  @Override
  public boolean isCaching() {
    return false;
  }

}
