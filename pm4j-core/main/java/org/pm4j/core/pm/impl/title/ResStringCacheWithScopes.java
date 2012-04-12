package org.pm4j.core.pm.impl.title;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pm4j.core.pm.impl.title.ResStringCache.Entry;

public class ResStringCacheWithScopes {

  /**
   * A map of caches for each resource scope (package).
   * That structure is required since the same key may be bound to different
   * values within different scope.
   */
  private Map<Object, ResStringCache> classToResStringCacheMap = new ConcurrentHashMap<Object, ResStringCache>();

  /**
   * Finds an entry for the given key and locale.
   *
   * @param scope
   *          The cache scope context object.
   * @param key
   *          The resource key.
   * @param locale
   *          The locale to find a value for.
   * @return An {@link Entry} if there is a cache entry for the given
   *         key-locale combination. Otherwise <code>null</code>.
   */
  public Entry find(Object scope, String key, Locale locale) {
    return getScopedCache(scope).find(key, locale);
  }

  /**
   * Adds a cache entry.
   *
   * @param scope
   *          The cache scope context object.
   * @param key
   *          The resource key.
   * @param locale
   *          The locale for the given value.
   * @param value
   *          The locale specific value.
   * @return The new resource entry.
   */
  public Entry put(Object scope, String key, Locale locale, String value) {
    ResStringCache scopeCache = getScopedCache(scope);
    return scopeCache.put(key, locale, value);
  }

  ResStringCache getScopedCache(Object scope) {
    ResStringCache scopeCache = classToResStringCacheMap.get(scope);
    if (scopeCache == null) {
      scopeCache = new ResStringCache();
      classToResStringCacheMap.put(scope, scopeCache);
    }
    return scopeCache;
  }
}
