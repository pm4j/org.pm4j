package org.pm4j.common.pageable.querybased;

import org.pm4j.common.query.QueryParams;

/**
 * Converts between query or selection items and their corresponding identifiers.
 *
 * @param <T_ITEM> the item type.
 * @param <T_ID> the item id type.
 *
 * @author olaf boede
 */
public interface QueryService<T_ITEM, T_ID> {

  /**
   * Provides the number of items that match the given query filter criteria.
   *
   * @param query The query the defines the result.
   * @return The number of query result matches.
   */
  long getItemCount(QueryParams query);

  /**
   * @param item the item to get the identifier for.
   * @return the item identifier.
   */
  T_ID getIdForItem(T_ITEM item);

  /**
   * @param id the identifier to get the item for.
   * @return the item.
   */
  T_ITEM getItemForId(T_ID id);
}
