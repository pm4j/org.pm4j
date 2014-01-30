package org.pm4j.common.pageable.querybased.pagequery;

import java.io.Serializable;

import org.pm4j.common.pageable.querybased.QueryService;
import org.pm4j.common.pageable.querybased.QueryServiceSerializationSupport;
import org.pm4j.common.selection.Selection;

/**
 * A selection base class that supports serializable selections.
 */
public abstract class PageQuerySelectionBase<T_ITEM, T_ID> implements Selection<T_ITEM>, Serializable {
  private static final long serialVersionUID = 1L;

  /** The service provider may be <code>null</code> in case of non-serializeable selections. */
  private QueryServiceSerializationSupport.SerializeableServiceProvider serviceProvider;
  transient private QueryService<T_ITEM, T_ID> service;

  public PageQuerySelectionBase(QueryService<T_ITEM, T_ID> service) {
    assert service != null;
    this.service = service;

    // find the (optionally embedded) serializeable service provider.
    QueryService<T_ITEM, T_ID> s = service;
    if (s instanceof CachingPageQueryService) {
      s = ((CachingPageQueryService<T_ITEM, T_ID>)service).getBaseService();
    }
    if (s instanceof QueryServiceSerializationSupport) {
      this.serviceProvider = ((QueryServiceSerializationSupport)s).getSerializeableServiceProvider();
    }
  }

  @Override
  public boolean isEmpty() {
    return getSize() == 0L;
  }

  @SuppressWarnings("unchecked")
  protected QueryService<T_ITEM, T_ID> getService() {
    if (service == null) {
      if (serviceProvider != null) {
        service = (PageQueryService<T_ITEM, T_ID>) serviceProvider.getQueryService();
      }

      if (service == null) {
        throw new RuntimeException("Your PageableQueryService does not support serialization of selections.\n" +
            "Please implement PageableQueryServiceWithSerialization.getSerializeableServiceProvider() to get serializeable selections.");
      }
    }
    return service;
  }

}