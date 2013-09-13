package org.pm4j.common.pageable.querybased;

import java.util.LinkedHashMap;
import java.util.Map;

import org.pm4j.common.pageable.ItemIdDao;
import org.pm4j.common.util.CallCounter;

public abstract class ItemIdDaoFakeBase<T_ITEM, T_ID> implements ItemIdDao<T_ITEM, T_ID> {

  public static final String METHOD_GET_ITEM_FOR_ID = "getItemForId";

  protected final Map<T_ID, T_ITEM> idToBeanMap = new LinkedHashMap<T_ID, T_ITEM>();
  protected final CallCounter callCounter = new CallCounter();

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

}
