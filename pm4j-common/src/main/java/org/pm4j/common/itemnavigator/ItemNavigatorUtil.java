package org.pm4j.common.itemnavigator;



/**
 * Some useful {@link ItemNavigator} methods.
 *
 * @author Olaf Boede
 */
public class ItemNavigatorUtil {

  /**
   * Navigates to the first record.
   * <p>
   * Precondition: {@link #getNumOfSelectedItems()} is greater than zero.
   *
   * @return the first item.
   */
  public static <T> T toFirst(ItemNavigator<T> itemNavigator) {
    return itemNavigator.navigateTo(0);
  }

  /**
   * Navigates to the first record.
   * <p>
   * Precondition: {@link #getNumOfSelectedItems()} is greater than zero.
   *
   * @return the first item.
   */
  public static <T> T toLast(ItemNavigator<T> itemNavigator) {
    int i = itemNavigator.getNumOfItems();
    return itemNavigator.navigateTo(i-1);
  }

  /**
   * Navigates to the previous record. <br>
   * Should only be called if {@link #hasPrev()} returns <code>true</code>.
   *
   * @return the previous item.
   */
  public static <T> T toPrev(ItemNavigator<T> itemNavigator) {
    int i = itemNavigator.getCurrentItemIdx();
    return itemNavigator.navigateTo(i-1);
  }

  /**
   * Navigates to the next record.<br>
   * Should only be called if {@link #hasNext()} returns <code>true</code>.
   *
   * @return the next item.
   */
  public static <T> T toNext(ItemNavigator<T> itemNavigator) {
    int i = itemNavigator.getCurrentItemIdx();
    return itemNavigator.navigateTo(i+1);
  }

  /**
   * @return <code>true</code> if there is a previous record.
   */
  public static boolean hasNext(ItemNavigator<?> itemNavigator) {
    int n = itemNavigator.getNumOfItems();
    return (n > 0) && (itemNavigator.getCurrentItemIdx() < n-1);
  }

  /** @return <code>true</code> if there is a previous record. */
  public static boolean hasPrev(ItemNavigator<?> itemNavigator) {
    return itemNavigator.getCurrentItemIdx() > 0;
  }

}
