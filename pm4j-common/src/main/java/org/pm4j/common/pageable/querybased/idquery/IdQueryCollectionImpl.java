package org.pm4j.common.pageable.querybased.idquery;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.pageable.querybased.QueryCollectionBase;
import org.pm4j.common.pageable.querybased.QueryCollectionModificationHandlerBase;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerWithAdditionalItems;
import org.pm4j.common.selection.SelectionHandlerWithIdSet;
import org.pm4j.common.util.collection.ListUtil;

/**
 * A {@link PageableCollection} based on an {@link IdQueryService}.
 * <p>
 * To get the items it first asks the service for the ID's by calling {@link IdQueryService#findIds(QueryParams, long, int)}.
 * After that it asks the service to provide the items for the current page by calling {@link IdQueryService#getItems(List)}.
 * <p>
 * The current implementation is can handle a limited result set size because it holds all item ID's in
 * memory.<br>
 * Future implementations overcome that limitation by reading chunks of ID's.
 *
 * @author oboede
 *
 * @param <T_ITEM> The type of handled items.
 * @param <T_ID> The item identifier type.
 */
public class IdQueryCollectionImpl<T_ITEM, T_ID> extends QueryCollectionBase<T_ITEM, T_ID> {

  private static final Log LOG = LogFactory.getLog(IdQueryCollectionImpl.class);

  private final IdQueryService<T_ITEM, T_ID> service;
  /** The collection type specific selection handler. */
  private final SelectionHandler<T_ITEM>  selectionHandler;
  private List<T_ID>                      ids;
  private List<T_ITEM>                    currentPageItems;
  private AddItemStrategy                 addItemStrategy = new AddItemStrategyAtTheEnd();

  /**
   * Maintains the set of ID's on removing items.
   */
  protected void onRemoveItems(Selection<T_ITEM> removedItems) {
    super.onRemoveItems(removedItems);
    boolean clearCachedItems = false;
    // XXX oboede: The iteration over removed items works only for limited selections.
    // Needs to be optimized when we support large item set.
    for (T_ITEM item : removedItems) {
      if (ids != null) {
        T_ID id = getService().getIdForItem(item);
        ids.remove(id);
      }
      if ((currentPageItems != null) && currentPageItems.contains(item)) {
        clearCachedItems = true;
      }
    }
    if (clearCachedItems) {
      currentPageItems = null;
    }
  }

  public IdQueryCollectionImpl(IdQueryService<T_ITEM, T_ID> service, QueryOptions queryOptions) {
    super(queryOptions);
    assert service != null;

    this.service = service;

    // Handling of transient and persistent item selection is separated by a handler composition.
    SelectionHandler<T_ITEM> querySelectionHandler = new SelectionHandlerWithIdSet<T_ITEM, T_ID>(service) {
      @Override
      protected Collection<T_ID> getAllIds() {
        return getIds();
      }
    };
    querySelectionHandler.setFirePropertyEvents(false);
    this.selectionHandler = new SelectionHandlerWithAdditionalItems<T_ITEM>(this, querySelectionHandler);

    this.modificationHandler = new QueryCollectionModificationHandlerBase<T_ITEM, T_ID>(this, service);

    PropertyChangeListener resetItemsOnQueryChangeListener = new PropertyChangeListener() {
      @Override public void propertyChange(PropertyChangeEvent evt) {
        clearCaches();
      }
    };
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, resetItemsOnQueryChangeListener);
    getQueryParams().addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, resetItemsOnQueryChangeListener);

    addPropertyChangeListener(PageableCollection.EVENT_ITEM_ADD, new PropertyChangeListener() {
      @SuppressWarnings("unchecked")
      @Override public void propertyChange(PropertyChangeEvent evt) {
        addItemStrategy.onAddItem((T_ITEM)evt.getNewValue());
      }
    });
  }

  protected IdQueryService<T_ITEM, T_ID> getService() {
    return service;
  }

  @Override
  public List<T_ITEM> getItemsOnPage() {
    if (currentPageItems == null) {
      synchronized (this) {
        if (currentPageItems == null) {
          currentPageItems = addItemStrategy.getCurrentPageItems();
        }
      }
    }

    return currentPageItems;
  }

  @Override
  public void setPageIdx(long pageIdx) {
    if (pageIdx != getPageIdx()) {
      currentPageItems = null;
    }
    super.setPageIdx(pageIdx);
  }

  @Override
  public long getNumOfItems() {
    return getIds().size() + modificationHandler.getModifications().getAddedItems().size();
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
  public ModificationHandler<T_ITEM> getModificationHandler() {
    return modificationHandler;
  }

  @Override
  public void clearCaches() {
    super.clearCaches();
    ids = null;
    currentPageItems = null;
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
        QueryParams queryParams = getQueryParamsWithRemovedItems();
        ids = queryParams.isExecQuery()
            ? service.findIds(queryParams, 0, Integer.MAX_VALUE)
            // In no-exec case: an unmodifyable collection.
            : Collections.EMPTY_LIST;
      }
      return ids;
    }
  }

  /**
   * A simple iterator that iterates slowly over all items using a lot of service calls.
   *
   * @author olaf boede
   */
  class ItemIterator implements Iterator<T_ITEM> {
    private int nextIdx = -1;
    private T_ITEM next = null;
    private final List<T_ID> ids = getIds();

    public ItemIterator() {
      addItemStrategy.readNext(this);
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public T_ITEM next() {
      T_ITEM current = next;
      addItemStrategy.readNext(this);
      return current;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Remove is not supported.");
    }
  }

  /**
   * A strategy that provides the logic for operations that are related to the
   * added item sort order.<br>
   * This allows (for future implementations) to add strategies for other new-item
   * sort order definitions.
   * <p>
   * (Is not an interface because an embedded interface can't use the generics of the
   * embedding class.)
   */
  abstract class AddItemStrategy {
    public abstract void onAddItem(T_ITEM item);
    public abstract void readNext(ItemIterator iter);
    public abstract List<T_ITEM> getCurrentPageItems();
  }

  /**
   * Adds the new items at the end of the pageable collection.
   */
  class AddItemStrategyAtTheEnd extends AddItemStrategy {

    @Override
    public void onAddItem(T_ITEM item) {
      // Add the new item to the current page cache list if it's just the last page.
      if ((currentPageItems != null) &&
          (currentPageItems.size() < getPageSize()) &&
          (PageableCollectionUtil.getNumOfPages(IdQueryCollectionImpl.this) == getPageIdx()+1)) {
        currentPageItems.add(item);
      }
    }

    @Override
    public void readNext(ItemIterator iter) {
      int maxIdListIdx = iter.ids.size()-1;
      do {
    	++iter.nextIdx;
        if (iter.nextIdx > maxIdListIdx) {
          List<T_ITEM> addedItems = modificationHandler.getModifications().getAddedItems();
          int addedItemIdx = iter.nextIdx - maxIdListIdx - 1;
          iter.next = (addedItemIdx < addedItems.size())
            ? addedItems.get(addedItemIdx)
            : null;
          return;
        }

        T_ID id = iter.ids.get(iter.nextIdx);
        iter.next = service.getItemForId(id);
        if (iter.next == null) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("No item found for ID: " + id + ". Iteration skips this item.");
          }
        }
      } while (iter.next == null);
    }

    public List<T_ITEM> getCurrentPageItems() {
        List<T_ID> pageIds = getCurrentPageIds();
        List<T_ITEM> items = pageIds.isEmpty()
            ? new ArrayList<T_ITEM>()
            : service.getItems(pageIds);
        List<T_ITEM> additionalItems = modificationHandler.getModifications().getAddedItems();
        if (!additionalItems.isEmpty()) {
          long pageIdx = getPageIdx();
          int pageSize = getPageSize();
          long queryItemCount = getIds().size();
          long numOfPagesFilledByQueryItems = queryItemCount / pageSize;
          if (pageIdx >= numOfPagesFilledByQueryItems) {
            boolean mixedPage = (pageIdx == numOfPagesFilledByQueryItems) &&
                (queryItemCount % pageSize) != 0;
            if (mixedPage) {
              // the page starts with query based items and has trailing additional items.
              for (T_ITEM i : additionalItems) {
                if (items.size() >= pageSize) {
                  break;
                }
                items.add(i);
              }
            } else {
              // the page shows only additional items.
              long firstItemIdx = (pageIdx) * pageSize;
              int offset = (int)(firstItemIdx - queryItemCount);
              items = new ArrayList<T_ITEM>(ListUtil.subListPage(additionalItems, offset, pageSize));
            }
          }
        }
        return items;
    }

    protected List<T_ID> getCurrentPageIds() {
      // TODO oboede: change the utility method to a zero based index.
      int first = (int)PageableCollectionUtil.getIdxOfFirstItemOnPage(IdQueryCollectionImpl.this) - 1;
      long last = PageableCollectionUtil.getIdxOfLastItemOnPage(IdQueryCollectionImpl.this);

      if (first < 0) {
        return Collections.emptyList();
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
  }
}
