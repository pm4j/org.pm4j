package org.pm4j.common.pageable.querybased;

import java.io.Serializable;

import org.pm4j.common.selection.Selection;

/**
 * A selection base class that supports serializable selections.
 */
public abstract class PageableQuerySelectionBase<T_ITEM, T_ID extends Serializable> implements Selection<T_ITEM>, Serializable {
  private static final long serialVersionUID = 1L;

  /** The service provider may be <code>null</code> in case of non-serializeable selections. */
  private PageableQueryService.SerializeableServiceProvider<T_ITEM, T_ID> serviceProvider;
  transient private PageableQueryService<T_ITEM, T_ID> service;

  public PageableQuerySelectionBase(PageableQueryService<T_ITEM, T_ID> service) {
    assert service != null;

    this.service = service;
    this.serviceProvider = service.getSerializeableServiceProvider();
  }

  @Override
  public boolean isEmpty() {
    return getSize() == 0L;
  }

  protected PageableQueryService<T_ITEM, T_ID> getService() {
    if (service == null) {
      if (serviceProvider != null) {
        service = serviceProvider.getQueryService();
      }

      if (service == null) {
        throw new RuntimeException("Your PageableQueryService does not support serialization of selections.\n" +
            "Please implement PageableQueryService.getSerializeableServiceProvider() to get serializeable selections.");
      }
    }
    return service;
  }

}