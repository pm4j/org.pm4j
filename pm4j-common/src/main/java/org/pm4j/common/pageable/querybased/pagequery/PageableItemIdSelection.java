package org.pm4j.common.pageable.querybased.pagequery;

import java.util.Collection;
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
public class PageableItemIdSelection<T_ITEM, T_ID> extends PageQueryItemIdSelection<T_ITEM, T_ID> {
  private static final long serialVersionUID = 1L;

  /** The query constraints and sort order for retrieving selected records based on selected IDs. */
  private QueryParams selectedIdsQueryParams;

  /**
   * Creates a selection based on a set of selected id's.
   *
   * @param service the service used to retrieve items for the selected id's.
   * @param idAttr the item ID attribute to use.
   * @param queryParams provides the sort order and custom query properties for the {@link QueryService} that executes the internally used query operation.
   * @param ids the set of selected id's.
   */
  public PageableItemIdSelection(PageQueryService<T_ITEM, T_ID> service, QueryAttr idAttr, QueryParams queryParams, Collection<T_ID> ids) {
    super(service, ids);
    this.selectedIdsQueryParams = queryParams.clone();
    this.selectedIdsQueryParams.setQueryExpression(new QueryExprCompare(idAttr, CompOpIn.class, ids));
  }

  @Override
  protected PageQueryService<T_ITEM, T_ID> getService() {
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