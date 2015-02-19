package org.pm4j.common.pageable;

import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.modifications.Modifications;
import org.pm4j.common.pageable.querybased.idquery.MaxQueryResultsViolationException;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.util.beanproperty.PropertyChangeSupported;

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
public interface PageableCollection<T_ITEM> extends Iterable<T_ITEM>, PropertyChangeSupported {

  /**
   * Gets fired whenever the page size was modified.<br>
   * The fired {@link PropertyChangeEvent} provides the old and the new page size.<br>
   * See also: {@link #setPageSize(int)}.
   */
  public static final String PROP_PAGE_SIZE = "pageable.page.size";

  /**
   * Gets fired whenever the current page index was modified.<br>
   * The fired {@link PropertyChangeEvent} provides the old and the new page index.<br>
   * See also: {@link #setPageIdx(long)}.
   */
  public static final String PROP_PAGE_IDX = "pageable.page.idx";

  /**
   * Gets fired whenever an item gets added to the collection.<br>
   * Provides the added item in {@link PropertyChangeEvent#getNewValue()}.<br>
   * See also: {@link ModificationHandler#addItem(Object)}.
   */
  public static final String EVENT_ITEM_ADD = "pageable.item.add";

  /**
   * Gets fired whenever the set of updated items was changed.<br>
   * Provides the item modification change:
   * <ul>
   *  <li>{@link PropertyChangeEvent#getOldValue()} provides a boolean for the old item changed state .</li>
   *  <li>{@link PropertyChangeEvent#getNewValue()} provides a boolean for the new item changed state .</li>
   * </ul>
   * See also: {@link ModificationHandler#registerUpdatedItem(Object, boolean)}.
   */
  public static final String EVENT_ITEM_UPDATE = "pageable.item.update";

  /**
   * A vetoable event that gets fired whenever a set of selected items was deleted.<br>
   * Provides the set of deleted items in {@link PropertyChangeEvent#getOldValue()}.<br>
   * See also: {@link ModificationHandler#removeSelectedItems()}.
   */
  public static final String EVENT_REMOVE_SELECTION = "pageable.remove-selection";

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
   * Adjusts the new page size and fires a {@link PropertyChangeEvent}
   * {@link #PROP_PAGE_SIZE} if it was changed.
   *
   * @param newSize
   *          The new page size. Should be greater than zero.
   */
  void setPageSize(int newSize);

  /**
   * @return The current page number. Starts with zero.
   */
  long getPageIdx();

  /**
   * Switches to the new page index and fires a {@link PropertyChangeEvent}
   * {@link #PROP_PAGE_IDX} if it was changed.
   *
   * @param pageIdx
   *          Navigates to the specified page.
   *          <p>
   *          Valid range: 0 .. {@link #getTotalPageNum()}-1.
   */
  void setPageIdx(long pageIdx);

  /**
   * Provides the total number of objects within this set. A filter definition may influence this number.<br/>
   * If a query is called to get the number of items a {@link MaxQueryResultsViolationException} 
   * could be thrown, if the number of found items is lager than {@link QueryParams#getMaxResults()}. 
   *
   * @return the number of filtered items.
   */
  long getNumOfItems();

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
   * Clears all cached information.<br>
   * Gets called if everything needs to be re-evalueated.
   */
  void clearCaches();

  /**
   * Provides a handler for item selection.
   *
   * @return the (collection type specific) selection handler.
   */
  SelectionHandler<T_ITEM> getSelectionHandler();

  /**
   * Provides the current selection.
   * <p>
   * Is a short cut method for <code>getSelectionHandler().getSelection()</code>.
   *
   * @return the current selection state. Never <code>null</code>.
   */
  Selection<T_ITEM> getSelection();

  /**
   * Provides a handler that can handle collection modifications (add- and delete item operations).
   * <p>
   * Returns a <code>null</code> if the collection does not support modifications.
   *
   * @return the handler or <code>null</code>.
   */
  ModificationHandler<T_ITEM> getModificationHandler();

  /**
   * Provides the registered modifications.
   * <p>
   * Is a short cut method for <code>getModificationHandler().getModifications()</code>.
   *
   * @return the modification set. Never <code>null</code>.
   */
  Modifications<T_ITEM> getModifications();

}
