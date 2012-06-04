package org.pm4j.core.pm.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.FilterItem;
import org.pm4j.core.pm.filter.FilterSet;


/**
 * Helper methods for filter set handling.
 *
 * @author olaf boede
 */
public class FilterSetUtil {

  /**
   * Creates a {@link FilterSet} with a define number of filter items (filter
   * condition lines).
   *
   * @param filterByDefinitions
   *          The set of filter-by options.
   * @param numOfFilterConditionLines
   *          The number of filter items to be generated.
   * @return A new {@link FilterSet}.
   */
  public static FilterSet makeFilterSet(Collection<? extends FilterByDefinition> filterByDefinitions, int numOfFilterConditionLines) {
    FilterSet fs = new FilterSet();
    List<FilterItem> filterItems = new ArrayList<FilterItem>(numOfFilterConditionLines);
    for (int i=0; i<numOfFilterConditionLines; ++i) {
      FilterItem item = new FilterItem();
      item.setFilterByOptions(filterByDefinitions);
      filterItems.add(item);
    }
    fs.setFilterItems(filterItems);
    return fs;
  }

}
