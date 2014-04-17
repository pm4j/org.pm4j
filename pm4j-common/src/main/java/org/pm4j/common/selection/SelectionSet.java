package org.pm4j.common.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link Selection} that consists of a set of {@link Selection}s.
 *
 * @param <T> The selection item type.
 *
 * @author Olaf Boede
 */
public class SelectionSet<T> implements Selection<T> {

  private static final long serialVersionUID = 1L;
  /** The set of sub-selections. */
  private List<Selection<T>> selections = new ArrayList<Selection<T>>();

  /**
   * Creates a selection that aggregates the given selections.
   * <p>
   * LIMITATION: If an item is part of multiple given selections, the result of {@link #getSize()} may
   * consider the item multiple times and the {@link #iterator()} may deliver the item multiple times
   * as well.
   *
   * @param srcSelections
   * @return
   */
  public static <T> Selection<T> aggregateSelections(Selection<T>... srcSelections) {
    List<Selection<T>> selections = new ArrayList<Selection<T>>();
    for (Selection<T> s : srcSelections) {
      if (s instanceof SelectionSet) {
        for (Selection<T> s2 : ((SelectionSet<T>)s).selections) {
          if (!s2.isEmpty()) {
            selections.add(s2);
          }
        }
      } else {
        if (!s.isEmpty()) {
          selections.add(s);
        }
      }
    }

    switch (selections.size()) {
    case 0: return EmptySelection.getEmptySelection();
    case 1: return selections.get(0);
    default: return new SelectionSet<T>(selections);
    }
  }

  /**
   * Creates a selection based on a set of given selections.
   *
   * @param srcSelections The set of selections to create a single selection for.
   */
  public SelectionSet(Selection<T>... srcSelections) {
    for (Selection<T> s : srcSelections) {
      if (s instanceof SelectionSet) {
        selections.addAll(((SelectionSet<T>)s).selections);
      } else {
        selections.add(s);
      }
    }
  }

  private SelectionSet(List<Selection<T>> srcSelections) {
    assert srcSelections != null;
    this.selections = srcSelections;
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
  public boolean isEmpty() {
    return getSize() == 0;
  }

  @Override
  public boolean contains(T item) {
    for (Selection<T> s : selections) {
      if (s.contains(item)) {
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
