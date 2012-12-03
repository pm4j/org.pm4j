package org.pm4j.common.pageable;

import java.awt.print.Pageable;
import java.util.List;

import org.pm4j.common.pageable.querybased.ClickedIds;
import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterCompare;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterNot;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;


/**
 * Some common algorithms that should not be implemented redundantly for each
 * kind of {@link Pageable}.
 *
 * @author olaf boede
 */
public final class PageableCollectionUtil2 {

  /**
   * @param pageable
   *          The set to get the result for.
   * @return The index of the first item on the current page.
   *         <p>
   *         Starts with one.<br>
   *         Is zero if there are no items in this set.
   */
  public static long getIdxOfFirstItemOnPage(PageableCollection2<?> pageable) {
    int pageSize = pageable.getPageSize();
    long idx = (pageable.getNumOfItems() > 0)
            ? (pageSize * (pageable.getCurrentPageIdx()-1)) + 1
            : 0;

    if (idx < 0) {
      throw new RuntimeException("Item index out of bounds: " + idx);
    }
    return idx;
  }

  public static int getIdxOfFirstItemOnPageAsInt(PageableCollection2<?> pageable) {
    long idx = getIdxOfFirstItemOnPage(pageable);
    if (idx > Integer.MAX_VALUE) {
      throw new RuntimeException("Integer array based implementation are limites to 2exp32 items. Please check a pageable collection for large sets (e.g. PageableQueryCollection). Requested last item index was: " + idx);
    }
    return (int) idx;
  }

  /**
   * @param pageable
   *          The set to get the result for.
   * @return The index of the last item on the current page.
   *         <p>
   *         Starts with one.<br>
   *         Is zero if there are no items in this set.
   */
  public static long getIdxOfLastItemOnPage(PageableCollection2<?> pageable) {
    int pageSize = pageable.getPageSize();
    return (pageable.getNumOfItems() > 0)
            ? Math.min(
                    getIdxOfFirstItemOnPage(pageable) + pageSize - 1,
                    pageable.getNumOfItems())
            : 0;
  }

  public static int getIdxOfLastItemOnPageAsInt(PageableCollection2<?> pageable) {
    long idx = getIdxOfLastItemOnPage(pageable);
    if (idx > Integer.MAX_VALUE) {
      throw new RuntimeException("Integer array based implementation are limites to 2exp32 items. Please check a pageable collection for large sets (e.g. PageableQueryCollection). Requested last item index was: " + idx);
    }
    return (int) idx;
  }

  /**
   *
   * @param pageable The {@link PageableCollection2} to evaluate.
   * @param itemPos The position to get the page number for. Starts with <i>One</i>.
   * @return The page number for the given item position. <i>One</i> for the first page.
   */
  public static int getPageForItemPos(PageableCollection2<?> pageable, int itemPos) {
    int pageSize = pageable.getPageSize();
    return ((itemPos-1) / pageSize) + 1;
  }

  /**
   * @param pageable
   *          The set to get the result for.
   * @return The number of available pages.
   */
  public static int getNumOfPages(PageableCollection2<?> pageable) {
    long numOfItems = pageable.getNumOfItems();
    if (numOfItems > 0) {
      int pageSize = pageable.getPageSize();
      int fullPages = (int)(numOfItems / pageSize);
      int remainder = (int)(numOfItems % pageSize);
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
  public static void navigateToPrevPage(PageableCollection2<?> pageable) {
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
  public static void navigateToNextPage(PageableCollection2<?> pageable) {
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
  public static void navigateToFirstPage(PageableCollection2<?> pageable) {
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
  public static void navigateToLastPage(PageableCollection2<?> pageable) {
    int currentPageIdx = pageable.getCurrentPageIdx();
    int lastPageIdx = getNumOfPages(pageable);
    if (currentPageIdx < lastPageIdx) {
      pageable.setCurrentPageIdx(lastPageIdx);
    }
  }

  /**
   * @param pageable
   *          the collection to handle.
   * @return <code>true</code> if there is a following page to navigate to.
   */
  public static boolean hasNextPage(PageableCollection2<?> pageable) {
    int currentPageIdx = pageable.getCurrentPageIdx();
    return currentPageIdx < getNumOfPages(pageable);
  }

  /**
   * @param pageable
   *          The set to handle.
   * @return <code>true</code> if there is a previous page to navigate to.
   */
  public static boolean hasPrevPage(PageableCollection2<?> pageable) {
    return pageable.getCurrentPageIdx() > 1;
  }

  /**
   * Ensures that the page index is not out of range.
   * <p>
   * If the current page index is behind the last page it will be corrected to
   * point to the last page.
   *
   * @param pageable the collection to handle.
   */
  public static void ensureCurrentPageInRange(PageableCollection2<?> pageable) {
    // ensure that the current page index is not behind the last page after filtering.
    int numOfPages = getNumOfPages(pageable);
    int currentPageIdx = pageable.getCurrentPageIdx();
    if (currentPageIdx >= numOfPages) {
      pageable.setCurrentPageIdx(numOfPages == 0 ? 1 : numOfPages);
    }

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
  public static <T> void setAllOnPageSelected(PageableCollection2<T> pageable, boolean doSelect) {
    SelectionHandler<T> handler = pageable.getSelectionHandler();
    handler.select(doSelect, pageable.getItemsOnPage());
  }

  /**
   * @param pageable
   *          The set to handle.
   * @return <code>true</code> if all items on the current page are selected.<p>
   *         For an empty page always <code>false</code>.
   */
  public static <T> boolean isAllOnPageSelected(PageableCollection2<T> pageable) {
    SelectionHandler<T> handler = pageable.getSelectionHandler();
    List<T> itemsOnPage = pageable.getItemsOnPage();
    if (itemsOnPage.isEmpty()) {
      return false;
    }

    for (T i : itemsOnPage) {
      if (!handler.getSelection().isSelected(i)) {
        return false;
      }
    }
    // all items are selected:
    return true;
  }

  public static <T> void selectFirstOnPage(PageableCollection2<T> pageable) {
    List<T> pageItems = pageable.getItemsOnPage();
    if (pageItems.size() > 0) {
      pageable.getSelectionHandler().select(true, pageItems.get(0));
    }
  }

  /**
   * Generates a query based on query restrictions and a set of
   * {@link ClickedIds}.<br>
   * The generated query selects the set of selected objects.
   *
   * @param idAttr
   *          the ID attribute that is related to the clicked id's.
   * @param queryParams
   *          the query params for the base query.
   * @param clickedIds
   *          the set of individually selected/deselected item ids.
   * @return the generated filter restriction that represents the selection.
   */
  public static <T_ID> QueryParams makeSelectionQueryParams(AttrDefinition idAttr, QueryParams queryParams, ClickedIds<T_ID> clickedIds) {
    QueryParams qp = queryParams.clone();

    FilterExpression idFilterExpr = new FilterCompare(idAttr, new CompOpIn(), clickedIds.getIds());
    if (clickedIds.isInvertedSelection()) {
      // Negative filter: use the query and de-select the clicked id's
      // No additional restriction if there are no clicked id's.
      if (!clickedIds.getIds().isEmpty()) {
        idFilterExpr = new FilterNot(idFilterExpr);
        FilterExpression qf = queryParams.getFilterExpression();
        qp.setFilterExpression((qf != null)
            ? new FilterAnd(qf, idFilterExpr)
            : idFilterExpr);
      }
    } else {
      // Positive filter: select only the clicked id's.
      qp.setFilterExpression(idFilterExpr);
    }

    return qp;
  }
}
