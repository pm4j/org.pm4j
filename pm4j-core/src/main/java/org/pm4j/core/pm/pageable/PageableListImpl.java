package org.pm4j.core.pm.pageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.filter.Filter;

/**
 * Implements a {@link PageableCollection} based on an {@link List} of items to
 * handle.
 *
 * @author olaf boede
 *
 * @param <T_ITEM>
 *          The type of items handled by this set.
 */
public class PageableListImpl<T_ITEM> implements PageableCollection<T_ITEM> {

  /** Contains the not filtered set of items in their original sort order. */
  private Collection<T_ITEM> originalObjects;
  /** The filtered set of all items sorted by {@link #initialSortComparator}. */
  private List<T_ITEM>       allObjectsInInitialSortOrder;
  /** The current set of filtered and sorted items. */
  private List<T_ITEM>       objects;
  private int                pageSize = 10;
  private int                currentPageIdx;
  private boolean            multiSelect;
  private Set<T_ITEM>        selectedItems = new LinkedHashSet<T_ITEM>();
  private Comparator<T_ITEM> initialSortComparator;
  private Comparator<?>      currentSortComparator;
  private Filter             currentFilter;

  /**
   * @param objects
   *          The set of objects to iterate over.
   * @param initialSortComparator
   *          The initial sort order to apply. May be <code>null</code>.
   */
  public PageableListImpl(Collection<T_ITEM> objects, Comparator<T_ITEM> initialSortComparator) {
    this.originalObjects = objects;
    this.initialSortComparator = initialSortComparator;
    this.currentPageIdx = 1;

    // TODO olaf: add another switch to enable such expensive checks:
    if (objects != null && PmDefaults.getInstance().debugHints) {
      Set<T_ITEM> checkSet = new HashSet<T_ITEM>(objects);
      if (checkSet.size() != objects.size()) {
        throw new PmRuntimeException("Some instances of the pageable list are not unique!");
      }
    }

    onUpdateCollection();
  }

  /**
   * Creates a collection without a special initial sort order definition.
   *
   * @param objects
   *          The set of objects to iterate over.
   */
  public PageableListImpl(Collection<T_ITEM> objects) {
    this(objects, null);
  }

  /**
   * Creates an empty collection.
   * @param pageSize The page size.
   * @param multiSelect The multi selection behavior definition.
   */
  @SuppressWarnings("unchecked")
  public PageableListImpl(int pageSize, boolean multiSelect) {
    this(Collections.EMPTY_LIST, null);
    this.pageSize = pageSize;
    this.multiSelect = multiSelect;
  }


  @Override
  public List<T_ITEM> getItemsOnPage() {
    if (objects.isEmpty()) {
      return Collections.emptyList();
    }

    int first = PageableCollectionUtil.getIdxOfFirstItemOnPage(this) - 1;
    int last = PageableCollectionUtil.getIdxOfLastItemOnPage(this);

    if (first < 0)
      throw new RuntimeException();

    return objects.subList(first, last);
  }

  @Override
  public int getNumOfItems() {
    return objects.size();
  }

  @Override
  public int getNumOfUnfilteredItems() {
    return originalObjects != null ? originalObjects.size() : 0;
  }

  @Override
  public Iterator<T_ITEM> getAllItemsIterator() {
    return objects.iterator();
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public void setPageSize(int newSize) {
    pageSize = newSize;
  }

  @Override
  public int getCurrentPageIdx() {
    return currentPageIdx;
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    this.currentPageIdx = pageIdx;
  }


  @Override
  public void sortItems(Comparator<?> sortComparator) {
    currentSortComparator = sortComparator;
    _applyFilterAndSortOrder();
  }

  /**
   * Assigns an initial sort order.
   * <p>
   * If there are already items, they get re-arranged immediately. But only if
   * there is not active {@link #currentSortComparator}.
   *
   * @param initialSortComparator
   *          The new sort comparator. May be <code>null</code> to get rid of
   *          the current {@link #initialSortComparator}.
   */
  @SuppressWarnings("unchecked")
  @Override
  public void setInitialBeanSortComparator(Comparator<?> initialSortComparator) {
    if (this.initialSortComparator == initialSortComparator) {
      return;
    }

    this.initialSortComparator = (Comparator<T_ITEM>) initialSortComparator;
    _assignAllObjectsInInitialSortOrderFromOri();

    // If there is no other active sort order definition, the new default needs to
    // be applied immediately to the active list of item.
    if (currentSortComparator == null) {
      _applyFilterAndSortOrder();
    }
  }

  @Override
  public void setItemFilter(Filter filter) {
    currentFilter = filter;
    _applyFilterAndSortOrder();
  }

  @Override
  public boolean isSelected(T_ITEM item) {
    return selectedItems.contains(item);
  }

  @Override
  public void select(T_ITEM item, boolean doSelect) {
    if (doSelect) {
      if (!multiSelect) {
        selectedItems.clear();
      }

      if (item != null) {
        selectedItems.add(item);
      }
    }
    else {
      selectedItems.remove(item);
    }
  }

  @Override
  public boolean isMultiSelect() {
    return multiSelect;
  }

  @Override
  public void setMultiSelect(boolean isMultiSelect) {
    this.multiSelect = isMultiSelect;
  }

  @Override
  public Collection<T_ITEM> getSelectedItems() {
    return selectedItems;
  }

  @Override
  public void onUpdateCollection() {
    _assignObjectsFromOri();
    _applyFilterAndSortOrder();
  }

  private void _assignObjectsFromOri() {
    _assignAllObjectsInInitialSortOrderFromOri();
    this.objects = allObjectsInInitialSortOrder;
  }

  @SuppressWarnings("unchecked")
  private void _assignAllObjectsInInitialSortOrderFromOri() {
    this.allObjectsInInitialSortOrder = (originalObjects == null || originalObjects.isEmpty())
        ? java.util.Collections.EMPTY_LIST
        : new ArrayList<T_ITEM>(originalObjects);
    if (initialSortComparator != null) {
      Collections.sort(allObjectsInInitialSortOrder, initialSortComparator);
    }
  }

  private void _applyFilterAndSortOrder() {
    List<T_ITEM> filteredList = _filter(allObjectsInInitialSortOrder);
    _sortAndAssignToObjects(filteredList);

    // XXX olaf: just moves to the last possible page if necessary.
    // The user may want to stay on the page with his selected item.
    // We need to define strategies for application specific definitions.
    if (currentPageIdx * pageSize >= filteredList.size()) {
      currentPageIdx = PageableCollectionUtil.getNumOfPages(this);
    }

    // TODO olaf: if the filter gets changed to find items again, we need to do this to prevent
    //            negative page item index identification...
    // Check if it was really a good idea to have a 0-page index for empty collections.
    if (objects.size() > 0 && currentPageIdx == 0) {
      currentPageIdx = 1;
    }
  }

  @SuppressWarnings("unchecked")
  private void _sortAndAssignToObjects(List<T_ITEM> srcList) {
    if (currentSortComparator == null) {
      objects = srcList;
    } else {
      // prevent unintended sort operation on the source list.
      objects = new ArrayList<T_ITEM>(srcList);
      Collections.sort(objects, (Comparator<T_ITEM>)currentSortComparator);
    }
  }

  /** Generates a list of filtered items based on the given list. */
  private List<T_ITEM> _filter(List<T_ITEM> unfilteredList) {
    if (currentFilter == null) {
      return unfilteredList;
    }

    // remember selected item and clear selection if not multiSelect
    T_ITEM selectedItem = null;
    if(!multiSelect) {
      if(!selectedItems.isEmpty()) {
        selectedItem = selectedItems.iterator().next();
        selectedItems.clear();
      }
    }
    List<T_ITEM> filteredList = new ArrayList<T_ITEM>();
    int listSize = unfilteredList.size();
    for (int i=0; i<listSize; ++i) {
      T_ITEM item = unfilteredList.get(i);
      if (currentFilter.doesItemMatch(item)) {
        filteredList.add(item);
        // re-select item if not multiSelect and item is within filtered items
        if(!multiSelect) {
          if(item == selectedItem) {
            select(selectedItem, true);
          }
        }
      }
    }

    return filteredList;
  }


}
