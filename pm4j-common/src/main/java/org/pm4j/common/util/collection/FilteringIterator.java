package org.pm4j.common.util.collection;

import java.util.Iterator;

/**
 * An iterator that filters the items of a given base iterator.
 */
public abstract class FilteringIterator<T> implements Iterator<T> {

  private Iterator<T> baseIter;
  private T nextItem;
  
  public FilteringIterator(Iterable<T> iterable) {
    this.baseIter = iterable.iterator();
    _next();
  }
  
  @Override
  public boolean hasNext() {
    return nextItem != null;
  }

  @Override
  public T next() {
    T result = nextItem;
    _next();
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  protected abstract boolean doesMatch(T t);
  
  private void _next() {
    if (baseIter.hasNext()) {
      nextItem = baseIter.next();
      while(!doesMatch(nextItem)) {
        if (baseIter.hasNext()) {
          nextItem = baseIter.next();
        }
        else {
          nextItem = null;
          break;
        }
      }
    }
    else {
      nextItem = null;
    }
  }
}
