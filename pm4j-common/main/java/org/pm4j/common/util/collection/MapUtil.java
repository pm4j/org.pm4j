package org.pm4j.common.util.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utiltiy methods for Map handling.
 *
 * @author olaf boede
 */
public final class MapUtil {

  /**
   * Single-line construction method for a {@link HashMap}.
   * <p>
   * Usage example:
   * <pre>
   *   Map<String, Integer> myMap = MapUtil.makeHashMap(
   *      "a", 1,
   *      "b", 345,
   *      "c", 7 );
   * </pre>
   *
   * @param <K> Map item key type.
   * @param <V> Map item value type.
   * @param keyValueSet The map content, organized in pairs.
   * The first item should be the key of the first item. The second one, the value of the first item.
   * After that other items might be passed in the same manner.
   *
   * @return The generated map.
   */
  @SuppressWarnings("unchecked")
  public static <K, V> HashMap<K, V> makeHashMap(Object... keyValueSet) {
    assert keyValueSet.length % 2 == 0 : "Odd number of arguments is not supported.";

    HashMap<K, V> map = new HashMap<K, V>();

    for (int i=0; i<keyValueSet.length; ++i) {
      map.put((K)keyValueSet[i], (V)keyValueSet[++i]);
    }

    return map;
  }

  /**
   * Single-line construction method for unmodifiable maps.
   *
   * @see #makeHashMap(Object...)
   */

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> makeFixHashMap(Object... keyValueSet) {
    return (Map<K, V>)Collections.unmodifiableMap(makeHashMap(keyValueSet));
  }

  /**
   * @see #makeHashMap(Object...)
   */
  @SuppressWarnings("unchecked")
  public static <K, V> LinkedHashMap<K, V> makeLinkedMap(Object... keyValueSet) {
    assert keyValueSet.length % 2 == 0 : "Odd number of arguments is not supported.";

    LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();

    for (int i=0; i<keyValueSet.length; ++i) {
      map.put((K)keyValueSet[i], (V)keyValueSet[++i]);
    }

    return map;
  }

}
