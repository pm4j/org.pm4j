package org.pm4j.common.pageable.querybased.idquery;

import java.util.List;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;

public class PageableIdServiceDaoBased<T_BEAN, T_ID> implements PageableIdQueryService<T_BEAN, T_ID> {

  private PageableIdQueryDao<T_BEAN, T_ID> dao;

  public PageableIdServiceDaoBased(PageableIdQueryDao<T_BEAN, T_ID> dao) {
    this.dao = dao;
  }

  @Override
  public List<T_ID> findIds(QueryParams query, long startIdx, int pageSize) {
    return dao.findIds(query, startIdx, pageSize);
  }

  @Override
  public List<T_BEAN> getItems(List<T_ID> ids) {
    return dao.getItems(ids);
  }

  @Override
  public long getItemCount(QueryParams query) {
    return dao.getItemCount(query);
  }

  @Override
  public T_ID getIdForItem(T_BEAN item) {
    return dao.getIdForItem(item);
  }

  @Override
  public T_BEAN getItemForId(T_ID id) {
    return dao.getItemForId(id);
  }

  @Override
  public QueryOptions getQueryOptions() {
    return null;
  }

}
