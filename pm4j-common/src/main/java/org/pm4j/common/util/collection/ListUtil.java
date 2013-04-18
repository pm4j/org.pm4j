package org.pm4j.common.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

/**
 * Some helper methods for list handling.
 *
 * @author olaf boede
 */
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

  /**
   * Identifies the index position of the given item within the provided list.
   * <p>
   * Uses the <code>equals</code> implementation to compare the given item with
   * the list items.
   *
   * @param list the list to check.
   * @param item the item to get the position for.
   * @return the list index of the item or <code>-1</code> if the item is not part of the list.
   */
  public static <T> int getItemPos(List<T> list, T item) {
    if (list != null) {
      for (int i = 0; i < list.size(); ++i) {
          if (ObjectUtils.equals(list.get(i), item)) {
              return i;
          }
      }
    }

    // not found
    return -1;
}

  public static <T> T lastItemOrNull(List<T> list) {
    return (list == null || list.isEmpty())
        ? null
        : list.get(list.size()-1);
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

  public static <T> List<T> subListPage(List<T> baseList, int fromIndex, int maxPageSize) {
    int toIndex = Math.min(baseList.size(), fromIndex + maxPageSize);
    return baseList.subList(fromIndex, toIndex);
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
