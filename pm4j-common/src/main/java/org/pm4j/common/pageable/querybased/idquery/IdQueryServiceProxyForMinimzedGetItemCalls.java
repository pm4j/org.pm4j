package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
    if (b == null) {
      b = delegate.getItemForId(id);
      idToBeanMap.put(id, b);
    }
    return b;
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

    // 1.a: All items are already loaded.
    if (idsToQuery.isEmpty()) {
      return new ArrayList<T_BEAN>(itemsInMap.values());
    }

    // 2. Get the missing items from the service.
    List<T_BEAN> queryResultItems = delegate.getItems(idsToQuery);
    if (itemsInMap.isEmpty()) {
      // 2.a: No items loaded: Add them to the map and return the item set.
      for (T_BEAN item : queryResultItems) {
        idToBeanMap.put(getIdForItem(item), item);
      }
      return queryResultItems;
    } else {
      // 2.b: Some items are already in memory.
      //      The in-memory instance will be used in favor to the service provided instance.
      //      This preserves any changed done in the already loaded instance.
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
            // Not found items are just gaps. Possibly object deleted by other users.
            // They will be handled by the logic of the caller (pageable collection).
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

  /**
   * Clears the internal hash map.<p>
   * May be used to force reloading of all items again.
   */
  public void clearWeakMap() {
    idToBeanMap.clear();
  }

}
