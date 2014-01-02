package org.pm4j.common.itemnavigator;

/**
 * An interface that supports navigation over a set of items.<br>
 * It may be used for UI components that iterate over a set or items.
 *
 * @param <T> type of handled items.
 *
 * @author olaf boede
 */
public interface ItemNavigator<T> {

  /**
   * Navigates to a specific item.
   *
   * @param itemPos
   *          target item position.
   * @return the item.
   */
  T navigateTo(int itemPos);

  /**
   * Provides the currently selected item.
   *
   * @return the currently selected record.
   */
  T getCurrentItem();

  /**
   * @return the number of items to navigate over.
   */
  int getNumOfItems();

  /**
   * @return the position of the current record.
   */
  int getCurrentItemIdx();

  /**
   * Clears all cached information.
   */
  void clearCaches();

}