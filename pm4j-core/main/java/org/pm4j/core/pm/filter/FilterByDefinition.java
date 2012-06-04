package org.pm4j.core.pm.filter;

import java.util.Collection;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmObject;

/**
 * Provides the meta data set for the UI of a filter item.<br>
 * It provides the options the user can define for a filter item.
 *
 * @author olaf boede
 */
public interface FilterByDefinition {

  /**
   * @return A name that is unique within a set of filter definitions.
   */
  String getName();

  /**
   * @return The localized title to display for this filter-by definition.
   */
  String getTitle();

  /**
   * @return The set of compare operators that can be applied.
   */
  Collection<CompOp> getCompOps();

  /**
   * @return Type of the value to enter.
   */
  Class<?> getValueType();

  /**
   * Defines if this filter filters the beans behind the item PMs or directly the item PMs.
   * <p>
   * An example: In a UserTablePm we have rows of type UserPm and behind these rows beans of type UserBean.<br>
   * If we define a filter that expects the UserBeanPm to be passed to {@link #doesItemMatch(Object, CompOp, Object)}
   * ...
   * TODO olaf: Check if it is really needed.
   *
   * @return
   */
  boolean isBeanFilter();

  /**
   * Checks if a filter definition with the given values is a filter condition
   * that needs to be evaluated for each item to filter.
   * <p>
   * Usually a filter definition without compare operator shouldn't be
   * considered. Specific filter definitions may have additional conditions.
   *
   * @param compOp
   *          The compare operator to check.
   * @param filterValue
   *          The filter value to check.
   * @return <code>true</code> if a filter with the given values provides a real
   *         filter condition.
   */
  boolean isEffectiveFilterItem(CompOp compOp, Object filterValue);

  /**
   * Provides a check that filters the given item based on criteria entered by
   * the user.
   *
   * @param item
   *          The item (row) to check.
   * @param compOp
   *          The user defined compare operator for the check.
   * @param filterValue
   *          The user defined value to compare to.
   * @return <code>true</code> if the item matches the given filter criteria.
   */
  boolean doesItemMatch(Object item, CompOp compOp, Object filterValue);

  /**
   * Creates a value type specific attribute.
   *
   * @return An attribute that supports the field- and condition-specific value data entry logic.
   */
  public abstract PmAttr<?> makeValueAttrPm(PmObject parentPm);

}
