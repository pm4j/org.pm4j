package org.pm4j.deprecated.core.pm.filter.impl;

import org.pm4j.deprecated.core.pm.filter.DeprFilter;
import org.pm4j.deprecated.core.pm.filter.DeprFilterItem;

/**
 * A filter that works based on restrictions defined by a {@link DeprFilterItem}.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprFilterItemFilter implements DeprFilter {

  private final DeprFilterItem filterItem;
  private final boolean passThrough;

  public DeprFilterItemFilter(DeprFilterItem filterItem) {
    this.filterItem = filterItem;
    this.passThrough =  filterItem.getFilterBy() == null ||
                        !filterItem.isEffective();
  }

  @Override
  public boolean doesItemMatch(Object item) {
    return  passThrough ||
            filterItem.getFilterBy().doesItemMatch(item, filterItem.getCompOp(), filterItem.getFilterByValue());
  }

  @Override
  public boolean isBeanFilter() {
    return false;
  }

}
