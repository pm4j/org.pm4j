package org.pm4j.common.pageable;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;

/**
 *
 * @author oboede
 *
 * @param <T_ITEM>
 * @deprecated Please use {@link PmTableCfg2#valuePath()} or override <code>PmTableImpl2#getPmBeans()</code> instead.
 */
public interface PageableCollectionFactory<T_ITEM> {

  PageableCollection2<T_ITEM> create(QueryOptions queryOptions, QueryParams queryParams);

}
