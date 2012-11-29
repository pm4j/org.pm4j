package org.pm4j.common.pageable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerWithAdditionalItems;
import org.pm4j.common.util.collection.CombinedIterator;

/**
 * A pageable collection that combines items provided by a backing base
 * {@link PageableCollection2} with a list of transient items.
 *
 * @param <T_ITEM>
 *          the type of handled items.
 *
 * @author olaf boede
 */
public class PageableCollectionWithAdditionalItems<T_ITEM> implements PageableCollection2<T_ITEM> {

  private final PageableCollection2<T_ITEM>  baseCollection;
  private final List<T_ITEM>                 additionalItems;
  private SelectionHandlerWithAdditionalItems<T_ITEM> selectionHandler;

  /**
   * @param baseCollection the handled set of (persistent) items.
   * @param itemToBeanConverter a converter that allows to convert the items to the corresponding
   */
  public PageableCollectionWithAdditionalItems(PageableCollection2<T_ITEM> baseCollection) {
    assert baseCollection != null;

    this.baseCollection = baseCollection;
    this.additionalItems   = new ArrayList<T_ITEM>();
    this.selectionHandler = new SelectionHandlerWithAdditionalItems<T_ITEM>(baseCollection, additionalItems);
  }

  /**
   * Adds a transient item to handle.
   *
   * @param items
   *          the new item.
   */
  public void addAdditionalItem(T_ITEM item) {
    additionalItems.add(item);
  }

  /**
   * Removes the given transient item.
   *
   * @param items
   *          the transient item to delete.
   */
  public void removeAdditionalItem(T_ITEM item) {
    additionalItems.remove(item);
  }

  /**
   * Provides the set of all transient items.
   *
   * @return the transient item set.
   */
  public List<T_ITEM> getAdditionalItems() {
    return additionalItems;
  }

  /**
   * Clears all transient items.
   */
  public void clearAdditionalItems() {
    additionalItems.clear();
  }

  /**
   * Provides the base collection without the set of additional items.
   *
   * @return the base collection.
   */
  public PageableCollection2<T_ITEM> getBaseCollection() {
    return baseCollection;
  }

  @Override
  public QueryParams getQueryParams() {
    return baseCollection.getQueryParams();
  }

  @Override
  public QueryOptions getQueryOptions() {
    return baseCollection.getQueryOptions();
  }

  @Override
  public List<T_ITEM> getItemsOnPage() {
    if (additionalItems.isEmpty()) {
      return baseCollection.getItemsOnPage();
    } else {
      List<T_ITEM> list = new ArrayList<T_ITEM>(baseCollection.getItemsOnPage());
      list.addAll(additionalItems);
      return list;
    }
  }

  @Override
  public int getPageSize() {
    return baseCollection.getPageSize();
  }

  @Override
  public void setPageSize(int newSize) {
    baseCollection.setPageSize(newSize);
  }

  @Override
  public int getCurrentPageIdx() {
    return baseCollection.getCurrentPageIdx();
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    baseCollection.setCurrentPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return baseCollection.getNumOfItems() + additionalItems.size();
  }

  @Override
  public long getUnfilteredItemCount() {
    return baseCollection.getUnfilteredItemCount() + additionalItems.size();
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new CombinedIterator<T_ITEM>(baseCollection.iterator(), additionalItems.iterator());
  }

  @Override
  public SelectionHandler<T_ITEM> getSelectionHandler() {
    return selectionHandler;
  }

  /**
   * Returns this instance.
   * Sub classes that support PM's in front of beans should provide an alternate implementation.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> SelectionHandler<T> getBeanSelectionHandler() {
    return (SelectionHandler<T>) this;
  }

  @Override
  public void clearCaches() {
    baseCollection.clearCaches();
  }

}
