package org.pm4j.common.pageable;

import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.QueryImpl;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryOptionsImpl;


public abstract class PageableCollectionBase2<T_ITEM> implements PageableCollection2<T_ITEM> {

  private int                pageSize = 10;
  private int                currentPageIdx;
  private final QueryParams        query;
  private final QueryOptions queryOptions;


  /**
   * @param query
   *          defines the current sort order and filter restricions.
   */
  public PageableCollectionBase2(QueryOptions queryOptions, QueryParams query) {
    this.queryOptions = (queryOptions != null)
        ? queryOptions
        : new QueryOptionsImpl();
    this.query = (query != null)
            ? query
            : new QueryImpl(this.queryOptions.getDefaultSortOrder());
    this.currentPageIdx = 1;
  }

  @Override
  public final QueryParams getQuery() {
    return query;
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

}
