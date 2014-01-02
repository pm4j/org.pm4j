package org.pm4j.common.pageable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.pageable.QueryService;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.util.CallCounter;

/**
 * A DAO fake that works in memory.
 *
 * @param <T_ITEM>
 * @param <T_ID>
 *
 * @author olaf boede
 */
public abstract class QueryServiceFakeBase<T_ITEM, T_ID> implements QueryService<T_ITEM, T_ID> {

  public static final String METHOD_GET_ITEM_FOR_ID = "getItemForId";

  protected final Map<T_ID, T_ITEM> idToBeanMap = new LinkedHashMap<T_ID, T_ITEM>();
  public final CallCounter callCounter = new CallCounter();
  private InMemQueryEvaluator<T_ITEM> queryEvaluator = new InMemQueryEvaluator<T_ITEM>();

  public void setQueryEvaluator(InMemQueryEvaluator<T_ITEM> queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }


  @Override
  public abstract T_ID getIdForItem(T_ITEM item);


  @Override
  public T_ITEM getItemForId(T_ID id) {
    callCounter.incCallCount(METHOD_GET_ITEM_FOR_ID);
    return idToBeanMap.get(id);
  }

  public T_ITEM addFakeItem(T_ITEM item) {
    T_ID id = getIdForItem(item);
    idToBeanMap.put(id, item);
    return item;
  }

  public void removeAllFakeItems() {
    idToBeanMap.clear();
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
