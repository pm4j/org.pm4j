package org.pm4j.core.pm.pageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

  private Collection<T_ITEM> originalObjects;
  private List<T_ITEM>       objectsInOriginalSortOrder;
  private List<T_ITEM>       objects;
  private int                pageSize = 10;
  private int                currentPageIdx;
  private boolean            multiSelect;
  private Set<T_ITEM>        selectedItems = new LinkedHashSet<T_ITEM>();
  private Comparator<?>      currentSortComparator;
  private Filter<T_ITEM>     currentFilter;

  /**
   * @param objects
   *          The set of objects to iterate over.
   * @param pageSize
   *          The page size to use.
   */
  public PageableListImpl(Collection<T_ITEM> objects) {
    this.originalObjects = objects;
    this.currentPageIdx = 1;

    onUpdateCollection();
  }

  /**
   * Creates an empty collection.
   * @param pageSize The page size.
   * @param multiSelect The multi selection behavior definition.
   */
  public PageableListImpl(int pageSize, boolean multiSelect) {
    this(new ArrayList<T_ITEM>());
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

  @Override
  public void sortBackingItems(Comparator<?> sortComparator) {
    sortItems(sortComparator);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void filterItems(Filter<?> filter) {
    currentFilter = (Filter<T_ITEM>)filter;
    _applyFilterAndSortOrder();
  }

  @Override
  public void filterBackingItems(Filter<?> filter) {
    filterItems(filter);
  }

  @Override
  public boolean isSelected(T_ITEM item) {
    return selectedItems.contains(item);
  }

  @Override
  public void select(T_ITEM item) {
    if (!multiSelect) {
      selectedItems.clear();
    }

    if (item != null) {
      selectedItems.add(item);
    }
  }

  @Override
  public void deSelect(T_ITEM item) {
    selectedItems.remove(item);
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

  @SuppressWarnings("unchecked")
  private void _assignObjectsFromOri() {
    this.objectsInOriginalSortOrder = (originalObjects == null || originalObjects.isEmpty())
        ? java.util.Collections.EMPTY_LIST
        : new ArrayList<T_ITEM>(originalObjects);
    this.objects = objectsInOriginalSortOrder;
  }

  private void _applyFilterAndSortOrder() {
    List<T_ITEM> filteredList = _filter(objectsInOriginalSortOrder);
    _sortAndAssignToObjects(filteredList);
  }

  @SuppressWarnings("unchecked")
  private void _sortAndAssignToObjects(List<T_ITEM> srcList) {
    if (currentSortComparator == null) {
      objects = srcList;
    } else {
      // prevent unintended sort operation on the source list.
      if (objects == srcList) {
        objects = new ArrayList<T_ITEM>(srcList);
      }
      Collections.sort(objects, (Comparator<T_ITEM>)currentSortComparator);
    }
  }

  private List<T_ITEM> _filter(List<T_ITEM> unfilteredList) {
    if (currentFilter == null) {
      return unfilteredList;
    }

    List<T_ITEM> filteredList = new ArrayList<T_ITEM>();
    int listSize = unfilteredList.size();
    for (int i=0; i<listSize; ++i) {
      T_ITEM item = unfilteredList.get(i);
      if (currentFilter.doesItemMatch(item)) {
        filteredList.add(item);
      }
    }
    return filteredList;
  }

}
