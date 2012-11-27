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
