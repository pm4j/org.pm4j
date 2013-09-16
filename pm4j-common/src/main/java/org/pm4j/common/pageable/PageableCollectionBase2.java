package org.pm4j.common.pageable;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;


/**
 * Some common basic implementation code for {@link PageableCollection2}.
 *
 * @param <T_ITEM> type of handled collection items.
 *
 * @author olaf boede
 */
public abstract class PageableCollectionBase2<T_ITEM> extends PropertyChangeSupportedBase implements PageableCollection2<T_ITEM> {

  private int                pageSize = 10;
  private long               pageIdx;
  private final QueryParams  queryParams;
  private final QueryOptions queryOptions;


  /**
   * @param queryOptions
   *          the set of sort order and filter restrictions that can be adjusted by the user.
   */
  public PageableCollectionBase2(QueryOptions queryOptions) {
    this.queryOptions = (queryOptions != null)
        ? queryOptions
        : new QueryOptions();
    this.queryParams = new QueryParams(this.queryOptions.getDefaultSortOrder());
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
    int oldSize = this.pageSize;
    pageSize = newSize;
    firePropertyChange(PROP_PAGE_SIZE, oldSize, newSize);
  }

  @Override
  public long getPageIdx() {
    return pageIdx;
  }

  @Override
  public void setPageIdx(long pageIdx) {
    long oldIdx = this.pageIdx;
    this.pageIdx = Math.max(0, pageIdx);
    firePropertyChange(PROP_PAGE_IDX, oldIdx, this.pageIdx);
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
