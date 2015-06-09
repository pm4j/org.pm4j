package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.pm4j.common.pageable.querybased.idquery.IdQueryService;
import org.pm4j.common.query.QueryParams;

/**
 * This {@link IdQueryService} proxy caches a weak map of all retrieved ID-Bean pairs.
 * This prevents repeated calls of getItemForId() to the embedded service delegate.
 *
 * A use case for that proxy is a service that re-generates new DTO instances on each getItemForId() call.
 * Such DTO re-generation scenarios may lead to loosing DTO modifications.
 *
 * Another use case is an optimization. If getItemForId() is expensive, this proxy gain a performance enhancement.
 *
 * @author Olaf Boede
 */
public class IdQueryServiceProxyForMinimzedGetItemCalls<T_BEAN, T_ID> implements IdQueryService<T_BEAN, T_ID> {

  private final IdQueryService<T_BEAN, T_ID> delegate;
  private Map<T_ID, T_BEAN> idToBeanMap = new WeakHashMap<T_ID, T_BEAN>();

  public IdQueryServiceProxyForMinimzedGetItemCalls(IdQueryService<T_BEAN, T_ID> delegate) {
    this.delegate = delegate;
  }

  @Override
  public T_ID getIdForItem(T_BEAN item) {
    return delegate.getIdForItem(item);
  }

  @Override
  public T_BEAN getItemForId(T_ID id) {
    T_BEAN b = idToBeanMap.get(id);
    return b != null ? b : delegate.getItemForId(id);
  }

  @Override
  public List<T_ID> findIds(QueryParams query, long startIdx, int pageSize) {
    return delegate.findIds(query, startIdx, pageSize);
  }

  @Override
  public List<T_BEAN> getItems(List<T_ID> ids) {
    // 1. Try to get all items from the id map.
    Map<T_ID, T_BEAN> itemsInMap = new LinkedHashMap<T_ID, T_BEAN>();
    List<T_ID> idsToQuery = new ArrayList<T_ID>(ids.size());
    for (T_ID id : ids) {
      T_BEAN b = idToBeanMap.get(id);
      if (b != null) {
        itemsInMap.put(id, b);
      } else {
        idsToQuery.add(id);
      }
    }

    // 1.a: All items are already
    if (idsToQuery.isEmpty()) {
      return new ArrayList<T_BEAN>(itemsInMap.values());
    }

    // 2. Get the missing items from the service.
    List<T_BEAN> queryResultItems = delegate.getItems(idsToQuery);
    if (itemsInMap.isEmpty()) {
      for (T_BEAN item : queryResultItems) {
        idToBeanMap.put(getIdForItem(item), item);
      }
      return queryResultItems;
    } else {
      List<T_BEAN> allItems = new ArrayList<T_BEAN>(ids.size());
      Iterator<T_BEAN> queryResultIterator = queryResultItems.iterator();
      for (T_ID id : ids) {
        if (itemsInMap.containsKey(id)) {
          allItems.add(itemsInMap.get(id));
        } else {
          if (queryResultIterator.hasNext()) {
            T_BEAN next = queryResultIterator.next();
            allItems.add(next);
            idToBeanMap.put(getIdForItem(next), next);
          } else {
            // TODO olaf: not yet complete. See also TODO in IdQueryCollectionImpl.AddItemStrategyAtTheEnd.getCurrentPageIds
          }
        }
      }
      return allItems;
    }
  }

  @Override
  public long getItemCount(QueryParams query) {
    return delegate.getItemCount(query);
  }

}
