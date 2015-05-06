package org.pm4j.common.pageable.querybased;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.crud.CrudService;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.query.inmem.InMemQueryEvaluator;
import org.pm4j.common.util.CallCounter;
import org.pm4j.common.util.reflection.BeanAttrAccessor;
import org.pm4j.common.util.reflection.BeanAttrAccessorImpl;

/**
 * A DAO fake that works in memory.
 *
 * @param <T_ITEM> Query result item type.
 * @param <T_ID> Query result item ID type.
 *
 * @author Olaf Boede
 */
public abstract class QueryServiceFakeBase<T_ITEM, T_ID>
    implements QueryService<T_ITEM, T_ID>,
               CrudService<T_ITEM, T_ID> {

  public static final String METHOD_GET_ITEM_FOR_ID = "getItemForId";
  public static final String METHOD_GET_ITEM_COUNT = "getItemCount";

  protected final Map<T_ID, T_ITEM> idToBeanMap = new LinkedHashMap<T_ID, T_ITEM>();
  public final CallCounter callCounter = new CallCounter();
  private InMemQueryEvaluator<T_ITEM> queryEvaluator = new InMemQueryEvaluator<T_ITEM>();
  private String idAttrName = "id";
  private BeanAttrAccessor idAttrAccessor;

  public void setQueryEvaluator(InMemQueryEvaluator<T_ITEM> queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }

  /**
   * The default implementation tries to find and use a getId() method signature via reflection.
   * <p>
   * There is no call counter placed for this method by default, because it seems to be not
   * performance relevant.
   */
  @Override
  public T_ID getIdForItem(T_ITEM item) {
    try {
      return getIdAttrAccessor(item).getBeanAttrValue(item);
    } catch (RuntimeException e1) {
        throw new RuntimeException("Please check if your query result item has a matching getId method. Alternatively you may override this method.", e1);
    }
  }

  @Override
  public T_ITEM getItemForId(T_ID id) {
    callCounter.incCallCount(METHOD_GET_ITEM_FOR_ID);
    return idToBeanMap.get(id);
  }

  @Override
  public long getItemCount(QueryParams query) {
    callCounter.incCallCount(METHOD_GET_ITEM_COUNT);
    return getQueryResult(query).size();
  }

  @Override
  public T_ITEM save(T_ITEM item) {
    T_ID id = getIdForItem(item);
    if (id == null) {
      id = makeIdForItem(item);
      setIdForItem(item, id);
    }
    idToBeanMap.put(id, item);
    return item;
  }

  /** Is not abstract because we don't want to force users that don't use the CRUD part to implement that for nothing. */
  protected T_ID makeIdForItem(T_ITEM item) {
    throw new RuntimeException("Please implement makeIdForItem()");
  }

  protected void setIdForItem(T_ITEM item, T_ID id) {
    getIdAttrAccessor(item).setBeanAttrValue(item, id);
  }

  @Override
  public void delete(T_ITEM entity) {
    T_ID id = getIdForItem(entity);
    idToBeanMap.remove(id);
  }

  public void deleteAll() {
    idToBeanMap.clear();
  }

  @Override
  public T_ITEM findById(T_ID id) {
    return idToBeanMap.get(id);
  }

  protected List<T_ITEM> getQueryResult(QueryParams query) {
    List<T_ITEM> beans = getQueryEvaluator().sort(idToBeanMap.values(), query.getEffectiveSortOrder());

    if (query.getQueryExpression() != null) {
      beans = getQueryEvaluator().evaluateSubSet(beans, query.getQueryExpression());
    }

    return beans;
  }

  protected InMemQueryEvaluator<T_ITEM> getQueryEvaluator() {
    return queryEvaluator;
  }

  /**
   * @param idAttrName the idAttrName to set
   */
  public void setIdAttrName(String idAttrName) {
    this.idAttrName = idAttrName;
    idAttrAccessor = null;
  }

  private BeanAttrAccessor getIdAttrAccessor(T_ITEM item) {
    if (idAttrAccessor == null) {
      idAttrAccessor = new BeanAttrAccessorImpl(item.getClass(), idAttrName, false);
    }
    return idAttrAccessor;
  }

}
