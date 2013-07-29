package org.pm4j.core.pm.filter.impl;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.filter.CombinedBy;
import org.pm4j.core.pm.filter.Filter;
import org.pm4j.core.pm.filter.FilterItem;
import org.pm4j.core.pm.filter.FilterSet;

/**
 * A filter that works based on restrictions defined by a {@link FilterSet}.
 *
 * @author olaf boede
 */
public class FilterSetFilter implements Filter {

  private final FilterSet filterSet;
  private final FilterItem[] activeFilterItems;
  private final CombinedBy combinedBy;


  public FilterSetFilter(FilterSet filterSet) {
    this.filterSet = filterSet;
    List<FilterItem> list = new ArrayList<FilterItem>();
    for (FilterItem i : filterSet.getFilterItems()) {
      if (i.isEffective()) {
        list.add(i);
      }
    }
    activeFilterItems = list.toArray(new FilterItem[list.size()]);
    combinedBy = filterSet.getCombindedBy();
  }

  @Override
  public boolean doesItemMatch(Object item) {

    // No filtering if there are no filter items.
    if (activeFilterItems.length == 0) {
      return true;
    }

    for (FilterItem i : activeFilterItems) {
      boolean match = new FilterItemFilter(i).doesItemMatch(item);
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
    return combinedBy == CombinedBy.AND;
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

  public FilterSet getFilterSet() {
    return filterSet;
  }

}
