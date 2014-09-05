package org.pm4j.common.itemnavigator;

import java.util.Arrays;
import java.util.List;

/**
 * An {@link ItemNavigator} for a {@link List}.<br>
 * Starts having the first item at the current position.
 *
 * @param <T> The item type.
 *
 * @author Olaf Boede
 */
public class ListItemNavigator<T> implements ItemNavigator<T> {

  private int idx = 0;
  private final List<T> list;

  public ListItemNavigator(T... items) {
    this(Arrays.asList(items));
  }

  public ListItemNavigator(List<T> items) {
    this.list = items;
  }

  @Override
  public T navigateTo(int itemPos) {
    assert itemPos >= 0 : "Item position should not be less than zero.";
    assert itemPos < list.size() : "Item position should be less than the number or items.";
    idx = itemPos;
    return getCurrentItem();
  }

  @Override
  public T getCurrentItem() {
    assert idx >= 0 : "Item position should not be less than zero.";
    assert idx < list.size() : "Item position should be less than the number or items.";
    return list.get(idx);
  }

  @Override
  public int getNumOfItems() {
    return list.size();
  }

  @Override
  public int getCurrentItemIdx() {
    return idx;
  }

  @Override
  public void clearCaches() {
  }

}
