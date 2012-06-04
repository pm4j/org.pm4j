package org.pm4j.core.pm.filter.impl;

import org.pm4j.core.pm.filter.Filter;
import org.pm4j.core.pm.filter.FilterItem;

/**
 * A filter that works based on restrictions defined by a {@link FilterItem}.
 *
 * @author olaf boede
 */
public class FilterItemFilter implements Filter {

  private final FilterItem filterItem;
  private final boolean passThrough;

  public FilterItemFilter(FilterItem filterItem) {
    this.filterItem = filterItem;
    this.passThrough =  filterItem.getFilterBy() == null ||
                        !filterItem.isEffective();
  }

  @Override
  public boolean doesItemMatch(Object item) {
    return  passThrough ||
            filterItem.getFilterBy().doesItemMatch(item, filterItem.getCompOp(), filterItem.getFilterByValue());
  }

}
