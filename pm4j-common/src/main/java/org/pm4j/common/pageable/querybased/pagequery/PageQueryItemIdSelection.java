package org.pm4j.common.pageable.querybased.pagequery;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.querybased.NoItemForKeyFoundException;
import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryParams;

/**
 * A selection that holds the ID's of selected items.
 * <p>
 * It uses a {@link PageQueryService} instance to retrieve the selected instances from the service.
 */
/*package*/ class PageQueryItemIdSelection<T_ITEM, T_ID> extends PageQuerySelectionHandler.QuerySelectionWithClickedIds<T_ITEM, T_ID> {
  private static final long serialVersionUID = 1L;

  private final Collection<T_ID> ids;

  private int readBlockSize = 1;

  /** The query constraints and sort order for retrieving selected records based on selected IDs. */
  protected final QueryParams selectedIdsQueryParams;

  /**
   * Creates a selection based on a set of selected id's which keeps the original sort order.
   *
   * @param service the service used to retrieve items for the selected id's.
   * @param idAttr the item ID attribute to use.
   * @param queryParams provides the sort order and custom query properties for the {@link QueryService} that executes the internally used query operation.
   * @param ids the set of selected id's.
   * @param sortOrderAware true of the iteration must use the original sort order, false if this is not necessary (more efficient)
   */
  @SuppressWarnings("unchecked")
  /*package*/ PageQueryItemIdSelection(PageQueryService<T_ITEM, T_ID> service, QueryAttr idAttr, QueryParams queryParams, Collection<T_ID> ids, boolean sortOrderAware) {
    super(service);
    this.ids = (ids != null) ? Collections.unmodifiableCollection(ids) : Collections.EMPTY_LIST;
    this.selectedIdsQueryParams = queryParams.clone(); // keep parameters like sort order, but use a different query expression
    if (!sortOrderAware) {
      queryParams.setSortOrder(null); // to speed up the queries
    }
    this.selectedIdsQueryParams.setQueryExpression(new QueryExprCompare(idAttr, CompOpIn.class, ids));
  }

  @Override
  public long getSize() {
    return ids.size();
  }

  @Override
  public boolean contains(T_ITEM item) {
    return ids.contains(getService().getIdForItem(item));
  }

  /** Sets the number of rows to retrieve at once to reduce number of SQL calls.
   * Not all implementations might support the block size.
   * For UI, e.g. record navigators, block size 1 is to preferred to avoid outdated records.
   * For bulk data processes, e.g. generating reports, a larger block size should be set.
   *
   * @param readBlockSize number of rows, default is null
   */
  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    this.readBlockSize = readBlockSize;
  }

  /**
   * return the suggested number of rows to retrieve at once to reduce number of SQL calls.
   */
  public int getIteratorBlockSizeHint() {
    return readBlockSize;
  }

  @Override
  public ClickedIds<T_ID> getClickedIds() {
    return new ClickedIds<T_ID>(ids, false);
  }

  @Override
  public PageQueryService<T_ITEM, T_ID> getService() {
    return (PageQueryService<T_ITEM, T_ID>) super.getService();
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new PagingItemIterator();
  }

  class PagingItemIterator implements Iterator<T_ITEM> {

    private int idIndex = 0;
    private List<T_ITEM> chunk = null;
    private int relativeIdIndex = 0;

    @Override
    public boolean hasNext() {
      return idIndex<getSize();
    }

    @Override
    public T_ITEM next() {
      // If the database changed, we might skip entries.
      // This can not avoided at 100%, and any try to reduce frequency of this quirk is expensive.
      if ( chunk == null || relativeIdIndex >= chunk.size() ) {
        relativeIdIndex = 0;
        chunk = getService().getItems(selectedIdsQueryParams, idIndex, getIteratorBlockSizeHint());
        if (chunk.size() == 0) {
          throw new NoItemForKeyFoundException("No further item found. It may have been deleted by a concurrent operation." +
              "\n\tUsed query service: " + getService());
        }
      }

      idIndex++;
      return chunk.get(relativeIdIndex++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}