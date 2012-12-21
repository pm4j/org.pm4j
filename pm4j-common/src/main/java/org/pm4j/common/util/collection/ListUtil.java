package org.pm4j.common.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ListUtil {

  public static <T> T listToItemOrNull(List<T> list) {
    if (list == null) {
      return null;
    }
    else {
      switch(list.size()) {
      case 0: return null;
      case 1: return list.get(0);
      default: throw new IllegalArgumentException("List has more than one item: " + list);
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> List<T> collectionsToList(Collection... collections) {
    List<T> list = new ArrayList<T>();
    for (Collection coll : collections) {
      list.addAll(coll);
    }
    return list;
  }

  public static <T> List<T> toList(Iterable<T> iterable) {
    if (iterable == null) {
      return null;
    }
    else if (iterable instanceof List) {
      return (List<T>) iterable;
    }
    else if (iterable instanceof Collection) {
      return new ArrayList<T>((Collection<T>)iterable);
    }
    else {
      return IterableUtil.shallowCopy(iterable);
    }
  }

  /**
   * Small syntactical helper.
   * @param <T>
   * @param items
   * @return
   */
  public static <T> T[] toArray(T... items) {
    return items;
  }

}
