package org.pm4j.common.pageable.querybased.idquery;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOG = LoggerFactory.getLogger(IdQueryCollectionImpl.class);

  private final IdQueryService<T_ITEM, T_ID>    service;
  /** The collection type specific selection handler. */
  private final SelectionHandler<T_ITEM>        selectionHandler;
  private List<T_ID>                            ids;
  private List<T_ITEM>                          currentPageItems;
  private AddItemStrategy                       addItemStrategy = new AddItemStrategyAtTheEnd();
  private final IdQueryStrategy                 idQueryStrategy;

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

  /**
   * Creates an instance that uses {@link SingleQueryStrategy}.
   *
   * @param service
   *          Provides the required query functionality.
   * @param queryOptions
   *          The set of sort order and filter restrictions that may be adjusted
   *          by the user (or application).
   */
  public IdQueryCollectionImpl(IdQueryService<T_ITEM, T_ID> service, QueryOptions queryOptions) {
    this(service, queryOptions, SingleQueryStrategy.INSTANCE);
  }

  /**
   * @param service
   *          Provides the required query functionality.
   * @param queryOptions
   *          The set of sort order and filter restrictions that may be adjusted
   *          by the user (or application).
   * @param idQueryStrategy
   *          The used query strategy.
   */
  public IdQueryCollectionImpl(IdQueryService<T_ITEM, T_ID> service, QueryOptions queryOptions, IdQueryStrategy idQueryStrategy) {
    super(queryOptions);
    assert service != null;
    assert idQueryStrategy != null;

    this.service = service;

    // Handling of transient and persistent item selection is separated by a handler composition.
    SelectionHandler<T_ITEM> querySelectionHandler = new IdQuerySelectionHandler<T_ITEM, T_ID>(service) {
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

    this.idQueryStrategy = idQueryStrategy;
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
    // getIds() throws a MaxResultsViolationException, if the number of found items in the database is larger than maxResultsLimit.
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
   * @throws MaxQueryResultsViolationException Thrown if the number of requested database records is larger than {@link QueryParams#getMaxQueryRecords()}.
   */
  @SuppressWarnings("unchecked")
  protected List<T_ID> getIds() {
    if (ids != null) {
      return ids;
    }

    synchronized (this) {
      if (ids == null) {
        currentPageItems = null;
        QueryParams queryParams = getQueryParamsWithRemovedItems();

        ids = queryParams.isExecQuery()
            ? idQueryStrategy.getIds(service, queryParams)
            // In no-exec case: an unmodifyable collection.
            : Collections.EMPTY_LIST;
      }

      return ids;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T_ITEM, T_ID> List<T_ID> findIds(IdQueryService<T_ITEM, T_ID> service, QueryParams queryParams, int maxListSize) {
    return queryParams.isExecQuery()
        ? service.findIds(queryParams, 0, maxListSize)
        // In no-exec case: an unmodifyable collection.
        : Collections.EMPTY_LIST;
  }

  private static int getMaxResultsAsInt(QueryParams queryParams) {
    long maxResults = queryParams.getMaxResults() != null ? queryParams.getMaxResults().longValue() : Integer.MAX_VALUE;
    if (maxResults > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("An IdQuery can't be configured to provide more than " + Integer.MAX_VALUE + " results.");
    }
    return (int)maxResults;
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

  /**
   * Interface for implementing a strategy to get the ID set according to performance considerations.
   */
  public interface IdQueryStrategy {

    /**
     * Finds all ID's set according to the given <code>queryParams</code>.
     *
     * @param service Provides the used query functionality.
     * @param queryParams Provides the restrictions and sort order to consider.
     * @return the ID set according to the current query configuration.
     * @throws MaxQueryResultsViolationException Thrown if the number of records provided by the service is larger than {@link QueryParams#getMaxQueryRecords()}.
     */
    List<?> getIds(IdQueryService<?, ?> service, QueryParams queryParams);
  }

  /**
   * A strategy that provides the ID set using a single
   * {@link IdQueryService#findIds(QueryParams, long, int)} request.
   * <p>
   * That strategy is fine for most scenarios.
   * <p>
   * The following performance risk should be considered:<br>
   * If the search restrictions cause an unexpected large result set, a DB query
   * may waste a time for sorting an unlimited result that is finally not used
   * by the following control flow because of a result size limit violation.<br>
   * If you get such problems you may consider using
   * {@link ExtraCountQueryStrategy}.
   */
  public static class SingleQueryStrategy implements IdQueryStrategy {

    /** An instance that may be used as a kind of singleton. */
    public static final SingleQueryStrategy INSTANCE = new SingleQueryStrategy();

    @Override
    public List<?> getIds(IdQueryService<?, ?> service, QueryParams queryParams) {
      // If possible increase the number of maxResultsLimit with 1, so that it is possible to check that the query returns more than maxResultsLimit entries.
      int maxResults = getMaxResultsAsInt(queryParams);
      int maxListSize = maxResults == Integer.MAX_VALUE ? Integer.MAX_VALUE : maxResults+1;

      List<?> ids = findIds(service, queryParams, maxListSize);
      if (ids.size() > maxResults) {
        // The actual number of items is not known. Thus we pass a 'null' here.
        throw new MaxQueryResultsViolationException(maxResults, null);
      }

      return ids;
    }
  }

  /**
   * A strategy that provides the ID set using the following service calls:
   * <ul>
   * <li>{@link IdQueryService#getItemCount(QueryParams)} and (if the count is
   * within the limits) a call to</li>
   * <li>{@link IdQueryService#findIds(QueryParams, long, int)}</li>
   * </ul>
   * That strategy is useful for some scenarios where performance
   * problems are expected in case of unlimited large result sets.<br>
   * An order-by clause might make the
   * {@link IdQueryService#findIds(QueryParams, long, int)} much more expensive
   * than the {@link IdQueryService#getItemCount(QueryParams)}.
   */
  public static class ExtraCountQueryStrategy implements IdQueryStrategy {

    /** An instance that may be used as a kind of singleton. */
    public static final ExtraCountQueryStrategy INSTANCE = new ExtraCountQueryStrategy();

    @Override
    public List<?> getIds(IdQueryService<?, ?> service, QueryParams queryParams) {
      int maxResults = getMaxResultsAsInt(queryParams);
      long itemCount = service.getItemCount(queryParams);
      if (itemCount > maxResults) {
        throw new MaxQueryResultsViolationException(maxResults, itemCount);
      }

      return findIds(service, queryParams, maxResults);
    }
  }
}
