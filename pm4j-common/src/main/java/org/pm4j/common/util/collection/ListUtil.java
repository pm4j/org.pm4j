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

  /**
   * Transforms a list to a single item or to a <code>null</code> if the list has no item.
   * <p>
   * Throws an {@link IllegalArgumentException} if the list has more than one item.
   *
   * @param list the list to transform. May be <code>null</code>.
   * @return the single list item or <code>null</code>.
   */
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
   * Transforms a list to its first single item or to a <code>null</code> if the list has no item.
   * <p>
   *
   * @param list the list to transform. May be <code>null</code>.
   * @return the first single list item or <code>null</code>.
   */
  public static <T> T listToFirstItemOrNull(List<T> list) {
    if (list == null) {
      return null;
    }
    else {
      switch(list.size()) {
      case 0: return null;
      default: return list.get(0);
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

  /**
   * Provides the last list item or <code>null</code> if there is no item or the given list is <code>null</code>.
   *
   * @param list The list.
   * @return the last list item or <code>null</code>.
   */
  public static <T> T lastItemOrNull(List<T> list) {
    return (list == null || list.isEmpty())
        ? null
        : list.get(list.size()-1);
  }

  /**
   * Joins the given set of collections to a single list containing all items.
   *
   * @param collections The given set of collections.
   * @return a list containing all items.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> List<T> collectionsToList(Collection... collections) {
    List<T> list = new ArrayList<T>();
    for (Collection coll : collections) {
      list.addAll(coll);
    }
    return list;
  }

  /**
   * Provides the given collection as a {@link List}.
   * <p>
   * If it is already a list, it will just return the given instance.<br>
   * Otherwise it provides a new list containing all items of the given container.
   *
   * @param iterable The set of items.
   * @return a list containing all items.
   */
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
   * Provides a shallow copy of the given collection.
   *
   * @param src The source collection. May be <code>null</code>.
   * @return a new list containing all source items. Is never <code>null</code>.
   */
  public static <T> List<T> shallowCopy(Collection<T> src) {
    return (src == null)
            ? new ArrayList<T>()
            : new ArrayList<T>(src);
  }

  /**
   * Provides a sub list of the given list instance.
   *
   * @param baseList
   * @param fromIndex
   * @param maxPageSize
   * @return
   */
  public static <T> List<T> subListPage(List<T> baseList, int fromIndex, int maxPageSize) {
    int toIndex = Math.min(baseList.size(), fromIndex + maxPageSize);
    return baseList.subList(fromIndex, toIndex);
  }

  /**
   * Adds the given item set if it is not already a member of the given
   * collection.
   *
   * @param targetCollection
   *          The collection to manage.
   * @param src
   *          The set of items to add if it is not already part of the target
   *          collection.<br>
   *          May be <code>null</code>.
   * @return the number of added items.
   */
  public static <T> int addItemsNotYetInCollection(Collection<T> targetCollection, Iterable<T> src) {
    int numAddedItems = 0;
    if (src != null) {
      for (T item : src) {
        if (!targetCollection.contains(item)) {
          targetCollection.add(item);
          ++numAddedItems;
        }
      }
    }
    return numAddedItems;
  }

  /**
   * Small syntactical helper.
   * @param <T>
   * @param items
   * @return
   */
  @Deprecated
  public static <T> T[] toArray(T... items) {
    return items;
  }




}
