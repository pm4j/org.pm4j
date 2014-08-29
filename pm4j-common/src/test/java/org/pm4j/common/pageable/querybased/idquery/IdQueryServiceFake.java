package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.pageable.querybased.QueryServiceFakeBase;
import org.pm4j.common.query.QueryParams;

/**
 * A pageable ID service fake that works in memory.
 *
 * @param <T_ITEM> Query result item type.
 * @param <T_ID> Query result item ID type.
 *
 * @author Olaf Boede
 */
public class IdQueryServiceFake<T_ITEM, T_ID>
    extends QueryServiceFakeBase<T_ITEM, T_ID>
    implements IdQueryService<T_ITEM, T_ID> {

  public static final String METHOD_FIND_IDS = "findIds";
  public static final String METHOD_GET_ITEMS = "getItems";


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


  /** {@link IdQueryService} fake for result items having an {@link Integer} identifier. */
  public static class WithIntegerId<T_ITEM> extends IdQueryServiceFake<T_ITEM, Integer> {
    private int nextId = 1;

    @Override
    protected Integer makeIdForItem(T_ITEM i) {
      return nextId++;
    }
  }

  /** {@link IdQueryService} fake for result items having a {@link Long} identifier. */
  public static class WithLongId<T_ITEM> extends IdQueryServiceFake<T_ITEM, Long> {
    private long nextId = 1;

    @Override
    protected Long makeIdForItem(T_ITEM i) {
      return nextId++;
    }
  }
}
