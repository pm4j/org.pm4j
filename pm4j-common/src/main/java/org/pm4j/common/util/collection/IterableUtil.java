package org.pm4j.common.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    ArrayList<T> copy;

    if (ori instanceof Collection) {
      //some collections implementations provide more optimized way
      copy = new ArrayList<T>((Collection<T>) ori);
    } else {
      copy = new ArrayList<T>();

      for (T t : ori) {
        copy.add(t);
      }
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
   * Joins some {@link Iterable}s to a single one.
   *
   * @param iterables
   *          A set of {@link Iterable}s to join to a single {@link Iterable}.<br>
   *          May contain <code>null</code>s.
   * @return A single {@link Iterable}. Never <code>null.</code>
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> join(Iterable<? extends T>... iterables) {
    if (iterables.length == 0) {
      return Collections.emptyList();
    }
    if (iterables.length == 1) {
      return (Iterable<T>)iterables[0];
    }
    return new IterableChain<T>(iterables);
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

  /**
   * Checks whether two instances of {@link Iterable} are equal. Iterables are considered as equal when the following conditions takes place:
   * <ul>
   * <li> They have the same number of elements </li>
   * <li> Elements are stored in the same order </li>
   * <li> All elements are equal to each other according to their {@link #equals(Object)} implementation</li>
   * </ul>
   * @param a first iterable
   * @param b second iterable
   * @return {@code true} if iterables are equal.
   */
  public static <T> boolean areEqual(Iterable<T> a, Iterable<T> b) {
    if(a == b) {
      return true;
    }
    if(a == null || b == null) {
      return false;
    }

    Iterator<T> firstIterator = a.iterator();
    Iterator<T> secondIterator = b.iterator();

    while(firstIterator.hasNext() || secondIterator.hasNext()) {
      if(firstIterator.hasNext() && secondIterator.hasNext()) {
        if(!firstIterator.next().equals(secondIterator.next())) {
          return false;
        }
      } else {
        return false;
      }
    }

    return true;
  }

  private IterableUtil() {
    super();
  }

  private static class IterableChain<T> implements Iterable<T> {

    private final Iterable<T>[] iterables;

    @SuppressWarnings("unchecked")
    public IterableChain(Iterable<? extends T>... iterables) {
      this.iterables = (Iterable<T>[]) iterables;
    }

    @Override
    public Iterator<T> iterator() {
      return new Iterator<T>() {
        int iterableIdx = -1;
        Iterator<T> currentIterator;
        /** The iterator internally already knows the next item. */
        T next;

        {
          // initially we try to fill the next member to be able to answer hasNext().
          findNext();
        }

        @Override
        public boolean hasNext() {
          return next != null;
        }

        @Override
        public T next() {
          T result = next;
          findNext();
          return result;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

        private boolean findNext() {
          if (currentIterator != null && currentIterator.hasNext()) {
            next = currentIterator.next();
            return true;
          } else {
            while (iterableIdx < iterables.length-1) {
              ++iterableIdx;
              if (iterables[iterableIdx] != null) {
                currentIterator = iterables[iterableIdx].iterator();
                // recursive call that uses the just found currentIterator.
                return findNext();
              }
            }
            // all iterables are done.
            next = null;
            return false;
          }
        }
      };
    }

  }

  /**
   * Generates a comma separated string based on the <code>toString()</code> result
   * of the given items.
   *
   * @param items
   * @return The comma separated string.
   */
  public static String itemsToString(Iterable<?> items) {
    StringBuilder b = new StringBuilder(100);
    for (Object o : items) {
      if (b.length() > 0) {
        b.append(", ");
      }
      b.append(o);
    }
    return b.toString();
  }
}
