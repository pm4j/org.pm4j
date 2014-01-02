package org.pm4j.common.pageable.querybased.pagequery;

import java.util.List;

import org.pm4j.common.pageable.QueryService;
import org.pm4j.common.query.QueryParams;

/**
 * Provides the data used for the {@link PageableQueryCollection}.
 *
 * @param <T_BEAN>
 *          the type of handled collection items.
 * @param <T_ID>
 *          the bean identifier type.
 *
 * @author olaf boede
 */
public interface PageableQueryService<T_BEAN, T_ID> extends QueryService<T_BEAN, T_ID> {

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

}
