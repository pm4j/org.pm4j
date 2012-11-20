package org.pm4j.common.pageable;

import java.util.Iterator;
import java.util.List;

import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.selection.SelectionHandler;

/**
 * Common interface for pageable object sets.
 * <p>
 * It allows to navigate over a set of objects using a page index.
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 *          The type of items handled by this set.
 */
public interface PageableCollection2<T_ITEM> extends Iterable<T_ITEM> {

  /**
   * Provides the {@link QueryParams} that defines filter constraints and sort order.
   *
   * @return the {@link QueryParams}. Never <code>null</code>.
   */
  QueryParams getQueryParams();

  /**
   * The set of query parameters that usually may be configured by the end user.
   *
   * @return the {@link QueryOptions}. Never <code>null</code>.
   */
  QueryOptions getQueryOptions();

  /**
   * Provides the set of item to display on the current page.
   * <p>
   * Throws an exception if the current page is less than the first one.<br>
   * Does not throw an exception if the current page is behind the last one.
   * In this case it just returns an empty page.
   * <p>
   * It is done this way to be able to react gracefully in case of a dynamic
   * collection that was changed by another process.
   *
   * @return The set of item to display on the current page.
   */
  List<T_ITEM> getItemsOnPage();

  /**
   * @return The number of items to reserve space for on the current page.
   */
  int getPageSize();

  /**
   * @param newSize The new page size. Should be greater than zero.
   */
  void setPageSize(int newSize);

  /**
   * @return The current page number. Starts with one.
   */
  int getCurrentPageIdx();

  /**
   * @param pageIdx
   *          Navigates to the specified page.
   *          <p>
   *          Valid range: 1 .. {@link #getTotalPageNum()}.
   */
  void setCurrentPageIdx(int pageIdx);

  /**
   * Provides the total number of objects within this set.
   *         <p>
   *         A filter definition may influence this number.
   *
   * @return the number of filtered items.
   */
  long getNumOfItems();

  /**
   * In some filtering scenarios it may happen that it is needed to know the
   * unfiltered total item size (e.g. for validation or display purposes).
   * <p>
   * This method provides this size.
   * <p>
   * Please make sure that the concrete implementation supports that functionality.<br>
   * E.g. a collection that is based on DB queries may have some extra effort to support
   * this functionality...
   *
   * @return The total number of object within this set.
   *         <p>
   *         A filter definition does <b>not</b> influence this number.
   */
  long getUnfilteredItemCount();

  /**
   * Provides an iterator over the collection of items.<br>
   * The current filter and sort order settings are considered.
   * <p>
   * ATTENTION: In case of large collections with page wise data loading the
   * provided iterator has some performance impact.
   *
   * @return An iterator over all items that are visible according to the
   *         current filter settings.
   */
  Iterator<T_ITEM> iterator();

  /**
   * Provides a handler for item selection.
   *
   * @return the (collection type specific) selection handler.
   */
  SelectionHandler<T_ITEM> getSelectionHandler();

  /**
   * Clears all cached information.<br>
   * Gets called if everything needs to be re-evalueated.
   */
  void clearCaches();
}
