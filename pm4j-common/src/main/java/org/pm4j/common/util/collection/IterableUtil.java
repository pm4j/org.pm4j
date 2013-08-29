package org.pm4j.common.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Shortcut methods for {@link Iterable}s.
 */
public class IterableUtil {

  /**
   * Provides the first item of the given list.
   *
   * @param list the list. May be <code>null</code>.
   * @return the first list item or <code>null</code> if the list was empty or <code>null</code>.
   */
  public static <T> T firstItem(Iterable<T> list) {
    if (list != null) {
      Iterator<T> i = list.iterator();
      if (i.hasNext()) {
        return i.next();
      }
    }

    return null;
  }


  /**
   * Generates a new {@link Iterable} that references the items of the given
   * {@link Iterable}.
   *
   * @param <T>
   *          The collection item fieldClass.
   * @param ori
   *          The iterable to get a shallow copy for.
   * @return The shallow copy.
   */
  public static <T> List<T> shallowCopy(Iterable<T> ori) {
    ArrayList<T> copy = new ArrayList<T>();

    for (T t : ori) {
      copy.add(t);
    }

    return copy;
  }

  /**
   * Provides a shallow copy of the given collection.
   * Removes all <code>null</code> items from <code>srcList</code>,
   *
   * @param srcList The source list. May be <code>null</code> and may contain <code>null</code> items.
   * @return A list with no <code>null</code> items. Never <code>null</code>.
   */
  public static <T> List<T> shallowCopyWithoutNulls(Collection<T> srcList) {
    if (srcList == null) {
      return new ArrayList<T>();
    }

    List<T> copy = new ArrayList<T>(srcList.size());
    for (T t : srcList) {
      if (t != null) {
        copy.add(t);
      }
    }

    return copy;
  }

  /**
   * Generates a collection that references the items of the given
   * {@link Iterator}.<br>
   * The iterator should be at its start position.
   *
   * @param <T>
   *          The collection item fieldClass.
   * @param iter
   *          The iterator to get a shallow copy for.
   * @return The shallow copy.
   */
  public static <T> List<T> shallowCopy(Iterator<T> iter) {
    ArrayList<T> copy = new ArrayList<T>();

    while (iter.hasNext()) {
      copy.add(iter.next());
    }

    return copy;
  }

  /**
   * Casts the {@link Iterable} to a {@link Collection} if it is one.
   * Otherwise it makes a shallow copy.
   *
   * @param iterable
   * @return the content as a collection.
   */
  public static <T> Collection<T> asCollection(Iterable<T> iterable) {
    return (iterable instanceof Collection)
        ? (Collection<T>) iterable
        : shallowCopy(iterable);
  }

  /**
   * @param i
   *          An {@link Iterable} to check.
   * @return <code>true</code> if the parameter is <code>null</code> or it has
   *         an empty item set.
   */
  public static boolean isEmpty(Iterable<?> i) {
    return i == null ||
           !i.iterator().hasNext();
  }

  private IterableUtil() {
    super();
  }
}
