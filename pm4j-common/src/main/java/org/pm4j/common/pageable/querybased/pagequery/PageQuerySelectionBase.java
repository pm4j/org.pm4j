package org.pm4j.common.pageable.querybased.pagequery;

import org.pm4j.common.pageable.querybased.QuerySelectionBase;
import org.pm4j.common.pageable.querybased.QueryService;

/**
 * A selection base class that supports serializable selections.
 */
public abstract class PageQuerySelectionBase<T_ITEM, T_ID> extends QuerySelectionBase<T_ITEM, T_ID> {
  private static final long serialVersionUID = 1L;

  public PageQuerySelectionBase(QueryService<T_ITEM, T_ID> service) {
    super(getBaseService(service));
  }

  private static <T_ITEM, T_ID> QueryService<T_ITEM, T_ID> getBaseService(QueryService<T_ITEM, T_ID> s) {
    return (s instanceof CachingPageQueryService)
        ? ((CachingPageQueryService<T_ITEM, T_ID>)s).getBaseService()
        : s;
  }
}