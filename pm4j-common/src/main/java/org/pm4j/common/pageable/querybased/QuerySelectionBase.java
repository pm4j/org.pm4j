package org.pm4j.common.pageable.querybased;

import java.io.Serializable;

import org.pm4j.common.pageable.querybased.QueryServiceSerializationSupport.SerializeableServiceProvider;
import org.pm4j.common.selection.Selection;

/**
 * A selection base class that supports serializable selections.
 * <p>
 * TODO oboede:<br>
 * . support {@link Serializable} services
 * . check service serializibility on serialization.
 *
 * @author Olaf Boede
 */
public abstract class QuerySelectionBase<T_ITEM, T_ID> implements Selection<T_ITEM>, Serializable {
  private static final long serialVersionUID = 1L;

  /** The service provider may be <code>null</code> in case of non-serializeable selections. */
  private QueryServiceSerializationSupport.SerializeableServiceProvider serviceProvider;

  /** Reference to the service that provides the selected objects.
   * Is transient because most services are not serializable. */
  transient private QueryService<T_ITEM, T_ID> service;

  /**
   * Initalizes the service used to retrieve the selected objects.
   * If the service implements {@link QueryServiceSerializationSupport}, this selection may be serialized.
   *
   * @param service The service that provides the selected objects. May not be <code>null</code>.
   */
  public QuerySelectionBase(QueryService<T_ITEM, T_ID> service) {
    assert service != null;
    this.service = service;

    // find the (optionally embedded) serializeable service provider.
    QueryService<T_ITEM, T_ID> s = service;
    if (s instanceof QueryServiceSerializationSupport) {
      this.serviceProvider = ((QueryServiceSerializationSupport)s).getSerializeableServiceProvider();
    }
  }

  @Override
  public boolean isEmpty() {
    return getSize() == 0L;
  }

  /**
   * Provides the service reference.<br>
   * After a de-serialization this reference may be <code>null</code>.
   * In this case a {@link SerializeableServiceProvider} my restore the service reference on the fly.
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  protected QueryService<T_ITEM, T_ID> getService() {
    if (service == null) {
      if (serviceProvider != null) {
        service = (QueryService<T_ITEM, T_ID>) serviceProvider.getQueryService();
      }

      if (service == null) {
        throw new RuntimeException("Your QueryService does not support serialization of selections.\n" +
            "Please add the service interface QueryServiceSerializationSupport to get serializeable selections.");
      }
    }
    return service;
  }

}