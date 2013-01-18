package org.pm4j.common.pageable;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;


/**
 * Some common basic implementation code for {@link PageableCollection2}.
 *
 * @param <T_ITEM> type of handled collection items.
 *
 * @author olaf boede
 */
public abstract class PageableCollectionBase2<T_ITEM> implements PageableCollection2<T_ITEM> {

  private int                pageSize = 10;
  private long               pageIdx;
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
    this.pageIdx = 0;
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
  public final int getCurrentPageIdx() {
    return (int)pageIdx+1;
  }
  @Override
  public long getPageIdx() {
    return pageIdx;
  }

  @Override
  public final void setCurrentPageIdx(int pageIdx) {
    setPageIdx(pageIdx-1);
  }
  @Override
  public void setPageIdx(long pageIdx) {
    this.pageIdx = (pageIdx < 0) ? 0 : pageIdx;
  }

  @Override
  public Selection<T_ITEM> getSelection() {
    return getSelectionHandler().getSelection();
  }

  @Override
  public Modifications<T_ITEM> getModifications() {
    ModificationHandler<T_ITEM> mh = getModificationHandler();
    return mh != null
        ? mh.getModifications()
        : new ModificationsImpl<T_ITEM>();
  }

}
