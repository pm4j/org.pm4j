package org.pm4j.core.pm.filter;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;

/**
 * PM interface for a filter item that may be used to define a single filter
 * condition.
 *
 * @author olaf boede
 */
public interface PmFilterItem extends PmBean<FilterItem> {

  /**
   * Provides the PM that allows to specify name of the field or column to compare.<br>
   * The set of selectable fields is provided in the option set of this attribute.
   * <p>
   * The user may select here from a list of business attributes ('Name', 'Street' etc.).
   *
   * @return The filter field selection PM.
   */
  PmAttr<? extends FilterByDefinition> getFilterBy();

  /**
   * Provides the compare operator to be used for the field.
   * The set of selectable operators is provided by the option set of this attribute.
   *
   * @return The compare operator selection PM.
   */
  PmAttr<?> getCompOp();

  /**
   * The PM for compare value specification.
   * <p>
   * Selectable values are provided as options.<br>
   * In most cases the value can be entered just as a simple string.
   * <p>
   * The view may have to check attribute type to identify the matching
   * representation. E.g. a date picker control for date types.
   *
   * @return The value to check against.
   */
  PmAttr<?> getFilterByValue();

}
