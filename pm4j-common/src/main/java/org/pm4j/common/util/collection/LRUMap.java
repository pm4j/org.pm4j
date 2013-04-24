package org.pm4j.common.util.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A thread safe Map that implements a LRU strategy: If more elements than a given maxSize are
 * put into the map the eldest entries will be removed. Thus the Map will never contain more
 * than maxSize elements.
 *
 * @author owahlen
 *
 * @param <K>
 *            Type of the key elements of the map.
 * @param <V>
 *            Type of the value elements of the map.
 *
 */
public class LRUMap<K, V> implements Map<K, V> {

    /**
     * The default maximum size of the LRUMap. If more keys than the size are put into the map
     * the eldest are removed.
     */
    public static final int DEFAULT_MAX_SIZE = 1000;

    /**
     * Note that a LinkedHashMap is used to realize the LRU strategy. Since this Map is not
     * thread safe it cannot be inherited and must wrapped by adapter methods.
     */
    private final Map<K, V> internalMap;

    /**
     * The default constructor constructs a LRUMap with a maximum of 1000 entries.
     */
    public LRUMap() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * Construct a LRUMap that will never contain more entries than maxSize. If more entries are
     * put into the map the eldest are be removed.
     *
     * @param maxSize
     */
    public LRUMap(final int maxSize) {
        this.internalMap = (Map<K, V>) Collections.synchronizedMap(new LinkedHashMap<K, V>(maxSize + 1, .75F, true) {
            private static final long serialVersionUID = 5369285290965670135L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        });
    }

    // Adapt some interface methods to the internal synchronized map

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return internalMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return internalMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return internalMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        internalMap.putAll(m);
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return internalMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return internalMap.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return internalMap.entrySet();
    }
}
