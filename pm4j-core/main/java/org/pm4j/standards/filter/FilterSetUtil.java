package org.pm4j.standards.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.query.FilterCompareDefinition;


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
   * @param filterCompareDefinitions
   *          The set of filter-by options.
   * @param numOfFilterConditionLines
   *          The number of filter items to be generated.
   * @return A new {@link FilterSet}.
   */
  public static FilterSet makeFilterSet(Collection<? extends FilterCompareDefinition> filterCompareDefinitions, int numOfFilterConditionLines) {
    FilterSet fs = new FilterSet();
    for (int i=0; i<numOfFilterConditionLines; ++i) {
      FilterItem item = new FilterItem();
      item.setFilterByOptions(filterCompareDefinitions);
      fs.addFilterItem(item);
    }
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
  public static FilterSet makeFilterSetStartingWithDefaultConditions(Collection<? extends FilterCompareDefinition> filterByDefinitions, int numOfFilterConditionLines) {
    List<FilterCompareDefinition> fbdWithDefaults = new ArrayList<FilterCompareDefinition>();
    for (FilterCompareDefinition fbd : filterByDefinitions) {
      if (fbd.getDefaultCompOp() != null || fbd.getDefaultFilterByValue() != null) {
        fbdWithDefaults.add(fbd);
      }
    }

    FilterSet fs = new FilterSet();
    for (int i=0; i<numOfFilterConditionLines; ++i) {
      FilterItem item = new FilterItem();
      item.setFilterByOptions(filterByDefinitions);
      if (i<fbdWithDefaults.size()) {
        FilterCompareDefinition fbd = fbdWithDefaults.get(i);
        item.setFilterBy(fbd);
        item.setCompOp(fbd.getDefaultCompOp());
        item.setFilterByValue(fbd.getDefaultFilterByValue());
      }
      fs.addFilterItem(item);
    }

    return fs;
  }
}
