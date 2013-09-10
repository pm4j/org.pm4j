package org.pm4j.common.pageable.idservicebased;

import java.util.List;

import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.ItemIdConverter;

/**
 * Interface for DAOs that provide data for a {@link PageableIdCollectionImpl}.
 *
 * @author OBOEDE
 *
 * @param <T_BEAN> Supported bean type.
 * @param <T_ID> The corresponding bean identifier type.
 */
public interface PageableIdDao<T_BEAN, T_ID> extends ItemIdConverter<T_BEAN, T_ID>{

  List<T_ID> findIds(QueryParams query, long startIdx, int pageSize);

  List<T_BEAN> getItems(List<T_ID> ids);

  /**
   * Provides the number of items that match the given query filter criteria.
   *
   * @param query
   * @return
   */
  long getItemCount(QueryParams query);

}
