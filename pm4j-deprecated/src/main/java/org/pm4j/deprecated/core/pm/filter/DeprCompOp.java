package org.pm4j.deprecated.core.pm.filter;

import java.util.Collection;

/**
 * The interface for filter compare operator definitions.
 *
 * @author olaf boede
 *
 * @deprecated see {@link org.pm4j.common.query.CompOp}
 */
public interface DeprCompOp {

  enum ValueNeeded { REQUIRED, OPTIONAL, NO }

  /**
   * @return A unique compare operator name.
   */
  String getName();

  /**
   * @return The localized title to display for this operator.
   */
  String getTitle();

  /**
   * @return The value-required definition.
   */
  ValueNeeded getValueNeeded();

  /**
   * For each compare operator a different set of selectable options may apply.
   *
   * @return The set of selectable value options.
   */
  Collection<?> getValueOptions();

  /**
   * Provides a check that filters the given item value based on criteria
   * entered by the user.
   *
   * @param itemValue
   *          The value found in the item (row) to check.
   * @param filterValue
   *          The user defined value to compare to.
   * @return <code>true</code> if the item matches the given filter criteria.
   */
  boolean doesValueMatch(Object itemValue, Object filterValue);

  /**
   * Checks if a filter definition with the given value is a filter condition
   * that needs to be evaluated for each item to filter.
   *
   * @param filterValue
   *          The filter value to check.
   * @return <code>true</code> if a filter with the given values provides a real
   *         filter condition.
   */
  boolean isEffectiveFilterValue(Object filterValue);

}
