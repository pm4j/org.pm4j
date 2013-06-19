package org.pm4j.common.pageable.inmem;

import java.util.Collection;

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
   * Provides the backing collection this pageable instance works on.
   *
   * @return the collection. Should not be <code>null</code>.
   */
  Collection<T_ITEM> getBackingCollection();

}
