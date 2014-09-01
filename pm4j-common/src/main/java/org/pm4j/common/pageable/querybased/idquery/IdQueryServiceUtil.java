package org.pm4j.common.pageable.querybased.idquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Utility methods for {@link IdQueryService}.
 *
 * @author Olaf Boede
 */
public class IdQueryServiceUtil {

  /**
   * Applies the order of the given id list to the given item list.
   *
   * @param items The unsorted item list.
   * @param ids   An ID list that defines the sort order.
   * @return The sorted item list.<br>
   *         The list size is the same as of parameter <code>items</code>.
   */
  public static <T_ITEM, T_ID> List<T_ITEM> sortByIdList(IdQueryService<T_ITEM, T_ID> service, List<T_ITEM> items, List<T_ID> ids) {
    return sortByIdList(service, items, ids, false);
  }

  /**
   * Applies the order of the given id list to the given item list.
   *
   * @param items The unsorted item list.
   * @param ids   An ID list that defines the sort order.
   * @return The sorted item list.<br>
   *         The list size is the same as of parameter <code>ids</code>.
   *         If there was no item for an ID a list position having a <code>null</code> value will be provided.
   */
  public static <T_ITEM, T_ID> List<T_ITEM> sortByIdListWithGaps(IdQueryService<T_ITEM, T_ID> service, List<T_ITEM> items, List<T_ID> ids) {
    return sortByIdList(service, items, ids, true);
  }

  private static <T_ITEM, T_ID> List<T_ITEM> sortByIdList(IdQueryService<T_ITEM, T_ID> service, List<T_ITEM> items, List<T_ID> ids, boolean withGaps) {
    Map<T_ID, T_ITEM> map = new HashMap<T_ID, T_ITEM>();
    for (T_ITEM item : items) {
      if (item != null) {
        T_ID id = service.getIdForItem(item);
        map.put(id, item);
      }
    }

    List<T_ITEM> sortedResult = new ArrayList<T_ITEM>();
    for (T_ID id : ids) {
      T_ITEM item = map.get(id);
      if (item != null || withGaps) {
        sortedResult.add(item);
      }
    }

    return sortedResult;
  }

}
