package org.pm4j.common.pageable.inmem;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.pageable.ItemNavigator;
import org.pm4j.common.selection.Selection;

/**
 * A navigator that keeps a collection of selected items in memory.
 *
 * @param <T> handled item type.
 *
 * @author olaf boede
 */
public class ItemNavigatorInMem<T> implements ItemNavigator<T> {

  private List<T> items = new ArrayList<T>();
  private int currentItemIdx;

  public ItemNavigatorInMem(Selection<T> selection) {
    if (selection != null) {
      for (T t : selection) {
        items.add(t);
      }
    }
  }

  @Override
  public T navigateTo(int itemPos) {
    currentItemIdx = Math.min(itemPos, getNumOfItems());
    currentItemIdx = Math.max(currentItemIdx, 0);

    return getCurrentItem();
  }

  @Override
  public T getCurrentItem() {
    return items.isEmpty() ? null : items.get(currentItemIdx);
  }

  @Override
  public int getNumOfItems() {
    return items.size();
  }

  @Override
  public int getCurrentItemIdx() {
    return currentItemIdx;
  }

  @Override
  public void clearCaches() {
    // nothing to do.
  }
}
