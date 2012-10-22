package org.pm4j.common.pageable.querybased;

import java.io.Serializable;
import java.util.List;

import org.pm4j.common.query.Query;
import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.selection.ItemIdConverter;

/**
 * Provides the services used for the {@link PageableQueryCollection}.
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
  List<T_BEAN> getItems(Query query, long startIdx, int pageSize);

  /**
   * Provides the number of items that match the given query filter criteria.
   *
   * @param query
   * @return
   */
  long getItemCount(Query query);

  /**
   * Provides the total number of unfiltered items.
   * <p>
   * Is used for some UI's that display the total number of items beside the number of filterd items.
   *
   * @param query
   * @return
   */
  long getUnfilteredItemCount(Query query);

  /**
   * Provides the set of filter definitions and attribute sort orders that can be
   * processed by this service.
   *
   * @return
   */
  QueryOptions getQueryOptions();

  /**
   * Provides a serializable instance that is able to deliver a reference to this service.
   * <p>
   * This is needed to support serializeable selections.
   * <p>
   * If serialization of selections is not needed this method may simply return <code>null</code>.<br>
   * In this case an attempt to serialize a selection will throw an exception that reports that
   * you should implement this method to get that feature...
   *
   * @return
   */
  SerializeableServiceProvider<T_BEAN, T_ID> getSerializeableServiceProvider();

  /**
   * An interface for serializeable objects that can provide a {@link PageableQueryService}.
   *
   * @param <T_BEAN> see {@link PageableQueryService}.
   * @param <T_ID>   see {@link PageableQueryService}.
   */
  static interface SerializeableServiceProvider<T_BEAN, T_ID> extends Serializable {

    /**
     * @return a reference to the service.
     */
    PageableQueryService<T_BEAN, T_ID> getQueryService();

  }
}
