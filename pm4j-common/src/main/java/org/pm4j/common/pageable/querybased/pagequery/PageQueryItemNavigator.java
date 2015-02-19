package org.pm4j.common.pageable.querybased.pagequery;

import org.pm4j.common.itemnavigator.ItemNavigator;
import org.pm4j.common.query.QueryParams;

/**
 * Navigates over items of a {@link PageQueryService} using {@link QueryParams} restrictions.
 *
 * @param <T> type of the items to iterate.
 *
 * @author olaf boede
 */
public class PageQueryItemNavigator<T> implements ItemNavigator<T> {

  /** Current navigator position. RecordNavigatorPageableQueryBased */
  private int currentItemIdx = 0;

  private PageQueryCollectionCache<T> pagedCollectionCache;

  /**
   * @param pageableService
   *          the service used to get the items from.
   * @param selectedItemsQueryParams
   *          the query parameters that may restrict the item set. <br>
   *          May be <code>null</code>.
   */
  public PageQueryItemNavigator(PageQueryService<T, ?> pageableService, QueryParams selectedItemsQueryParams) {
    setPageableCollection(pageableService, selectedItemsQueryParams);
  }

  /**
   * @param pageableService
   *          the service used to get the items from.
   * @param selectedItemsQueryParams
   *          the query parameters that may restrict the item set. <br>
   *          May be <code>null</code>.
   */
  public void setPageableCollection(PageQueryService<T, ?> pageableService, QueryParams selectedItemsQueryParams) {
    this.pagedCollectionCache = (pageableService != null) ? new PageQueryCollectionCache<T>(pageableService,
        selectedItemsQueryParams, 1) : null;
  }

  @Override
  public T getCurrentItem() {
    return pagedCollectionCache != null ? pagedCollectionCache.getAt(currentItemIdx) : null;
  }

  @Override
  public T navigateTo(int itemPos) {
    currentItemIdx = Math.min(itemPos, getNumOfItems());
    currentItemIdx = Math.max(currentItemIdx, 0);

    return getCurrentItem();
  }

  @Override
  public int getNumOfItems() {
    return pagedCollectionCache != null ? (int) pagedCollectionCache.getCollectionSize() : 0;
  }

  @Override
  public int getCurrentItemIdx() {
    return currentItemIdx;
  }

  @Override
  public void clearCaches() {
    if (pagedCollectionCache != null) {
      pagedCollectionCache.clear();
    }
  }
}
