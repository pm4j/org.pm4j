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
import org.pm4j.common.pageable.Modifications;
import org.pm4j.common.pageable.ModificationsImpl;
import org.pm4j.common.pageable.PageableCollection2;
import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.pageable.PageableCollectionUtil2;
import org.pm4j.common.pageable.querybased.PageableQuerySelectionHandler.ItemIdSelection;
import org.pm4j.common.query.AttrDefinition;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.FilterNot;
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

  private final PageableQueryService<T_ITEM, T_ID>          service;
  private final PageableQuerySelectionHandler<T_ITEM, T_ID> selectionHandler;
  private final QueryCollectionModificationHandler          modificationHandler;
  private final CachingPageableQueryService<T_ITEM, T_ID>   cachingService;
  /** A cached reference to the query paramerters that considers the removed item set too. */
  private QueryParams                                       queryParamsWithRemovedItems;

  /**
   * Creates a service based collection without query restrictions.
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
    this.cachingService = new CachingPageableQueryService<T_ITEM, T_ID>(service);
    this.selectionHandler = new PageableQuerySelectionHandler<T_ITEM, T_ID>(cachingService) {
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
        cachingService.getCache().clearPageCache();
        queryParamsWithRemovedItems = null;
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T_ITEM> getItemsOnPage() {
    QueryParams queryParams = getQueryParamsWithRemovedItems();
    if (!queryParams.isExecQuery()) {
      return Collections.EMPTY_LIST;
    }
    long firstItemIdx = PageableCollectionUtil2.getIdxOfFirstItemOnPage(this)-1;
    if (firstItemIdx < 0) {
    	return Collections.EMPTY_LIST;
    }
    return cachingService.getItems(
        queryParams,
        firstItemIdx,
        getPageSize());
  }

  @Override
  public void setCurrentPageIdx(int pageIdx) {
    if (pageIdx != getCurrentPageIdx()) {
      cachingService.getCache().clearPageCache();
    }
    super.setCurrentPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return getQueryParams().isExecQuery()
    	? cachingService.getItemCount(getQueryParamsWithRemovedItems())
    	: 0;
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
    cachingService.getCache().clear();
    queryParamsWithRemovedItems = null;
  }

  @Override
  public ModificationHandler<T_ITEM> getModificationHandler() {
    return modificationHandler;
  }

  QueryParams getQueryParamsWithRemovedItems() {
	if (queryParamsWithRemovedItems == null) {
	    if (modificationHandler.getModifications().getRemovedItems().isEmpty()) {
	    	queryParamsWithRemovedItems = getQueryParams();
	    }
	    else {
		    QueryParams qParams = getQueryParams().clone();
		    FilterExpression queryFilterExpr = qParams.getFilterExpression();
		    FilterExpression removedItemsFilterExpr = modificationHandler.getRemovedItemsFilterExpr(queryFilterExpr);
		    qParams.setFilterExpression(queryFilterExpr != null
		        ? new FilterAnd(queryFilterExpr, removedItemsFilterExpr)
		        : removedItemsFilterExpr);
		    queryParamsWithRemovedItems = qParams;
	    }
	}

	return queryParamsWithRemovedItems;
  }

  /**
   * Handles transiend removed item information.
   */
  class QueryCollectionModificationHandler implements ModificationHandler<T_ITEM> {

    private ModificationsImpl<T_ITEM> modifications = new ModificationsImpl<T_ITEM>();

    public FilterExpression getRemovedItemsFilterExpr(FilterExpression queryFilterExpr) {
      AttrDefinition idAttr = new AttrDefinition("id", Long.class);
      @SuppressWarnings("unchecked")
      ClickedIds<T_ID> ids = modifications.getRemovedItems().isEmpty()
          ? new ClickedIds<T_ID>()
          : ((ItemIdSelection<T_ITEM, T_ID>)modifications.getRemovedItems()).getClickedIds();
      return new FilterNot(PageableCollectionUtil2.makeSelectionQueryParams(idAttr, queryFilterExpr, ids));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeSelectedItems() {
      Selection<T_ITEM> items = selectionHandler.getSelection();
      if (!selectionHandler.selectAll(false)) {
        return false;
      }
      if (items.getSize() == 0) {
        return true;
      }

      Selection<T_ITEM> oldRemovedItemSelection = modifications.getRemovedItems();
      if (items instanceof ItemIdSelection) {
        if (oldRemovedItemSelection.isEmpty()) {
          modifications.setRemovedItems((ItemIdSelection<T_ITEM, T_ID>) items);
        } else {
          Collection<T_ID> ids = ((ItemIdSelection<T_ITEM, T_ID>) items).getClickedIds().getIds();
          modifications.setRemovedItems(new ItemIdSelection<T_ITEM, T_ID>((ItemIdSelection<T_ITEM, T_ID>)oldRemovedItemSelection, ids));
        }
// TODO olaf: inverted selections are not yet handled by query
//      } else if (items instanceof PageableQuerySelectionHandler.InvertedSelection) {
//        removedInvertedItemSelections.addSelection(items);
      } else {
        // TODO: add exception interface signatur.
        long newSize = items.getSize() + oldRemovedItemSelection.getSize();
        if (newSize > 1000) {
          throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
        }

        Collection<T_ID> ids = new ArrayList<T_ID>((int)items.getSize());
        for (T_ITEM i : items) {
          ids.add(cachingService.getIdForItem(i));
        }

        modifications.setRemovedItems(oldRemovedItemSelection.isEmpty()
            ? new ItemIdSelection<T_ITEM, T_ID>(service, ids)
            : new ItemIdSelection<T_ITEM, T_ID>((ItemIdSelection<T_ITEM, T_ID>)oldRemovedItemSelection, ids));
      }

      clearCaches();
      return true;
    }

    /**
     * Adds the item as the last one.
     */
    @Override
    public void addItem(T_ITEM item) {
      // XXX olaf: check if this can be handled here. Currently it's handled in the wrapping
      // collection with additional items.
      // addedItems.add(item);
      throw new UnsupportedOperationException("Please embed this collection in a PageableCollectionWithAdditionalItems to handle add operations.");
    }

    public void updateItem(T_ITEM item) {
      modifications.registerUpdatedItem(item);
    };

    @Override
    public void clearRegisteredModifications() {
      modifications = new ModificationsImpl<T_ITEM>();
    }

    @Override
    public Modifications<T_ITEM> getModifications() {
      return modifications;
    }

  }

}
