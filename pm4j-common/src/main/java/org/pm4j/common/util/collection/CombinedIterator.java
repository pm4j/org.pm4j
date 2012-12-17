package org.pm4j.common.util.collection;

import java.util.Iterator;

/**
 * An iterator that combines the content of two underlying iterators to provide
 * the complete iteration result.
 *
 * @param <T_ITEM> type of handled items.
 *
 * @author olaf boede
 */
public class CombinedIterator<T_ITEM> implements Iterator<T_ITEM> {

  private final Iterator<T_ITEM> baseIterator;
  private final Iterator<T_ITEM> secondIterator;

  public CombinedIterator(Iterator<T_ITEM> baseIterator, Iterator<T_ITEM> secondIterator) {
    assert baseIterator != null;
    assert secondIterator != null;

    this.baseIterator = baseIterator;
    this.secondIterator = secondIterator;
  }

  @Override
  public boolean hasNext() {
    return baseIterator.hasNext() || secondIterator.hasNext();
  }

  @Override
  public T_ITEM next() {
    return baseIterator.hasNext() ? baseIterator.next() : secondIterator.next();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}