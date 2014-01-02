package org.pm4j.common.pageable.querybased.idquery;

import java.util.List;

import org.pm4j.common.pageable.QueryService;
import org.pm4j.common.query.QueryParams;

/**
 * Interface for services that provide data for a {@link PageableIdQueryCollectionImpl}.
 *
 * @author OBOEDE
 *
 * @param <T_BEAN> Supported bean type.
 * @param <T_ID> The corresponding bean identifier type.
 */
public interface PageableIdQueryService<T_BEAN, T_ID> extends QueryService<T_BEAN, T_ID> {

  /**
   * Finds a set of ID's for the given {@link QueryParams}.
   *
   * @param query
   *          The query parameter set.
   * @param startIdx
   *          The first item to fetch from the result set.
   * @param pageSize
   *          The number of items to
   * @return The list of found IDs.<br>
   *         If the given query had a sort order definition, the ID's are sorted
   *         accordingly.
   */
  List<T_ID> findIds(QueryParams query, long startIdx, int pageSize);

  /**
   * Provides items for the given ID's.
   *
   * @param ids The set of ID's. Should not be <code>null</code>.
   * @return The list of corresponding objects. Sorted according to the order of provided ID's.
   */
  List<T_BEAN> getItems(List<T_ID> ids);

}
