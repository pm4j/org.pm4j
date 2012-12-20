package org.pm4j.common.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link Selection} that consists of a set of {@link Selection}s.
 *
 * @param <T> the selection item type.
 *
 * @author olaf boede
 */
public class SelectionSet<T> implements Selection<T> {

  private static final long serialVersionUID = 1L;
  private List<Selection<T>> selections = new ArrayList<Selection<T>>();

  public void addSelection(Selection<T> s) {
    selections.add(s);
  }

  @Override
  public long getSize() {
    long size = 0;
    for (Selection<T> s : selections) {
      size += s.getSize();
    }
    return size;
  }

  @Override
  public boolean isSelected(T item) {
    for (Selection<T> s : selections) {
      if (s.isSelected(item)) {
        return true;
      }
    }
    // not selected
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    return new SelectionsIterator();
  }

  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    for (Selection<T> s : selections) {
      s.setIteratorBlockSizeHint(readBlockSize);
    }
  }

  /**
   * Iterates over the items of all selections.
   */
  class SelectionsIterator implements Iterator<T> {

    private Iterator<Selection<T>> selectionsIter;
    private Iterator<T> currentSelectionIter;
    private T next;

    public SelectionsIterator() {
      this.selectionsIter = selections.iterator();
     // this.currentSelection = sel
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public T next() {
      T result = next;
      _next();
      return result;
    }

    private T _next() {
      if (currentSelectionIter == null || !currentSelectionIter.hasNext()) {
        if (selectionsIter.hasNext()) {
          Selection<T> s = selectionsIter.next();
          currentSelectionIter = s.iterator();
          if (!currentSelectionIter.hasNext()) {
            return _next();
          }
        } else {
          return null;
        }
      }

      next = currentSelectionIter.next();
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
