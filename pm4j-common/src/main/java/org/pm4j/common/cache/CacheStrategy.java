package org.pm4j.common.cache;


/**
 * Interface for cache strategy implementations.
 *
 * @author olaf boede
 */
public interface CacheStrategy {

  /** Marker instance, identifies no value within the cache. */
  public static final Object NO_CACHE_VALUE = "-- no cache value available --";

  /** Marker instance, identifies a <code>null</code> value within the cache. */
  public static final Object NULL_VALUE_OBJECT = "--null value--";

  Object getCachedValue(Object ctxt);

  <T> T setAndReturnCachedValue(Object ctxt, T value);

  void clear(Object ctxt);

  boolean isCaching();
}
