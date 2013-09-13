package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.pageable.querybased.ItemIdDaoFakeBase;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;

public abstract class PageableIdQueryDaoFakeBase<T_ITEM, T_ID> extends ItemIdDaoFakeBase<T_ITEM, T_ID> implements PageableIdQueryDao<T_ITEM, T_ID> {

  public static final String METHOD_FIND_IDS = "findIds";
  public static final String METHOD_GET_ITEMS = "getItems";
  public static final String METHOD_GET_ITEM_COUNT = "getItemCount";

  private InMemQueryEvaluator<T_ITEM> queryEvaluator = new InMemQueryEvaluator<T_ITEM>();

  @Override
  public List<T_ID> findIds(QueryParams query, long startIdx, int pageSize) {
    callCounter.incCallCount(METHOD_FIND_IDS);
    if (startIdx >= idToBeanMap.size()) {
      return Collections.emptyList();
    }

    List<T_ITEM> allQueryResultItems = getQueryResult(query);
    int endIdx = Math.min((int) startIdx + pageSize, allQueryResultItems.size());

    List<T_ITEM> beanList = allQueryResultItems.subList((int) startIdx, endIdx);
    List<T_ID> idList = new ArrayList<T_ID>();
    for (T_ITEM b : beanList) {
      idList.add(getIdForItem(b));
    }
    return idList;
  }

  @Override
  public List<T_ITEM> getItems(List<T_ID> ids) {
    callCounter.incCallCount(METHOD_GET_ITEMS);
    List<T_ITEM> beans = new ArrayList<T_ITEM>(ids.size());
    for (T_ID id : ids) {
      beans.add(idToBeanMap.get(id));
    }
    return beans;
  }

  @Override
  public long getItemCount(QueryParams query) {
    callCounter.incCallCount(METHOD_GET_ITEM_COUNT);
    return getQueryResult(query).size();
  }

  protected List<T_ITEM> getQueryResult(QueryParams query) {
    List<T_ITEM> beans = getQueryEvaluator().sort(idToBeanMap.values(), query.getEffectiveSortOrder());

    if (query.getFilterExpression() != null) {
      beans = getQueryEvaluator().evaluateSubSet(beans, query.getFilterExpression());
    }

    return beans;
  }

  protected InMemQueryEvaluator<T_ITEM> getQueryEvaluator() {
    return queryEvaluator;
  }

}
