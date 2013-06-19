package org.pm4j.common.pageable;

import org.pm4j.common.query.QueryOptions;
import org.pm4j.common.query.QueryParams;

public interface PageableCollectionFactory<T_ITEM> {

  PageableCollection2<T_ITEM> create(QueryOptions queryOptions, QueryParams queryParams);

}
