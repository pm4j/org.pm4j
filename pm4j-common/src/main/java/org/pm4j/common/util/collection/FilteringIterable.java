package org.pm4j.common.util.collection;

import java.util.Iterator;

/**
 * An iterator that filters the items of a given base iterator.
 */
public abstract class FilteringIterable<T> implements Iterable<T> {

  private Iterable<T> baseIterable;
  
  public FilteringIterable(Iterable<T> baseIterable) {
    this.baseIterable = baseIterable;
  }
  
  @Override
  public Iterator<T> iterator() {
    return new FilteringIterator<T>(baseIterable) {
      @Override
      protected boolean doesMatch(T t) {
        return FilteringIterable.this.doesMatch(t);
      }
    };
  }

  protected abstract boolean doesMatch(T t);
}
