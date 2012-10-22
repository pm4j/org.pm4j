package org.pm4j.common.pageable.inmem;

import java.util.Comparator;

import org.pm4j.common.pageable.PageableCollection2;

public interface PageableInMemCollection<T_ITEM> extends PageableCollection2<T_ITEM> {

  void setSortOrderComparator(Comparator<T_ITEM> comparator);

}
