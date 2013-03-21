package org.pm4j.common.util.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * A cache containing map for already retrieved values. If the map does not contain a value for
 * a requested key, internally the method {@link #getMissingValue(Object)} gets called to
 * provide (and cache) that value.
 *
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 *
 * @author olaf boede
 */
public abstract class MapBasedCache<K, V> {

    private Map<K, V> map;
    private int cacheHitCont;
    private int cacheMissCount;

    /**
     * Construct the MapBasedCache with a conventional HashMap.
     */
    public MapBasedCache() {
        this(new HashMap<K, V>());
    }
    
    /**
     * Initialize the MapBasedCache with a custom Map implementation.
     * This allows for more memory preserving implementations like LRU Maps or
     * org.apache.commons.collections.map.ReferenceMap.
     * 
     * @param map
     *            the instance of the custom map implementation.
     */
    public MapBasedCache(Map<K, V> map) {
        assert map != null;
        this.map = map;
    }
    
    /**
     * Provides the value for the given key.
     *
     * @param key
     *            the key
     * @return the corresponding value.
     */
    public V get(K key) {
        V v = map.get(key);
        if (v == null) {
            v = getMissingValue(key);
            cacheMissCount++;
            if (v != null) {
                map.put(key, v);
            }
        } else {
            cacheHitCont++;
        }
        return v;
    }

    /**
     * Gets called if the internal key-value map did not contain an entry for the key.
     *
     * @param key
     *            the key to get a value for.
     * @return the found value.
     */
    protected abstract V getMissingValue(K key);

    /**
     * Clears the cache and the debug counter values.
     */
    public void clear() {
      map.clear();
      cacheHitCont = 0;
      cacheMissCount = 0;
    }

    /**
     * @return a counter for cache hits.
     */
    public int getCacheHitCount() {
        return cacheHitCont;
    }

    /**
     * @return a counter for the calls to {@link #getMissingValue(Object)} (cache miss).
     */
    public int getCacheMissCount() {
        return cacheMissCount;
    }
}
