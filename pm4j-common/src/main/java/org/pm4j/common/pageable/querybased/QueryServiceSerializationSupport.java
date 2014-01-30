package org.pm4j.common.pageable.querybased;

import java.io.Serializable;

import org.pm4j.common.pageable.PageableCollection;
import org.pm4j.common.pageable.querybased.pagequery.PageQueryService;
import org.pm4j.common.selection.Selection;

/**
 * A interface for {@link QueryService}es that support the functionality needed to serialize a {@link Selection} of a {@link PageableCollection}.
 *
 * @author Olaf Boede
 */
public interface QueryServiceSerializationSupport {
  /**
   * An interface for serializeable objects that can provide a {@link PageQueryService}.
   */
  static interface SerializeableServiceProvider extends Serializable {

    /**
     * @return a reference to the service.
     */
    QueryService<?, ?> getQueryService();

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
