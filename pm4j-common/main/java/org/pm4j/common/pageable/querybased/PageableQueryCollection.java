package org.pm4j.common.pageable.querybased;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.util.collection.ListUtil;

public class PageableQueryCollection<T_ITEM, T_ID extends Serializable> extends PageableCollectionBase2<T_ITEM> {

  private final PageableQueryService<T_ITEM, T_ID> service;
  private long                                     itemCount                           = -1;
  private long                                     unfilteredItemCount                 = -1;
  private SelectionHandler<T_ITEM>                 selectionHandler;

  private PropertyChangeListener                   resetItemCountOnQueryChangeListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      itemCount = -1;
      unfilteredItemCount = -1;
    }
  };

  public PageableQueryCollection(PageableQueryService<T_ITEM, T_ID> service) {
    this(service, null);
  }

  public PageableQueryCollection(PageableQueryService<T_ITEM, T_ID> service, QueryParams query) {
    super(service.getQueryOptions(), query);

    this.service = service;
    this.selectionHandler = new PageableQuerySelectionHandler<T_ITEM, T_ID>(service, getQueryParams());

    // uses getQuery() because the super ctor may have created it.
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, resetItemCountOnQueryChangeListener);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_ITEM> getItemsOnPage() {
    long startIdx = PageableCollectionUtil2.getIdxOfFirstItemOnPage(this)-1;
    QueryParams queryParams = getQueryParams();
    return (startIdx > -1 && queryParams.isExecQuery())
    		? service.getItems(getQueryParams(), startIdx, getPageSize())
    		: Collections.EMPTY_LIST;
  }

  @Override
  public long getNumOfItems() {
    return (itemCount != -1)
        ? itemCount
        : (itemCount = getQueryParams().isExecQuery()
          ? service.getItemCount(getQueryParams())
          : 0);
  }

  @Override
  public long getUnfilteredItemCount() {
    return (unfilteredItemCount != -1)
        ? unfilteredItemCount
        : (unfilteredItemCount = getQueryParams().isExecQuery()
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
    itemCount = -1;
    unfilteredItemCount = -1;
  }

}
