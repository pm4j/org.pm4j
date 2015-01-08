package org.pm4j.common.cache;

/**
 * Interface for cache strategy implementations.
 *
 * @author Olaf Boede
 */
public interface CacheStrategy {

  /** Marker instance, identifies no value within the cache. */
  public static final Object NO_CACHE_VALUE = "-- no cache value available --";

  /**
   * Provides the value stored in the cache.
   *
   * @param ctxt The cache context instance. E.g. a PM that may have a configured cache.
   * @return
   *  <ul>
   *   <li>{@link #NO_CACHE_VALUE} if the cache is not filled or</li>
   *   <li>The cached value</li>
   *  </ul>
   */
  Object getCachedValue(Object ctxt);

  /**
   * Informs about a value to cache.
   *
   * @param ctxt The cache context instance. E.g. a PM that may have a configured cache.
   * @param value The new value to cache.
   * @return The new value again for fluent programming style support.
   */
  <T> T setAndReturnCachedValue(Object ctxt, T value);

  /**
   * Clears the cached value.
   * <p>
   * But: Has no effect if {@link #isCacheNeverCleared()} returns <code>true</code>.
   *
   * @param ctxt The cache context instance. E.g. a PM that may have a configured cache.
   */
  void clear(Object ctxt);

  /**
   * Indicates if this strategy is really caching something.
   *
   * @return <code>true</code> if the strategy stores cached values.<br>
   *         <code>false</code> if it is not caching value (e.g. the not caching default implementation).
   */
  boolean isCaching();

  /**
   * @return <code>true</code> if the cache configured, not to be cleared on usual clear cache events.
   */
  boolean isCacheNeverCleared();

}
