package org.pm4j.core.pm.impl.title;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache for string resource values.
 * <p>
 * Hint for <code>null</code> value handling: If you don't have a value for a given resource, you
 * may put a <code>null</code> value for the given key to this cache.
 * This way you get next time for the same key an {@link Entry} with a <code>null</code>
 * value.
 * You have only to scan the potentially slow localization datasource (file, db etc.) when
 * get no entry back from {@link #find(String, Locale)}.
 *
 * @author olaf boede
 */
public class ResStringCache {

  private Map<Locale, Map<String, Entry>> localeToKeyToValueMap = new ConcurrentHashMap<Locale, Map<String, Entry>>();

  private static final Entry EMPTY_ENTRY = new Entry(null);

  /**
   * A structure that holds the cached value.
   */
  public static class Entry {
    private String value;

    public Entry(String value) {
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }
  }

  /**
   * Finds an entry for the given key and locale.
   *
   * @param key
   *          The resource key.
   * @param locale
   *          The locale to find a value for.
   * @return An {@link Entry} if there is a cache entry for the given
   *         key-locale combination. Otherwise <code>null</code>.
   */
  public Entry find(String key, Locale locale) {
    Map<String, Entry> keyToValueMap = getKeyToValueMap(locale);
    return keyToValueMap.get(key);
  }

  /**
   * Puts a cache entry.
   *
   * @param key
   *          The resource key.
   * @param locale
   *          The locale for the given value.
   * @param value
   *          The locale specific value.
   * @return The new resource entry.
   */
  public Entry put(String key, Locale locale, String value) {
    Entry newEntry = (value != null)
                            ? new Entry(value)
                            : EMPTY_ENTRY;
    Map<String, Entry> keyToValueMap = getKeyToValueMap(locale);
    keyToValueMap.put(key, newEntry);

    return newEntry;
  }

  private final Map<String, Entry> getKeyToValueMap(Locale locale) {
    Map<String, Entry> map = localeToKeyToValueMap.get(locale);
    if (map == null) {
      map = new ConcurrentHashMap<String, Entry>();
      localeToKeyToValueMap.put(locale, map);
    }
    return map;
  }
}
