package org.pm4j.core.pm.pageable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.pm4j.core.pm.filter.Filter;

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
public interface PageableCollection<T_ITEM> {

  /**
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
   * Sorts the items of this collection based on the given operator.
   *
   * @param sortComparator
   *          The item comparator to use.<br>
   *          May be <code>null</code> to switch sorting off.
   */
  void sortItems(Comparator<?> sortComparator);

  /**
   * Provides a sort comparator that will be applied to the items of the backing
   * collection.
   * <p>
   * In case of a simple collection (without backing collection), the comparator
   * will be applied to the items of this collection.
   *
   * @param sortComparator
   *          The item comparator to use.<br>
   *          May be <code>null</code> to switch sorting off.
   */
  void sortBackingItems(Comparator<?> sortComparator);

  /**
   * Defines the (optional) comparator used to sort the items initially by.
   * <p>
   * ATTENTION: Gets currently only delegated to a {@link PageableListImpl}.
   *
   * @param comparator A comparator.
   */
  void setInitialBeanSortComparator(Comparator<?> comparator);

  /**
   * Provides a filter that will be applied to the items of this
   * collection.
   *
   * @param filter
   *          The item filter to use.<br>
   *          May be <code>null</code> to switch filtering off.
   */
  void setItemFilter(Filter filter);

  /**
   * Provides a filter that will be applied to the items of the backing
   * collection.
   * <p>
   * In case of a simple collection (without backing collection), the filter
   * will be applied to the items of this collection.
   *
   * @param filter
   *          The item filter to use.<br>
   *          May be <code>null</code> to switch filtering off.
   */
//  void setBackingItemFilter(Filter filter);

  /**
   * @return The current filter definition for the backing collection.
   */
//  Filter getBackingItemFilter();

  /**
   * @return The total number of objects within this set.
   *         <p>
   *         A filter definition may influence this number.
   */
  int getNumOfItems();

  /**
   * @return The total number of object within this set.
   *         <p>
   *         A filter definition does <b>not</b> influence this number.
   */
  int getNumOfUnfilteredItems();

  /**
   * Provides an iterator over the item set.<br>
   * The current filter and sort order settings are considered.
   * <p>
   * ATTENTION: In case of large collections with page wise data loading the the
   * provided iterator has some performance impact.
   *
   * @return An iterator over all items that are visible according to the
   *         current filter settings.
   */
  Iterator<T_ITEM> getAllItemsIterator();

  /**
   * @param item
   *          The item to check.
   * @return <code>true</code> if the item was selected.
   */
  boolean isSelected(T_ITEM item);

  /**
   * Adds the given item to the set of selected items.
   * <p>
   * Has no effect if the item is already selected.
   *
   * @param item
   *          The item to select.
   */
  void select(T_ITEM item, boolean select);
  @Deprecated
  void select(T_ITEM item);
  @Deprecated
  void deSelect(T_ITEM item);


  /**
   * Removes the given item from the set of selected items.
   * <p>
   * Has no effect if the item was not selected.
   *
   * @param item
   *          The item to deselect.
   */
//  void deSelect(T_ITEM item);

  /**
   * @return <code>true</code> if more than one item can be selected.
   */
  boolean isMultiSelect();

  /**
   * Defines the selection behavior.
   *
   * @param isMultiSelect
   */
  void setMultiSelect(boolean isMultiSelect);

  /**
   * @return The set of selected items.
   */
  Collection<T_ITEM> getSelectedItems();

  /**
   * Gets called whenever the collection behind this instance was updated.
   */
  // TODO olaf: special position handling for add/remove etc. needed?
  void onUpdateCollection();

}
