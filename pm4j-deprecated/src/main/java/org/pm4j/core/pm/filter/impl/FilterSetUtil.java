package org.pm4j.core.pm.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.filter.Filter;
import org.pm4j.core.pm.filter.FilterByDefinition;
import org.pm4j.core.pm.filter.FilterItem;
import org.pm4j.core.pm.filter.FilterSet;
import org.pm4j.core.pm.filter.Filterable;
import org.pm4j.core.pm.filter.PmFilterSet;


/**
 * Helper methods for filter set handling.
 *
 * @author olaf boede
 */
public class FilterSetUtil {

  /**
   * Looks for an active {@link FilterSet} within a given {@link Filterable}.
   *
   * @param filterable The {@link Filterable} that may have an active {@link FilterSet} for the given id.
   * @param filterId The filter identifier. E.g. {@link PmFilterSet#USER_FILTER_SET_ID}.
   * @return The found active {@link FilterSet} or <code>null</code>.
   */
  public static FilterSet findActiveFilterSet(Filterable filterable, String filterId) {
    FilterSet fs = null;
    Filter f = filterable.getFilter(filterId);

    if (f instanceof FilterSetFilter) {
      fs = ((FilterSetFilter)f).getFilterSet();
    }
    return fs;
  }

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

  /**
   * Creates a {@link FilterSet} with a define number of filter items (filter
   * condition lines).
   * <p>
   * The first items will be contain the default values of the passed
   * {@link FilterByDefinition}s.
   * <p>
   * ATTENTION: If there are more {@link FilterByDefinition}s with defaults than
   * <code>numOfFilterConditionLines</code> some of these defaults will not be
   * applied!
   *
   * @param filterByDefinitions
   *          The set of filter-by options.
   * @param numOfFilterConditionLines
   *          The number of filter items to be generated.
   * @return A new {@link FilterSet}.
   */
  public static FilterSet makeFilterSetStartingWithDefaultConditions(Collection<? extends FilterByDefinition> filterByDefinitions, int numOfFilterConditionLines) {
    List<FilterByDefinition> fbdWithDefaults = new ArrayList<FilterByDefinition>();
    for (FilterByDefinition fbd : filterByDefinitions) {
      if (fbd.getDefaultCompOp() != null || fbd.getDefaultFilterByValue() != null) {
        fbdWithDefaults.add(fbd);
      }
    }

    FilterSet fs = new FilterSet();
    List<FilterItem> filterItems = new ArrayList<FilterItem>(numOfFilterConditionLines);
    for (int i=0; i<numOfFilterConditionLines; ++i) {
      FilterItem item = new FilterItem();
      item.setFilterByOptions(filterByDefinitions);
      if (i<fbdWithDefaults.size()) {
        FilterByDefinition fbd = fbdWithDefaults.get(i);
        item.setFilterBy(fbd);
        item.setCompOp(fbd.getDefaultCompOp());
        item.setFilterByValue(fbd.getDefaultFilterByValue());
      }
      filterItems.add(item);
    }
    fs.setFilterItems(filterItems);
    return fs;
  }
}
