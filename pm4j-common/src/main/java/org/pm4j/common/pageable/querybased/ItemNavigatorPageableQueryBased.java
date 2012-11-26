package org.pm4j.common.pageable.querybased;

import org.pm4j.common.pageable.ItemNavigator;
import org.pm4j.common.query.QueryParams;

/**
 * Navigates over items of a {@link PageableQueryService} using {@link QueryParams} restrictions.
 *
 * @param <T> type of the items to iterate.
 *
 * @author olaf boede
 */
public class ItemNavigatorPageableQueryBased<T> implements ItemNavigator<T> {

  /** Current navigator position. RecordNavigatorPageableQueryBased */
  private int positionIdx = 0;

  private PageableQueryCollectionCache<T> pagedCollectionCache;

  /**
   * @param pageableService
   *          the service used to get the items from.
   * @param selectedItemsQueryParams
   *          the query parameters that may restrict the item set. <br>
   *          May be <code>null</code>.
   */
  public ItemNavigatorPageableQueryBased(PageableQueryService<T, ?> pageableService, QueryParams selectedItemsQueryParams) {
    setPageableCollection(pageableService, selectedItemsQueryParams);
  }

  /**
   * @param pageableService
   *          the service used to get the items from.
   * @param selectedItemsQueryParams
   *          the query parameters that may restrict the item set. <br>
   *          May be <code>null</code>.
   */
  public void setPageableCollection(PageableQueryService<T, ?> pageableService, QueryParams selectedItemsQueryParams) {
    this.pagedCollectionCache = (pageableService != null) ? new PageableQueryCollectionCache<T>(pageableService,
        selectedItemsQueryParams, 1) : null;
  }

  @Override
  public T getCurrentItem() {
    return pagedCollectionCache != null ? pagedCollectionCache.getAt(positionIdx) : null;
  }

  @Override
  public T navigateTo(int itemPos) {
    positionIdx = Math.min(itemPos, getNumOfItems());
    positionIdx = Math.max(positionIdx, 0);

    return getCurrentItem();
  }

  @Override
  public int getNumOfItems() {
    return (int) pagedCollectionCache.getQueryResultSetSize();
  }

  @Override
  public int getCurrentItemIdx() {
    return positionIdx;
  }

}
