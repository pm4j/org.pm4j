package org.pm4j.common.pageable;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.common.pageable.inmem.ItemNavigatorInMem;

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
public class ItemNavigatorWithAdditionalItems<T> implements ItemNavigator<T> {

  private final ItemNavigator<T> baseNavigator;
  private List<T> additionalItems = new ArrayList<T>();
  private int currentPos;

  /**
   * @param baseNavigator
   *          a navigator that usually provides the persistent items to navigate
   *          over.
   * @param transientItems
   *          the transient items to handle.
   */
  public ItemNavigatorWithAdditionalItems(ItemNavigator<T> baseNavigator, T... transientItems) {
    assert baseNavigator != null;
    this.baseNavigator = baseNavigator;
    for (T t : transientItems) {
      this.additionalItems.add(t);
    }
  }

  /**
   * @param baseNavigator
   *          a navigator that usually provides the persistent items to navigate
   *          over.
   *          <p>
   *          May be <code>null</code>. This creates a navigator having only
   *          additional items.
   */
  public ItemNavigatorWithAdditionalItems(ItemNavigator<T> baseNavigator) {
    assert baseNavigator != null;
    this.baseNavigator = baseNavigator != null
        ? baseNavigator
        : new ItemNavigatorInMem<T>(null);
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
    return (currentPos < n)
        ? baseNavigator.navigateTo(currentPos)
        : additionalItems.isEmpty()
            ? null
            : additionalItems.get(currentPos - n);
  }

  @Override
  public int getNumOfItems() {
    return baseNavigator.getNumOfItems() + additionalItems.size();
  }

  @Override
  public int getCurrentItemIdx() {
    return currentPos;
  }

  @Override
  public void clearCaches() {
    baseNavigator.clearCaches();
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
  public List<T> getAdditionalItems() {
    return additionalItems;
  }

  /**
   * Adds a transient item to handle.
   *
   * @param item the new item.<br>
   * Should not be <code>null</code>.
   * @return the position of the added item.
   */
  public int addAdditionalItem(T item) {
      assert item != null;

      additionalItems.add(item);
      return getNumOfItems() - 1;
  }

}
