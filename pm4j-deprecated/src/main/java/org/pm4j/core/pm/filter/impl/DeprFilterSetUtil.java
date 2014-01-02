package org.pm4j.core.pm.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.core.pm.filter.DeprFilter;
import org.pm4j.core.pm.filter.DeprFilterByDefinition;
import org.pm4j.core.pm.filter.DeprFilterItem;
import org.pm4j.core.pm.filter.DeprFilterSet;
import org.pm4j.core.pm.filter.DeprFilterable;
import org.pm4j.core.pm.filter.DeprPmFilterSet;


/**
 * Helper methods for filter set handling.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprFilterSetUtil {

  /**
   * Looks for an active {@link DeprFilterSet} within a given {@link DeprFilterable}.
   *
   * @param filterable The {@link DeprFilterable} that may have an active {@link DeprFilterSet} for the given id.
   * @param filterId The filter identifier. E.g. {@link DeprPmFilterSet#USER_FILTER_SET_ID}.
   * @return The found active {@link DeprFilterSet} or <code>null</code>.
   */
  public static DeprFilterSet findActiveFilterSet(DeprFilterable filterable, String filterId) {
    DeprFilterSet fs = null;
    DeprFilter f = filterable.getFilter(filterId);

    if (f instanceof DeprFilterSetFilter) {
      fs = ((DeprFilterSetFilter)f).getFilterSet();
    }
    return fs;
  }

  /**
   * Creates a {@link DeprFilterSet} with a define number of filter items (filter
   * condition lines).
   *
   * @param filterByDefinitions
   *          The set of filter-by options.
   * @param numOfFilterConditionLines
   *          The number of filter items to be generated.
   * @return A new {@link DeprFilterSet}.
   */
  public static DeprFilterSet makeFilterSet(Collection<? extends DeprFilterByDefinition> filterByDefinitions, int numOfFilterConditionLines) {
    DeprFilterSet fs = new DeprFilterSet();
    List<DeprFilterItem> filterItems = new ArrayList<DeprFilterItem>(numOfFilterConditionLines);
    for (int i=0; i<numOfFilterConditionLines; ++i) {
      DeprFilterItem item = new DeprFilterItem();
      item.setFilterByOptions(filterByDefinitions);
      filterItems.add(item);
    }
    fs.setFilterItems(filterItems);
    return fs;
  }

  /**
   * Creates a {@link DeprFilterSet} with a define number of filter items (filter
   * condition lines).
   * <p>
   * The first items will be contain the default values of the passed
   * {@link DeprFilterByDefinition}s.
   * <p>
   * ATTENTION: If there are more {@link DeprFilterByDefinition}s with defaults than
   * <code>numOfFilterConditionLines</code> some of these defaults will not be
   * applied!
   *
   * @param filterByDefinitions
   *          The set of filter-by options.
   * @param numOfFilterConditionLines
   *          The number of filter items to be generated.
   * @return A new {@link DeprFilterSet}.
   */
  public static DeprFilterSet makeFilterSetStartingWithDefaultConditions(Collection<? extends DeprFilterByDefinition> filterByDefinitions, int numOfFilterConditionLines) {
    List<DeprFilterByDefinition> fbdWithDefaults = new ArrayList<DeprFilterByDefinition>();
    for (DeprFilterByDefinition fbd : filterByDefinitions) {
      if (fbd.getDefaultCompOp() != null || fbd.getDefaultFilterByValue() != null) {
        fbdWithDefaults.add(fbd);
      }
    }

    DeprFilterSet fs = new DeprFilterSet();
    List<DeprFilterItem> filterItems = new ArrayList<DeprFilterItem>(numOfFilterConditionLines);
    for (int i=0; i<numOfFilterConditionLines; ++i) {
      DeprFilterItem item = new DeprFilterItem();
      item.setFilterByOptions(filterByDefinitions);
      if (i<fbdWithDefaults.size()) {
        DeprFilterByDefinition fbd = fbdWithDefaults.get(i);
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
