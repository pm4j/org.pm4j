package org.pm4j.common.itemnavigator;


/**
 * An {@link ItemNavigator} that iterates over a collection of items having a
 * different type.
 *
 * @author Olaf Boede
 */
public class ConvertingItemNavigator<T_EXTERNAL, T_INTERNAL> implements ItemNavigator<T_EXTERNAL> {

  public interface Converter<T_EXTERNAL, T_INTERNAL> {

    /**
     * @param selectedItem
     *          an item provided by the internal navigator.
     * @return an instance that can be used by the external item navigator
     *         interface.
     */
    T_EXTERNAL toNavigatorItemType(T_INTERNAL selectedItem);

  }

  private final ItemNavigator<T_INTERNAL> queryNavigator;
  private final Converter<T_EXTERNAL, T_INTERNAL> converter;
  private T_EXTERNAL currentItem;

  /**
   * @param queryNavigator
   * @param converter
   */
  public ConvertingItemNavigator(ItemNavigator<T_INTERNAL> queryNavigator, Converter<T_EXTERNAL, T_INTERNAL> converter) {
    this.queryNavigator = queryNavigator;
    this.converter = converter;
  }

  @Override
  public T_EXTERNAL navigateTo(int itemPos) {
    // prevent unnecessary calls if this gets called multiple times for the same position.
    if (queryNavigator.getCurrentItemIdx() != itemPos || currentItem == null) {
      currentItem = converter.toNavigatorItemType(queryNavigator.navigateTo(itemPos));
    }
    return currentItem;
  }

  @Override
  public T_EXTERNAL getCurrentItem() {
    if (currentItem == null) {
      T_INTERNAL internalItem = queryNavigator.getCurrentItem();
      if (internalItem != null) {
        currentItem = converter.toNavigatorItemType(internalItem);
      }
    }
    return currentItem;
  }

  @Override
  public int getNumOfItems() {
    return queryNavigator.getNumOfItems();
  }

  @Override
  public int getCurrentItemIdx() {
    return queryNavigator.getCurrentItemIdx();
  }

  @Override
  public void clearCaches() {
    queryNavigator.clearCaches();
    currentItem = null;
  }

}
