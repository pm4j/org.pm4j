package org.pm4j.common.pageable.inmem;

import java.util.Comparator;

import org.pm4j.common.pageable.PageableCollection2;

/**
 * In memory specific pageable collection extension.<br>
 * Allows to use a compare operator.
 *
 * @param <T_ITEM> type of handled collection items.
 *
 * @author olaf boede
 */
public interface PageableInMemCollection<T_ITEM> extends PageableCollection2<T_ITEM> {

  /**
   * Changes a sort order based on the given comparator.
   *
   * @param comparator the new comparator. May be <code>null</code> to switch back to the default sort order.
   */
  void setSortOrderComparator(Comparator<T_ITEM> comparator);

}
