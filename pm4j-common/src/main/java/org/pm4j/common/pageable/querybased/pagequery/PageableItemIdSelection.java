package org.pm4j.common.pageable.querybased.pagequery;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.pageable.querybased.NoItemForKeyFoundException;
import org.pm4j.common.pageable.querybased.idquery.MaxQueryResultsViolationException;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryExprAnd;
import org.pm4j.common.query.QueryExprCompare;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;

/**
 * A selection that holds the ID's of selected items.
 * <p>
 * It uses a {@link PageQueryService} instance to retrieve the selected instances from the service.
 */
public class PageableItemIdSelection<T_ITEM, T_ID> extends ItemIdSelection<T_ITEM, T_ID> {
  private static final long serialVersionUID = 1L;
  
  private QueryParams queryParams;

  /**
   * Creates a selection based on a set of selected id's.
   *
   * @param service the service used to retrieve items for the selected id's.
   * @param queryOptions 
   * @param queryParams 
   * @param ids the set of selected id's.
   */
  public PageableItemIdSelection(PageQueryService<T_ITEM, T_ID> service, QueryOptions queryOptions, QueryParams queryParams, Collection<T_ID> ids) {
    super(service, ids);
    this.queryParams = addItemIds(queryParams, ids, queryOptions.getIdAttribute());
  }

  @Override
  protected PageQueryService<T_ITEM, T_ID> getService() {
    return (PageQueryService<T_ITEM, T_ID>) super.getService();
  }
  
  private static QueryParams addItemIds(QueryParams origQueryParams, Collection<?> ids, QueryAttr idQueryAttr) {
    QueryParams queryParams = origQueryParams.clone();
    QueryExpr inIdsQuery = new QueryExprCompare(idQueryAttr, CompOpIn.class, ids);
    queryParams.setQueryExpression(and(origQueryParams.getQueryExpression(), inIdsQuery));
    return queryParams;
  }

  private static QueryExpr and(QueryExpr expr1, QueryExpr expr2) {
    if ( expr1 == null) {
      return expr2;
    } else if ( expr2 == null) {
      return expr1;
    }
    return new QueryExprAnd(expr1, expr2);
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
      // If the database changed, We might get entries repeatedly or skip entries.
      // This can not avoided at 100%, and any try to reduce frequency of this quirk is expensive.
      
      if ( chunk == null || relativeIdIndex >= chunk.size() ) {
        relativeIdIndex = 0;
        chunk = getService().getItems(queryParams, idIndex, getIteratorBlockSizeHint());
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