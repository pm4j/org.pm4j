package org.pm4j.common.pageable.querybased;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
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
import org.pm4j.common.selection.SelectionHandlerUtil;
import org.pm4j.common.selection.SelectionHandlerWithAdditionalItems;
import org.pm4j.common.selection.SelectionWithAdditionalItems;
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

  final PageableQueryService<T_ITEM, T_ID>                  service;
  private final SelectionHandler<T_ITEM>                    selectionHandler;
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
    this.modificationHandler = new QueryCollectionModificationHandler();

    // Uses getQueryParams() because the super ctor may have created it.
    QueryParams myQueryParams = getQueryParams();

    // Handling of transient and persistent item selection is separated by a handler composition.
    SelectionHandler<T_ITEM> querySelectionHandler = new PageableQuerySelectionHandler<T_ITEM, T_ID>(cachingService) {
      @Override
      protected QueryParams getQueryParams() {
        return getQueryParamsWithRemovedItems();
      }
    };
    querySelectionHandler.setFirePropertyEvents(false);
    this.selectionHandler = new SelectionHandlerWithAdditionalItems<T_ITEM>(this, querySelectionHandler);

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

    return PageableQueryUtil.getQueryAndAdditionalItemsOnPage
        (this, cachingService, queryParams, getPageIdx(), getModifications().getAddedItems());
  }

  @Override
  public void setPageIdx(long pageIdx) {
    if (pageIdx != getPageIdx()) {
      cachingService.getCache().clearPageCache();
    }
    super.setPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return getQueryParams().isExecQuery()
    	? cachingService.getItemCount(getQueryParamsWithRemovedItems()) +
    	  modificationHandler.getModifications().getAddedItems().size()
    	: 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<T_ITEM> iterator() {
    return !getQueryParams().isExecQuery()
        ? Collections.EMPTY_LIST.iterator()
        : new PageableQueryCollectionIterator<T_ITEM>(this);
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
      @SuppressWarnings("unchecked")
      ClickedIds<T_ID> ids = modifications.getRemovedItems().isEmpty()
          ? new ClickedIds<T_ID>()
          : ((ItemIdSelection<T_ITEM, T_ID>)modifications.getRemovedItems()).getClickedIds();
      AttrDefinition idAttr = getQueryOptions().getIdAttribute();
      return new FilterNot(PageableCollectionUtil2.makeSelectionQueryParams(idAttr, queryFilterExpr, ids));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeSelectedItems() {
      // Nothing to remove in case of an empty selection.
      Selection<T_ITEM> items = selectionHandler.getSelection();
      if (items.getSize() == 0) {
        return true;
      }

      // Check for vetos
      try {
        PageableQueryCollection.this.fireVetoableChange(PageableCollection2.EVENT_REMOVE_SELECTION, items, null);
      } catch (PropertyVetoException e) {
        return false;
      }

      // Clear the selection. All selected items will be deleted.
      SelectionHandlerUtil.forceSelectAll(selectionHandler, false);

      // Identify the sets of persistent and transient items to delete.
      Selection<T_ITEM> persistentItems = items;

      // In case of a selection with transient items, we have to handle transient items accordingly.
      if (items instanceof SelectionWithAdditionalItems) {
        // Removed transient items will simply be forgotten.
        List<T_ITEM> transientItems = ((SelectionWithAdditionalItems<T_ITEM>)items).getAdditionalSelectedItems();
        for (T_ITEM i : new ArrayList<T_ITEM>(transientItems)) {
          modifications.unregisterAddedItem(i);
        }

        // Only the persistent sub-set needs to be handled in the following code.
        persistentItems = ((SelectionWithAdditionalItems<T_ITEM>)items).getBaseSelection();
      }

      // Remember the previous set of removed items. It needs to be extended by some additional items to remove.
      Selection<T_ITEM> oldRemovedItemSelection = modifications.getRemovedItems();


      if (persistentItems instanceof ItemIdSelection) {
        if (oldRemovedItemSelection.isEmpty()) {
          modifications.setRemovedItems((ItemIdSelection<T_ITEM, T_ID>) persistentItems);
        } else {
          Collection<T_ID> ids = ((ItemIdSelection<T_ITEM, T_ID>) persistentItems).getClickedIds().getIds();
          // XXX olaf: assumes that we handle removed items as ItemIdSelection. But that will change as soon
          // as we add remove handling for inverted selections.
          modifications.setRemovedItems(new ItemIdSelection<T_ITEM, T_ID>((ItemIdSelection<T_ITEM, T_ID>)oldRemovedItemSelection, ids));
        }
      } else {
     // TODO olaf: inverted selections are not yet handled by query
//      if (items instanceof PageableQuerySelectionHandler.InvertedSelection) {
//        removedInvertedItemSelections.addSelection(items);
        // TODO: add exception interface signatur.
        long newSize = persistentItems.getSize() + oldRemovedItemSelection.getSize();
        if (newSize > 1000) {
          throw new IndexOutOfBoundsException("Maximum 1000 rows can be removed within a single save operation.");
        }

        Collection<T_ID> ids = PageableQueryUtil.getItemIds(cachingService, persistentItems);
        modifications.setRemovedItems(oldRemovedItemSelection.isEmpty()
            ? new ItemIdSelection<T_ITEM, T_ID>(service, ids)
            : new ItemIdSelection<T_ITEM, T_ID>((ItemIdSelection<T_ITEM, T_ID>)oldRemovedItemSelection, ids));
      }

      clearCaches();
      PageableQueryCollection.this.firePropertyChange(PageableCollection2.EVENT_REMOVE_SELECTION, items, null);
      return true;
    }

    @Override
    public void addItem(T_ITEM item) {
      modifications.registerAddedItem(item);
      PageableQueryCollection.this.firePropertyChange(PageableCollection2.EVENT_ITEM_ADD, null, item);
    }

    @Override
    public void updateItem(T_ITEM item, boolean isUpdated) {
      // a modification of a new item should not lead to a double-listing within the updated list too.
      if (isUpdated && modifications.getAddedItems().contains(item)) {
        return;
      }

      boolean wasUpdated = modifications.getUpdatedItems().contains(item);
      modifications.registerUpdatedItem(item, isUpdated);
      PageableQueryCollection.this.firePropertyChange(PageableCollection2.EVENT_ITEM_UPDATE, wasUpdated, isUpdated);
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

/**
 * Iterates page wise through a pageable query collection.
 *
 * @param <T_ITEM> type of collection items.
 */
class PageableQueryCollectionIterator<T_ITEM> implements Iterator<T_ITEM> {
  private final PageableQueryCollection<T_ITEM, ?> pc;
  private final QueryParams queryParams;
  private final List<T_ITEM> additionalItems;
  private final long numOfPages;
  private List<T_ITEM> pageItems;
  private int pageRowIdx = -1;
  private long currentPageIdx = -1;
  private T_ITEM next;

  public PageableQueryCollectionIterator(PageableQueryCollection<T_ITEM, ?> pc) {
    assert pc != null;
    this.pc = pc;
    this.queryParams = pc.getQueryParamsWithRemovedItems();
    this.additionalItems = pc.getModifications().getAddedItems();
    this.numOfPages = PageableCollectionUtil2.getNumOfPages(pc);
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

  private void readNext() {
    next = null;
    ++pageRowIdx;
    if ((pageItems == null) || (pageRowIdx == pc.getPageSize())) {
      ++currentPageIdx;
      if (currentPageIdx >= numOfPages) {
        return;
      }
      pageItems = PageableQueryUtil.getQueryAndAdditionalItemsOnPage
                    (pc, pc.service, queryParams, currentPageIdx, additionalItems);
      pageRowIdx = 0;
    }

    if (pageRowIdx < pageItems.size()) {
      next = pageItems.get(pageRowIdx);
    }
  }
}

/**
 * Helper methods for pageable queries.
 */
class PageableQueryUtil {

  /**
   * Provides the set of page items for a mix of query based and additional transient items.
   *
   * @param pc the pageable collection
   * @param pq
   * @param qp
   * @param pageIdx
   * @param additionalItems
   * @return
   */
  public static <T_ITEM> List<T_ITEM> getQueryAndAdditionalItemsOnPage(
      PageableCollection2<T_ITEM> pc,
      PageableQueryService<T_ITEM, ?> pq,
      QueryParams qp,
      long pageIdx,
      List<T_ITEM> additionalItems)
  {
    List<T_ITEM> itemsOnPage;
    int pageSize = pc.getPageSize();
    long queryItemCount = pq.getItemCount(qp);
    long numOfPagesFilledByQueryItems = queryItemCount / pageSize;

    if (additionalItems.isEmpty() ||
        (pageIdx < numOfPagesFilledByQueryItems)) {
      // all page items are accessible through the query service.
      if (queryItemCount == 0) {
        itemsOnPage = Collections.emptyList();
      } else {
        long firstItemIdx = PageableCollectionUtil2.getIdxOfFirstItemOnPage(pc, pageIdx);
        itemsOnPage = pq.getItems(qp, firstItemIdx, pageSize);
      }
    } else {
      boolean mixedPage = (pageIdx == numOfPagesFilledByQueryItems) &&
                          (queryItemCount % pageSize) != 0;
      if (mixedPage) {
        // the page starts with query based items and has trailing additional items.
        long firstItemIdx = PageableCollectionUtil2.getIdxOfFirstItemOnPage(pc, pageIdx);
        itemsOnPage = new ArrayList<T_ITEM>(pq.getItems(qp, firstItemIdx, pageSize));
        for (T_ITEM i : additionalItems) {
          if (itemsOnPage.size() >= pageSize) {
            break;
          }
          itemsOnPage.add(i);
        }
      } else {
        // the page shows only additional items.
        long firstItemIdx = (pageIdx) * pageSize;
        int offset = (int)(firstItemIdx - queryItemCount);
        itemsOnPage = new ArrayList<T_ITEM>(ListUtil.subListPage(additionalItems, offset, pageSize));
      }
    }

    return itemsOnPage;
  }

  public static <T_ITEM, T_ID> Collection<T_ID> getItemIds(PageableQueryService<T_ITEM, T_ID> service, Selection<T_ITEM> items) {
    Collection<T_ID> ids = new ArrayList<T_ID>((int)items.getSize());
    for (T_ITEM i : items) {
      ids.add(service.getIdForItem(i));
    }
    return ids;
  }

}
