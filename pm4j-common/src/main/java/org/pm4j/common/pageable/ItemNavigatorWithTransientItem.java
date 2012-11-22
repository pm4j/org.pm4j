package org.pm4j.common.pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * A navigator over a set of persistent and transient items.
 * <p>
 * The persistent items are handled by a separate {@link ItemNavigator}.</br>
 * The transient items are handled within a simple list.
 *
 * @param <T>
 *
 * @author olaf boede
 */
public class ItemNavigatorWithTransientItem<T> implements ItemNavigator<T> {

  private final ItemNavigator<T> baseNavigator;
  private List<T> transientItems = new ArrayList<T>();
  private int currentPos;

  /**
   * @param baseNavigator
   *          a navigator that usually provides the persistent items to navigate
   *          over.
   * @param transientItems
   *          the transient items to handle.
   */
  public ItemNavigatorWithTransientItem(ItemNavigator<T> baseNavigator, T... transientItems) {
    assert baseNavigator != null;
    this.baseNavigator = baseNavigator;
    for (T t : transientItems) {
      this.transientItems.add(t);
    }
  }

  /**
   * @param baseNavigator
   *          a navigator that usually provides the persistent items to navigate
   *          over.
   */
  public ItemNavigatorWithTransientItem(ItemNavigator<T> baseNavigator) {
    assert baseNavigator != null;
    this.baseNavigator = baseNavigator;
  }

  @Override
  public T navigateTo(int itemPos) {
    currentPos = itemPos;
    if (currentPos < baseNavigator.getNumOfItems()) {
      baseNavigator.navigateTo(itemPos);
    }

    return getCurrentItem();
  }

  @Override
  public T getCurrentItem() {
    int n = baseNavigator.getNumOfItems();
    return (currentPos < n) ? baseNavigator.navigateTo(currentPos) : transientItems.get(currentPos - n);
  }

  @Override
  public int getNumOfItems() {
    return baseNavigator.getNumOfItems() + transientItems.size();
  }

  @Override
  public int getCurrentItemIdx() {
    return currentPos;
  }

  /**
   * @return the backing navigator for the set of persistent items.
   */
  public ItemNavigator<T> getBaseNavigator() {
    return baseNavigator;
  }

  /**
   * @return the set of transient items.<br>
   *         Never <code>null</code>.
   */
  public List<T> getTransientItems() {
    return transientItems;
  }

  /**
   * Adds a transient item to handle.
   *
   * @param item the new item.<br>
   * Should not be <code>null</code>.
   * @return the position of the added item.
   */
  public int addTransientItem(T item) {
      assert item != null;

      transientItems.add(item);
      return getNumOfItems() - 1;
  }

}