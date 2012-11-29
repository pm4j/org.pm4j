package org.pm4j.common.pageable;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;


/**
 * Some common basic implementation code for {@link PageableCollection2}.
 *
 * @param <T_ITEM> type of handled collection items.
 *
 * @author olaf boede
 */
public abstract class PageableCollectionBase2<T_ITEM> implements PageableCollection2<T_ITEM> {

  private int                pageSize = 10;
  private int                currentPageIdx;
  private final QueryParams  queryParams;
  private final QueryOptions queryOptions;


  /**
   * @param queryOptions
   *          the set of sort order and filter restricions that can be adjusted by the user.
   * @param queryParams
   *          defines the current sort order and filter restricions.
   */
  public PageableCollectionBase2(QueryOptions queryOptions, QueryParams queryParams) {
    this.queryOptions = (queryOptions != null)
        ? queryOptions
        : new QueryOptions();
    this.queryParams = (queryParams != null)
            ? queryParams
            : new QueryParams(this.queryOptions.getDefaultSortOrder());
    this.currentPageIdx = 1;
  }

  @Override
  public final QueryParams getQueryParams() {
    return queryParams;
  }

  @Override
  public QueryOptions getQueryOptions() {
    return queryOptions;
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
    this.currentPageIdx = (pageIdx < 1) ? 1 : pageIdx;
  }

  /**
   * The default implementation just passes the result of
   * {@link #getSelectionHandler()}.
   * <p>
   * Only implementations that handle PM's in front of bean items will provide
   * here a different handler.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> SelectionHandler<T> getBeanSelectionHandler() {
    return (SelectionHandler<T>)getSelectionHandler();
  }

}
