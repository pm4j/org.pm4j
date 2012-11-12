package org.pm4j.common.util;

import java.util.Comparator;

/**
 * Inverts the compare result of the given base comparator.
 *
 * @author OBOEDE
 *
 * @param <T> The type of items to compare.
 */
public final class InvertingComparator<T> implements Comparator<T> {

  private final Comparator<T> baseComparator;

  public InvertingComparator(Comparator<T> baseComparator) {
    this.baseComparator = baseComparator;
  }

  @Override
  public int compare(T o1, T o2) {
    return -baseComparator.compare(o1, o2);
  }

  public Comparator<T> getBaseComparator() {
    return baseComparator;
  }


}
