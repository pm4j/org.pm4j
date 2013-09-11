package org.pm4j.common.pageable.querybased.idquery;

import org.pm4j.common.query.QueryOptions;

/**
 * Interface for services that provide data for a {@link PageableIdQueryCollectionImpl}.
 *
 * @author OBOEDE
 *
 * @param <T_BEAN> Supported bean type.
 * @param <T_ID> The corresponding bean identifier type.
 */
public interface PageableIdQueryService<T_BEAN, T_ID> extends PageableIdQueryDao<T_BEAN, T_ID> {

  /**
   * Provides the set of filter definitions and attribute sort orders that can be
   * processed by this service.
   *
   * @return
   */
  QueryOptions getQueryOptions();

}