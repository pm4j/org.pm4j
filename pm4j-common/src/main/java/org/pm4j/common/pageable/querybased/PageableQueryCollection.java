package org.pm4j.common.pageable.querybased;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.ModificationHandler;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.pageable.querybased.PageableQuerySelectionHandler.ItemIdSelection;
import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;
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
  private PageableQuerySelectionHandler<T_ITEM, T_ID> selectionHandler;
  private QueryCollectionModificationHandler       modificationHandler;

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
    this.selectionHandler = new PageableQuerySelectionHandler<T_ITEM, T_ID>(service) {
      @Override
      protected QueryParams getQueryParams() {
        return getQueryParamsWithRemovedItems();
      }
    };

    this.modificationHandler = new QueryCollectionModificationHandler();

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
      QueryParams queryParams = getQueryParamsWithRemovedItems();
      pageItemsCache = (startIdx > -1 && queryParams.isExecQuery())
      		? service.getItems(queryParams, startIdx, getPageSize())
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
          ? service.getItemCount(getQueryParamsWithRemovedItems())
          : 0);
  }

  @Override
  public long getUnfilteredItemCount() {
    return (unfilteredItemCountCache != -1)
        ? unfilteredItemCountCache
        : (unfilteredItemCountCache = getQueryParams().isExecQuery()
          ? service.getUnfilteredItemCount(getQueryParamsWithRemovedItems())
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
        next = ListUtil.listToItemOrNull(service.getItems(getQueryParamsWithRemovedItems(), currentIdx++, 1));
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

  QueryParams getQueryParamsWithRemovedItems() {
    if ((modificationHandler != null) &&
        !modificationHandler.hasRemovedItems()) {
      return getQueryParams();
    }

    QueryParams qParams = getQueryParams().clone();
    FilterExpression queryFilterExpr = qParams.getFilterExpression();
    FilterExpression removedItemsFilterExpr = modificationHandler.getRemovedItemsFilterExpr(queryFilterExpr);
    qParams.setFilterExpression(queryFilterExpr != null
        ? new FilterAnd(queryFilterExpr, removedItemsFilterExpr)
        : removedItemsFilterExpr);
    return qParams;
  }


  /**
   * Handles transiend removed item information.
   */
  class QueryCollectionModificationHandler implements ModificationHandler<T_ITEM> {

    //private List<T_ITEM> addedItems = new ArrayList<T_ITEM>();
    //SelectionSet<T_ITEM> removedInvertedItemSelections = new SelectionSet<T_ITEM>();
    private ItemIdSelection<T_ITEM, T_ID> removedIdSelection;

    /**
     * @return <code>true</code> if there are removed items registered.
     */
    // @Override
    public boolean hasRemovedItems() {
      return (removedIdSelection != null) &&
             (removedIdSelection.getSize() > 0);
    }

    public FilterExpression getRemovedItemsFilterExpr(FilterExpression queryFilterExpr) {
      AttrDefinition idAttr = new AttrDefinition("id", Long.class);
      return PageableCollectionUtil2.makeSelectionQueryParams(idAttr, queryFilterExpr, removedIdSelection.getClickedIds());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeItems(Selection<T_ITEM> items) {
      if (items instanceof ItemIdSelection) {
        if (removedIdSelection == null) {
          removedIdSelection = (ItemIdSelection<T_ITEM, T_ID>) items;
        } else {
          Collection<T_ID> ids = ((ItemIdSelection<T_ITEM, T_ID>) items).getClickedIds().getIds();
          removedIdSelection = new ItemIdSelection<T_ITEM, T_ID>(removedIdSelection, ids);
        }
// TODO olaf: inverted selections are not yet handled by query
//      } else if (items instanceof PageableQuerySelectionHandler.InvertedSelection) {
//        removedInvertedItemSelections.addSelection(items);
      } else {
        // TODO: add exception interface signatur.
        long newSize = items.getSize() + (removedIdSelection == null ? 0 : removedIdSelection.getSize());
        if (newSize > 1000) {
          throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
        }

        Collection<T_ID> ids = new ArrayList<T_ID>((int)items.getSize());
        for (T_ITEM i : items) {
          ids.add(service.getIdForItem(i));
        }

        removedIdSelection = (removedIdSelection == null)
            ? new ItemIdSelection<T_ITEM, T_ID>(service, ids)
            : new ItemIdSelection<T_ITEM, T_ID>(removedIdSelection, ids);
      }
    }

    /**
     * Adds the item as the last one.
     */
    @Override
    public void addItem(T_ITEM item) {
      // XXX olaf: check if this can be handled here. Currently it's handled in the wrapping
      // collection with additional items.
      //addedItems.add(item);
      throw new UnsupportedOperationException("Please embed this collection in a PageableCollectionWithAdditionalItems to handle add operations.");
    };

  }

}
