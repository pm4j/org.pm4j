package org.pm4j.common.pageable.querybased.pagequery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.modifications.ModificationHandler;
import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.PageableCollectionUtil;
import org.pm4j.common.pageable.querybased.QueryCollectionBase;
import org.pm4j.common.pageable.querybased.QueryCollectionModificationHandlerBase;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.SelectionHandler;
import org.pm4j.common.selection.SelectionHandlerWithAdditionalItems;
import org.pm4j.common.util.collection.ListUtil;

/**
 * A {@link PageableCollection} that uses a {@link PageQueryService} and {@link QueryParams}
 * to get the data to provide.
 *
 * @param <T_ITEM> type of handled items.
 * @param <T_ID> the item identifier type.
 *
 * @author olaf boede
 */
public class PageQueryCollection<T_ITEM, T_ID> extends QueryCollectionBase<T_ITEM, T_ID> {

  final PageQueryService<T_ITEM, T_ID>                  service;
  private final SelectionHandler<T_ITEM>                    selectionHandler;
  private final CachingPageQueryService<T_ITEM, T_ID>   cachingService;

  /**
   * @param service
   *          the service used to get the data.
   * @param queryOptions
   *          defined the id-attribute as well as the available sort and filter options.
   */
  public PageQueryCollection(PageQueryService<T_ITEM, T_ID> service, QueryOptions queryOptions) {
    super(queryOptions);

    this.service = service;
    this.cachingService = new CachingPageQueryService<T_ITEM, T_ID>(service);
    this.modificationHandler = new QueryCollectionModificationHandlerBase<T_ITEM, T_ID>(this, cachingService);

    // Handling of transient and persistent item selection is separated by a handler composition.
    SelectionHandler<T_ITEM> querySelectionHandler = new PageQuerySelectionHandler<T_ITEM, T_ID>(cachingService) {
      @Override
      protected QueryParams getQueryParams() {
        return getQueryParamsWithRemovedItems();
      }
    };
    querySelectionHandler.setFirePropertyEvents(false);
    this.selectionHandler = new SelectionHandlerWithAdditionalItems<T_ITEM>(this, querySelectionHandler);

  }

  /** In addition: reset the page item cache on sort order change. */
  @Override
  protected void onSortOrderChange() {
    super.onSortOrderChange();
    cachingService.getCache().clearPageCache();
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
    super.clearCaches();
    cachingService.getCache().clear();
  }

  @Override
  public ModificationHandler<T_ITEM> getModificationHandler() {
    return modificationHandler;
  }

}

/**
 * Iterates page wise through a pageable query collection.
 *
 * @param <T_ITEM> type of collection items.
 */
class PageableQueryCollectionIterator<T_ITEM> implements Iterator<T_ITEM> {
  private final PageQueryCollection<T_ITEM, ?> pc;
  private final QueryParams queryParams;
  private final List<T_ITEM> additionalItems;
  private final long numOfPages;
  private List<T_ITEM> pageItems;
  private int pageRowIdx = -1;
  private long currentPageIdx = -1;
  private T_ITEM next;

  public PageableQueryCollectionIterator(PageQueryCollection<T_ITEM, ?> pc) {
    assert pc != null;
    this.pc = pc;
    this.queryParams = pc.getQueryParamsWithRemovedItems();
    this.additionalItems = pc.getModifications().getAddedItems();
    this.numOfPages = PageableCollectionUtil.getNumOfPages(pc);
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
      PageableCollection<T_ITEM> pc,
      PageQueryService<T_ITEM, ?> pq,
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
        long firstItemIdx = PageableCollectionUtil.getIdxOfFirstItemOnPage(pc, pageIdx);
        itemsOnPage = pq.getItems(qp, firstItemIdx, pageSize);
      }
    } else {
      boolean mixedPage = (pageIdx == numOfPagesFilledByQueryItems) &&
                          (queryItemCount % pageSize) != 0;
      if (mixedPage) {
        // the page starts with query based items and has trailing additional items.
        long firstItemIdx = PageableCollectionUtil.getIdxOfFirstItemOnPage(pc, pageIdx);
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

}
