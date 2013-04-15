package org.pm4j.common.pageable.querybased;

import java.util.List;

import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.selection.ItemIdConverter;

/**
 * Provides the data used for the {@link PageableQueryCollection}.
 *
 * @param <T_BEAN>
 *          the type of handled colleciton items.
 * @param <T_ID>
 *          the bean identifier type.
 *
 * @author olaf boede
 */
public interface PageableQueryService<T_BEAN, T_ID> extends ItemIdConverter<T_BEAN, T_ID> {

  /**
   * Provides an item subset. E.g. the items for a page to display.
   *
   * @param query
   *          the query to execute.
   * @param startIdx
   *          index of the first item to provide. Starts with zero.
   * @param pageSize
   *          The number of items to provide.
   * @return the set of found items. The last page may be contain less items
   *         than <code>pageSize</code>.
   */
  List<T_BEAN> getItems(QueryParams query, long startIdx, int pageSize);

  /**
   * Provides the number of items that match the given query filter criteria.
   *
   * @param query
   * @return
   */
  long getItemCount(QueryParams query);

  /**
   * Provides the set of filter definitions and attribute sort orders that can be
   * processed by this service.
   *
   * @return
   */
  QueryOptions getQueryOptions();
}
