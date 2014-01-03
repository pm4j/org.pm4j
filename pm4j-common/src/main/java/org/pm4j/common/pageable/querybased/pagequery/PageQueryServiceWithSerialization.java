package org.pm4j.common.pageable.querybased.pagequery;

import java.io.Serializable;

import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.selection.Selection;

/**
 * A service that supports the functionality needed to serialize a {@link Selection} of a {@link PageableCollection}.
 *
 * @author olaf boede
 *
 * @param <T_BEAN>
 * @param <T_ID>
 */
public interface PageQueryServiceWithSerialization<T_BEAN, T_ID> extends PageQueryService<T_BEAN, T_ID> {

  /**
   * An interface for serializeable objects that can provide a {@link PageQueryService}.
   */
  static interface SerializeableServiceProvider extends Serializable {

    /**
     * @return a reference to the service.
     */
    PageQueryService<?, ?> getQueryService();

  }

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
  SerializeableServiceProvider getSerializeableServiceProvider();

}