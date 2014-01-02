package org.pm4j.core.pm.filter.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.filter.DeprCombinedBy;
import org.pm4j.core.pm.filter.DeprFilter;
import org.pm4j.core.pm.filter.DeprFilterItem;
import org.pm4j.core.pm.filter.DeprFilterSet;

/**
 * A filter that works based on restrictions defined by a {@link DeprFilterSet}.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprFilterSetFilter implements DeprFilter {

  private final DeprFilterSet filterSet;
  private final DeprFilterItem[] activeFilterItems;
  private final DeprCombinedBy combinedBy;


  public DeprFilterSetFilter(DeprFilterSet filterSet) {
    this.filterSet = filterSet;
    List<DeprFilterItem> list = new ArrayList<DeprFilterItem>();
    for (DeprFilterItem i : filterSet.getFilterItems()) {
      if (i.isEffective()) {
        list.add(i);
      }
    }
    activeFilterItems = list.toArray(new DeprFilterItem[list.size()]);
    combinedBy = filterSet.getCombindedBy();
  }

  @Override
  public boolean doesItemMatch(Object item) {

    // No filtering if there are no filter items.
    if (activeFilterItems.length == 0) {
      return true;
    }

    for (DeprFilterItem i : activeFilterItems) {
      boolean match = new DeprFilterItemFilter(i).doesItemMatch(item);
      switch (combinedBy) {
        case AND:
          if (!match) {
            return false;
          }
          break;
        case OR:
          if (match) {
            return true;
          }
          break;
        default:
          throw new IllegalArgumentException("Unknown coditionLogOp: " + combinedBy);
      }
    }

    // In case of AND all checks must be passed.
    // In case of OR this code location indicates that all checks did fail.
    return combinedBy == DeprCombinedBy.AND;
  }

  @Override
  public boolean isBeanFilter() {
    return false;
  }

  /**
   * Returns <code>true</code> if the filter contains some definitions that really may filter.
   *
   * @return <code>true</code> if it makes sense to apply this filter.
   */
  public boolean isEffective() {
    return activeFilterItems.length > 0;
  }

  public DeprFilterSet getFilterSet() {
    return filterSet;
  }

}
