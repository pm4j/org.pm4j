package org.pm4j.common.pageable.inmem;

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

}
