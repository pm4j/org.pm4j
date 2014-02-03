package org.pm4j.common.pageable.querybased.pagequery;

import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.querybased.QueryServiceSerializationSupport;
import org.pm4j.common.selection.Selection;

/**
 * A service that supports the functionality needed to serialize a {@link Selection} of a {@link PageableCollection}.
 *
 * @author olaf boede
 *
 * @param <T_BEAN>
 * @param <T_ID>
 *
 * @deprecated Please combine your query service interface with {@link QueryServiceSerializationSupport}.
 */
@Deprecated
public interface PageQueryServiceWithSerialization<T_BEAN, T_ID> extends QueryServiceSerializationSupport, PageQueryService<T_BEAN, T_ID> {

  /**
   * @deprecated Please use {@link QueryServiceSerializationSupport#SerializeableServiceProvider}
   */
  static interface SerializeableServiceProvider extends QueryServiceSerializationSupport.SerializeableServiceProvider2 {
  }

}