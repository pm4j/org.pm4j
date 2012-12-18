package org.pm4j.common.pageable.idservicebased;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerWithIdSet;
import org.pm4j.common.util.collection.ListUtil;

public class PageableIdCollectionImpl<T_ITEM, T_ID> extends PageableCollectionBase2<T_ITEM> {

  private final PageableIdService<T_ITEM, T_ID> service;
  /** The collection type specific selection handler. */
  private final SelectionHandler<T_ITEM>  selectionHandler;
  private List<T_ID>                      ids;
  private List<T_ITEM>                    currentPageItems;
  private long                            unfilteredItemCount = -1;

  private PropertyChangeListener          resetItemsOnQueryChangeListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      clearCaches();
    }
  };

  public PageableIdCollectionImpl(PageableIdService<T_ITEM, T_ID> service, QueryParams query) {
    super(null, query);
    assert service != null;

    this.service = service;
    this.selectionHandler = new SelectionHandlerWithIdSet<T_ITEM, T_ID>(service) {
      @Override
      protected Collection<T_ID> getAllIds() {
        return getIds();
      }
    };

    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, resetItemsOnQueryChangeListener);
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, resetItemsOnQueryChangeListener);
  }

  @Override
  public List<T_ITEM> getItemsOnPage() {
    if (currentPageItems != null) {
      return currentPageItems;
    }

    // prevent parallel service calls for the same thing.
    synchronized (this) {
      if (currentPageItems == null) {
        List<T_ID> pageIds = getCurrentPageIds();
        if (pageIds.isEmpty()) {
          currentPageItems = Collections.emptyList();
          return currentPageItems;
        }

        currentPageItems = service.getItems(pageIds);
      }
    }

    return currentPageItems;
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    if (pageIdx != getCurrentPageIdx()) {
      currentPageItems = null;
    }
    super.setCurrentPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return getIds().size();
  }

  @Override
  public long getUnfilteredItemCount() {
    if (unfilteredItemCount == -1) {
      unfilteredItemCount = (getQueryParams().getFilterExpression() == null)
          ? getNumOfItems()
          : service.getUnfilteredItemCount(getQueryParams());
    }
    return unfilteredItemCount;
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new ItemIterator();
  }

  @Override
  public SelectionHandler<T_ITEM> getSelectionHandler() {
    return selectionHandler;
  }

  @Override
  public void clearCaches() {
    ids = null;
    currentPageItems = null;
    unfilteredItemCount = -1;
  }

  @Override
  public void addItem(T_ITEM item) {
    throw new UnsupportedOperationException();
  }

  protected List<T_ID> getCurrentPageIds() {
    int first = (int)PageableCollectionUtil2.getIdxOfFirstItemOnPage(this) - 1;
    long last = PageableCollectionUtil2.getIdxOfLastItemOnPage(this);

    if (first < 0) {
      throw new RuntimeException();
    }
    if (last > Integer.MAX_VALUE) {
      throw new RuntimeException("Id collection based implementation is limites to 2exp32 items. Requested last item index: " + last);
    }

    List<T_ID> ids = getIds();

    // handle collection size changes
    // TODO olaf: not yet complete.
    int subListFirst = Math.min(first, ids.size());
    int subListLast = Math.min((int)last, ids.size());

    return ids.subList(subListFirst, subListLast);
  }

  /**
   * Provides all matching IDs using a lazy loading mechanism.
   *
   * @return the ID set according to the current query configuration.
   */
  @SuppressWarnings("unchecked")
  protected List<T_ID> getIds() {
    if (ids != null) {
      return ids;
    }

    // prevent parallel service calls for the same thing.
    synchronized (this) {
      if (ids == null) {
        currentPageItems = null;
        QueryParams queryParams = getQueryParams();
        ids = queryParams.isExecQuery()
            ? service.findIds(queryParams)
            // In no-exec case: an unmodifyable collection.
            : Collections.EMPTY_LIST;
      }
      return ids;
    }
  }

  class IdIterator implements Iterator<T_ID> {
    private int nextIdx;
    private final List<T_ID> ids = getIds();

    @Override
    public boolean hasNext() {
      return nextIdx < ids.size();
    }

    @Override
    public T_ID next() {
      ++nextIdx;
      return ids.get(nextIdx);
    }

    @Override
    public void remove() {
      throw new RuntimeException("Remove is not supported.");
    }
  };

  /**
   * A simple iterator that iterates slowly over all items using a lot of service calls.
   *
   * @author olaf boede
   */
  class ItemIterator implements Iterator<T_ITEM> {
    private int nextIdx = -1;
    private final List<T_ID> ids = getIds();

    @Override
    public boolean hasNext() {
      return nextIdx < ids.size();
    }

    protected void throwMissingItemException(T_ID idOfItem) {
      throw new RuntimeException("Item for id '" + nextIdx + "' not found.");
    }

    @Override
    public T_ITEM next() {
      T_ID id = ids.get(nextIdx);
      @SuppressWarnings("unchecked")
      // TODO olaf: not optimized:
      T_ITEM item = ListUtil.listToItemOrNull(service.getItems(Arrays.asList(id)));
      if (item == null) {
        throwMissingItemException(id);
      }

      return item;
    }

    @Override
    public void remove() {
      throw new RuntimeException("Remove is not supported.");
    }
  }
}
