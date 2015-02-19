package org.pm4j.standards.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.deprecated.core.pm.filter.DeprFilterByDefinition;


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
  public static FilterSet makeFilterSet(Collection<? extends FilterDefinition> filterCompareDefinitions, int numOfFilterConditionLines) {
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
   */
  public static void initializeFilterSetStartingWithDefaultConditions(FilterSet fs, Collection<? extends FilterDefinition> filterByDefinitions, int numOfFilterConditionLines) {
    List<FilterDefinition> fbdWithDefaults = new ArrayList<FilterDefinition>();
    for (FilterDefinition fbd : filterByDefinitions) {
      if (fbd.getDefaultCompOp() != null || fbd.getDefaultFilterByValue() != null) {
        fbdWithDefaults.add(fbd);
      }
    }

    for (int i=0; i<numOfFilterConditionLines; ++i) {
      FilterItem item = new FilterItem();
      item.setFilterByOptions(filterByDefinitions);
      if (i<fbdWithDefaults.size()) {
        FilterDefinition fbd = fbdWithDefaults.get(i);
        item.setFilterBy(fbd);
        item.setCompOp(fbd.getDefaultCompOp());
        item.setFilterByValue(fbd.getDefaultFilterByValue());
      }
      fs.addFilterItem(item);
    }
  }

  /**
   * Fills the givent filter set to have finally the requested
   * <code>numOfFilterConditionLines</code>. It uses the order of the given list
   * of filterByDefinition to generate the various filter items.
   * <p>
   * If the list is shorter than the requested number of lines, the set of
   * definitions will be applied again until the number of lines is filled.
   *
   *
   * @param fs
   *          the filter set to add filters to.
   * @param filterByDefinitions
   *          the set of available user filter definitions.
   * @param numOfFilterConditionLines
   *          the number of filters that should finally exist within the given
   *          filter set.
   */
  public static void fillFilterItems(FilterSet fs, List<FilterDefinition> filterByDefinitions, int numOfFilterConditionLines) {
    int initialFilterSize = fs.getFilterItems().size();
    for (int i=initialFilterSize; i<numOfFilterConditionLines; ++i) {

      FilterItem item = new FilterItem();
      item.setFilterByOptions(filterByDefinitions);

      int fbdIdx = i % filterByDefinitions.size();
      FilterDefinition fbd = filterByDefinitions.get(fbdIdx);
      item.setFilterBy(fbd);
      fs.addFilterItem(item);
    }
  }
}
