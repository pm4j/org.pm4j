package org.pm4j.core.pm.pageable;

import java.awt.print.Pageable;
import java.util.Iterator;
import java.util.List;

/**
 * Some common algorithms that should not be implemented redundantly for each
 * kind of {@link Pageable}.
 *
 * @author olaf boede
 */
public final class PageableCollectionUtil {

  /**
   * @param pageable
   *          The set to get the result for.
   * @return The index of the first item on the current page.
   *         <p>
   *         Starts with one.<br>
   *         Is zero if there are no items in this set.
   */
  public static int getIdxOfFirstItemOnPage(PageableCollection<?> pageable) {
    int pageSize = pageable.getPageSize();
    return (pageable.getNumOfItems() > 0)
            ? (pageSize * (pageable.getCurrentPageIdx()-1)) + 1
            : 0;
  }

  /**
   * @param pageable
   *          The set to get the result for.
   * @return The index of the last item on the current page.
   *         <p>
   *         Starts with one.<br>
   *         Is zero if there are no items in this set.
   */
  public static int getIdxOfLastItemOnPage(PageableCollection<?> pageable) {
    int pageSize = pageable.getPageSize();
    return (pageable.getNumOfItems() > 0)
            ? Math.min(
                    getIdxOfFirstItemOnPage(pageable) + pageSize - 1,
                    pageable.getNumOfItems())
            : 0;
  }

  /**
   *
   * @param pageable The {@link PageableCollection} to evaluate.
   * @param itemPos The position to get the page number for. Starts with <i>One</i>.
   * @return The page number for the given item position. <i>One</i> for the first page.
   */
  public static int getPageForItemPos(PageableCollection<?> pageable, int itemPos) {
    int pageSize = pageable.getPageSize();
    return ((itemPos-1) / pageSize) + 1;
  }

  /**
   * @param pageable
   *          The set to get the result for.
   * @return The number of available pages.
   */
  public static int getNumOfPages(PageableCollection<?> pageable) {
    int numOfItems = pageable.getNumOfItems();
    if (numOfItems > 0) {
      int pageSize = pageable.getPageSize();
      int fullPages = numOfItems / pageSize;
      int remainder = numOfItems % pageSize;
      return fullPages + (remainder > 0 ? 1 : 0);
    } else {
      return 0;
    }
  }

  /**
   * Navigates one page back.
   * <p>
   * Does nothing if the set is already on the first page or has no items.
   *
   * @param pageable
   *          The set to handle.
   */
  public static void navigateToPrevPage(PageableCollection<?> pageable) {
    int currentPageIdx = pageable.getCurrentPageIdx();
    if (currentPageIdx > 1) {
      pageable.setCurrentPageIdx(currentPageIdx - 1);
    }
  }

  /**
   * Navigates one page forward.
   * <p>
   * Does nothing if the set is already on the last page or has no items.
   *
   * @param pageable
   *          The set to handle.
   */
  public static void navigateToNextPage(PageableCollection<?> pageable) {
    int currentPageIdx = pageable.getCurrentPageIdx();
    if (currentPageIdx < getNumOfPages(pageable)) {
      pageable.setCurrentPageIdx(currentPageIdx + 1);
    }
  }

  /**
   * Navigates to the last page.
   * <p>
   * Does nothing if the set is already on the last page or has no items.
   *
   * @param pageable
   *          The set to handle.
   */
  public static void navigateToFirstPage(PageableCollection<?> pageable) {
    int currentPageIdx = pageable.getCurrentPageIdx();
    if (currentPageIdx > 1) {
      pageable.setCurrentPageIdx(1);
    }
  }

  /**
   * Navigates to the last page.
   * <p>
   * Does nothing if the set is already on the last page or has no items.
   *
   * @param pageable
   *          The set to handle.
   */
  public static void navigateToLastPage(PageableCollection<?> pageable) {
    int currentPageIdx = pageable.getCurrentPageIdx();
    int lastPageIdx = getNumOfPages(pageable);
    if (currentPageIdx < lastPageIdx) {
      pageable.setCurrentPageIdx(lastPageIdx);
    }
  }

  /**
   * @param pageable
   *          The set to handle.
   * @return <code>true</code> if there is a following page to navigate to.
   */
  public static boolean hasNextPage(PageableCollection<?> pageable) {
    int currentPageIdx = pageable.getCurrentPageIdx();
    return currentPageIdx < getNumOfPages(pageable);
  }

  /**
   * @param pageable
   *          The set to handle.
   * @return <code>true</code> if there is a previous page to navigate to.
   */
  public static boolean hasPrevPage(PageableCollection<?> pageable) {
    return pageable.getCurrentPageIdx() > 1;
  }

  /**
   * Select or deselect all items displayed on the current page.
   *
   * @param pageable
   *          The set to handle.
   * @param doSelect
   *          <code>true</code> selects all items of the current page.<br>
   *          <code>false</code> does deselect all items of the current page.
   */
  // TODO: rename to 'selectAllOnPage'
  public static <T> void setAllOnPageSelected(PageableCollection<T> pageable, boolean doSelect) {
    for (T i : pageable.getItemsOnPage()) {
      pageable.select(i, doSelect);
    }
  }

  /**
   * @param pageable
   *          The set to handle.
   * @return <code>true</code> if all items on the current page are selected.<p>
   *         For an empty page always <code>false</code>.
   */
  public static <T> boolean isAllOnPageSelected(PageableCollection<T> pageable) {
    List<T> itemsOnPage = pageable.getItemsOnPage();
    if (itemsOnPage.isEmpty()) {
      return false;
    }

    for (T i : itemsOnPage) {
      if (!pageable.isSelected(i)) {
        return false;
      }
    }
    // all items are selected:
    return true;
  }

  public static <T> void selectAll(PageableCollection<T> pageable, boolean doSelect) {
    Iterator<T> i = doSelect
          ? pageable.getAllItemsIterator()
          : pageable.getSelectedItems().iterator();
    while (i.hasNext()) {
      pageable.select(i.next(), doSelect);
    }
  }

// TODO olaf: maintain a set of selected items of the current filter settings
//  public boolean isAllSelected(PageableCollection<?> pageable) {
//    return pageable.getSelectedItems().size() == pageable.getNumOfItems();
//  }

}
