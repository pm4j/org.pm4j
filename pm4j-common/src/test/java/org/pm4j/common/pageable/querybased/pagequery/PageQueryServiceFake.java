package org.pm4j.common.pageable.querybased.pagequery;

import java.util.Collections;
import java.util.List;

import org.pm4j.common.pageable.querybased.QueryServiceFakeBase;
import org.pm4j.common.query.QueryParams;

/**
 * Basic in-memory test fake implementation for {@link PageQueryService}.
 *
 * @param <T_ITEM> Query result item type.
 * @param <T_ID> Query result item ID type.
 *
 * @author Olaf Boede
 */
public class PageQueryServiceFake<T_ITEM, T_ID>
    extends QueryServiceFakeBase<T_ITEM, T_ID>
    implements PageQueryService<T_ITEM, T_ID> {

  public static final String METHOD_GET_ITEMS = "getItems";

  @Override
  public List<T_ITEM> getItems(QueryParams query, long startIdx, int pageSize) {
    callCounter.incCallCount(METHOD_GET_ITEMS);
    if (startIdx >= idToBeanMap.size()) {
      return Collections.emptyList();
    }

    List<T_ITEM> allQueryResultItems = getQueryResult(query);
    int endIdx = Math.min((int) startIdx + pageSize, allQueryResultItems.size());

    List<T_ITEM> beanList = allQueryResultItems.subList((int) startIdx, endIdx);
    return beanList;
  }

  /** {@link PageQueryService} fake for result items having an {@link Integer} identifier. */
  public static class WithIntegerId<T_ITEM> extends PageQueryServiceFake<T_ITEM, Integer> {
    private int nextId = 1;

    @Override
    protected Integer makeIdForItem(T_ITEM i) {
      return nextId++;
    }
  }

  /** {@link PageQueryService} fake for result items having a {@link Long} identifier. */
  public static class WithLongId<T_ITEM> extends PageQueryServiceFake<T_ITEM, Long> {
    private long nextId = 1;

    @Override
    protected Long makeIdForItem(T_ITEM i) {
      return nextId++;
    }
  }

}
