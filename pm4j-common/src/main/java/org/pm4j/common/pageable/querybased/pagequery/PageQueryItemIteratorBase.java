package org.pm4j.common.pageable.querybased.pagequery;

import java.util.Iterator;
import java.util.List;

/**
 * An iterator base class that supports block wise iteration.
 * <p>
 * Sub classes need to implement {@link #getItems(long, int)}.
 * <p>
 * If the results provided by {@link #getItems(long, int)} may still contain
 * items that don't belong to the selected item set, this may be corrected by an
 * implementation of {@link #isItemSelected(Object)}.
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 *          the item type.
 */
public abstract class PageQueryItemIteratorBase<T_ITEM> implements Iterator<T_ITEM> {

  private long idx = 0;
  private List<T_ITEM> pageItems;
  private T_ITEM item;
  private int pagePos = -1;
  private int iteratorBlockSize;

  public PageQueryItemIteratorBase(int iteratorBlockSize) {
    this.iteratorBlockSize = iteratorBlockSize;
    doNext();
  }

  protected abstract List<T_ITEM> getItems(long startIdx, int blockSize);

//  protected abstract int getItemCount();

  /**
   * Gets called for all results of {@link #getItems(long, int)}. If this method
   * returns <code>false</code>, this item will not be used by this iterator.
   * <p>
   * The default implementation always returns <code>true</code>.
   *
   * @param item the item it check.
   * @return <code>true</code> if the given item is part of the selection.
   */
  protected boolean isItemSelected(T_ITEM item) {
    return true;
  }


  @Override
  public boolean hasNext() {
    return item != null;
  }

  @Override
  public T_ITEM next() {
    T_ITEM result = pageItems.get(pagePos);
    doNext();
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void doNext() {
    boolean nextFound = false;
    do {
      boolean doQuery = (pagePos == -1) ||
                        (pagePos == pageItems.size()-1);

      if (doQuery) {
        // if previous read block was not completely filled, the query call sequence already did provide
        // all available results.
        if ((pageItems != null) && (pageItems.size() < iteratorBlockSize)) {
          item = null;
          pageItems = null;
          return;
        }

        pagePos = 0;
        pageItems = getItems(idx, iteratorBlockSize);
        if (pageItems == null || pageItems.isEmpty()) {
          item = null;
          pageItems = null;
          return;
        }
      }
      else {
        ++pagePos;
      }

      ++idx;
      item = pageItems.get(pagePos);
      nextFound = isItemSelected(item);
    }
    while(!nextFound);
  }

}