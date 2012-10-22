package org.pm4j.standards.filter;

import java.util.List;

import org.pm4j.common.query.FilterCompareDefinition;

/**
 * Interface for classes that may provide a set of configurable filter-by
 * definitions.<br>
 * E.g. a table may provide its specific filter definitions that can be handled.
 *
 * @author olaf boede
 */
public interface FilterSetProvider {

  /**
   * Provides the set of filter compare definitions that can be
   * specified/modified for this object.<br>
   * Provides an empty collection if there is no filter definition. Never
   * <code>null</code>.
   *
   * @return The set of filter compare definitions.
   */
  List<FilterCompareDefinition> getAvailablePmFilterCompareDefinitions();

  /**
   * Provides the set of currently active filters.
   *
   * @return the current filter set.
   */
  FilterSet getActivePmFilterSet();

  /**
   * Defines a new filter set to apply.
   *
   * @param filterSet the new filter set.
   */
  void setActivePmFilterSet(FilterSet filterSet);

}
