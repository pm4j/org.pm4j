package org.pm4j.common.cache;


/**
 * A {@link CacheStrategy} that does NOT cache.
 *
 * @author Olaf Boede
 */
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

  /**
   * Returns <code>false</code> because this cache is always immediately cleared.
   */
  @Override
  public boolean isCacheNeverCleared() {
    return false;
  }

}
