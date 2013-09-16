package org.pm4j.common.pageable.querybased;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pm4j.common.pageable.PageableCollectionBase2;
import org.pm4j.common.query.FilterAnd;
import org.pm4j.common.query.FilterExpression;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;

/**
 * Provides some basic implementation for query based pageable collections.
 *
 * @author oboede
 *
 * @param <T_ITEM> Type of items to handle.
 * @param <T_ID> The item id type.
 */
public abstract class PageableQueryCollectionBase<T_ITEM, T_ID> extends PageableCollectionBase2<T_ITEM> {

  /** Handler for item modifications. */
  protected QueryCollectionModificationHandlerBase<T_ITEM, T_ID> modificationHandler;

  /** A cached reference to the query parameters that considers the removed item set too. */
  private QueryParams                                       queryParamsWithRemovedItems;

  public PageableQueryCollectionBase(QueryOptions queryOptions) {
    super(queryOptions);

    // Uses getQueryParams() because the super ctor may have created it.
    QueryParams myQueryParams = getQueryParams();

    myQueryParams.addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_FILTER, new PropertyChangeListener() {
      @Override public void propertyChange(PropertyChangeEvent evt) {
        onQueryCriteriaChange();
      }
    });
    myQueryParams.addPropertyChangeListener(QueryParams.PROP_EFFECTIVE_SORT_ORDER, new PropertyChangeListener() {
      @Override public void propertyChange(PropertyChangeEvent evt) {
        onSortOrderChange();
      }
    });
    addPropertyChangeListener(EVENT_REMOVE_SELECTION, new PropertyChangeListener() {
      @Override public void propertyChange(PropertyChangeEvent evt) {
        @SuppressWarnings("unchecked")
        Selection<T_ITEM> removedItems = (Selection<T_ITEM>) evt.getOldValue();
        onRemoveItems(removedItems);
      }
    });

  }

  protected void onRemoveItems(Selection<T_ITEM> removedItems) {
    queryParamsWithRemovedItems = null;
  }

  /** Reset all caches on each query filter criteria change. */
  protected void onQueryCriteriaChange() {
    clearCaches();
  }

  /** Clear the generated query params. They are invalid now. */
  protected void onSortOrderChange() {
    queryParamsWithRemovedItems = null;
  }

  public QueryParams getQueryParamsWithRemovedItems() {
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

  @Override
  public void clearCaches() {
    queryParamsWithRemovedItems = null;
  }



}
