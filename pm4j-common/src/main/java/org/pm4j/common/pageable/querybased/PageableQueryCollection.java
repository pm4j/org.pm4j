package org.pm4j.common.pageable.querybased;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.util.collection.ListUtil;

/**
 * A {@link PageableCollection2} that uses a {@link PageableQueryService} and {@link QueryParams}
 * to get the data to provide.
 *
 * @param <T_ITEM> type of handled items.
 * @param <T_ID> the item identifier type.
 *
 * @author olaf boede
 */
public class PageableQueryCollection<T_ITEM, T_ID extends Serializable> extends PageableCollectionBase2<T_ITEM> {

  private final PageableQueryService<T_ITEM, T_ID> service;
  private long                                     itemCountCache                           = -1;
  private long                                     unfilteredItemCountCache                 = -1;
  private List<T_ITEM>                             pageItemsCache;
  private SelectionHandler<T_ITEM>                 selectionHandler;

  /**
   * Creates a sercie based collection without query restrictions.
   *
   * @param service
   *          the service used to get the data.
   */
  public PageableQueryCollection(PageableQueryService<T_ITEM, T_ID> service) {
    this(service, null);
  }

  /**
   * @param service
   *          the service used to get the data.
   * @param queryParams
   *          the set of query parameters that provides data restrictions and
   *          sort order.<br>
   *          May be <code>null</code> if there are no query restrictions.
   */
  public PageableQueryCollection(PageableQueryService<T_ITEM, T_ID> service, QueryParams queryParams) {
    super(service.getQueryOptions(), queryParams);

    this.service = service;
    this.selectionHandler = new PageableQuerySelectionHandler<T_ITEM, T_ID>(service, getQueryParams());

    // Uses getQueryParams() because the super ctor may have created it.
    QueryParams myQueryParams = getQueryParams();

    // Reset all caches on each query filter criteria change.
    myQueryParams.addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        clearCaches();
      }
    });
    // In addition: reset the page item cache on sort order change.
    myQueryParams.addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        pageItemsCache = null;
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_ITEM> getItemsOnPage() {
    if (pageItemsCache == null) {
      long startIdx = PageableCollectionUtil2.getIdxOfFirstItemOnPage(this)-1;
      QueryParams queryParams = getQueryParams();
      pageItemsCache = (startIdx > -1 && queryParams.isExecQuery())
      		? service.getItems(getQueryParams(), startIdx, getPageSize())
      		: Collections.EMPTY_LIST;
    }
    return pageItemsCache;
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    if (pageIdx != getCurrentPageIdx()) {
      pageItemsCache = null;
    }
    super.setCurrentPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return (itemCountCache != -1)
        ? itemCountCache
        : (itemCountCache = getQueryParams().isExecQuery()
          ? service.getItemCount(getQueryParams())
          : 0);
  }

  @Override
  public long getUnfilteredItemCount() {
    return (unfilteredItemCountCache != -1)
        ? unfilteredItemCountCache
        : (unfilteredItemCountCache = getQueryParams().isExecQuery()
          ? service.getUnfilteredItemCount(getQueryParams())
          : 0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<T_ITEM> iterator() {
    return !getQueryParams().isExecQuery()
        ? Collections.EMPTY_LIST.iterator()
        : new Iterator<T_ITEM>() {
      int currentIdx;
      T_ITEM next;

      {
        readNext();
      }

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public T_ITEM next() {
        T_ITEM rval = next;
        readNext();
        return rval;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      protected boolean readNext() {
        // TODO olaf: Needs to be optimized. Blockwise ...
        next = ListUtil.listToItemOrNull(service.getItems(getQueryParams(), currentIdx++, 1));
        return next != null;
      }
    };
  }

  @Override
  public SelectionHandler<T_ITEM> getSelectionHandler() {
    return selectionHandler;
  }

  @Override
  public void clearCaches() {
    itemCountCache = -1;
    unfilteredItemCountCache = -1;
    pageItemsCache = null;
  }

  @Override
  public void addItem(T_ITEM item) {
    throw new UnsupportedOperationException();
  }
}
