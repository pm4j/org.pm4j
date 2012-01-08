package org.pm4j.core.pm.impl.cache;

import org.pm4j.core.pm.PmObject;

/**
 * Interface for PM cache strategy implementations.
 *
 * @author olaf boede
 */
public interface PmCacheStrategy {

  /** Marker instance, identifies no value within the cache. */
  public static final Object NO_CACHE_VALUE = "-- no cache value available --";

  /** Marker instance, identifies a <code>null</code> value within the cache. */
  public static final Object NULL_VALUE_OBJECT = "--null value--";

  Object getCachedValue(PmObject pm);

  Object setAndReturnCachedValue(PmObject pm, Object value);

  void clear(PmObject pm);

  boolean isCaching();
}
