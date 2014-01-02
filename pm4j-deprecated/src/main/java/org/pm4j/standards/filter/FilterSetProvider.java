package org.pm4j.standards.filter;

import java.util.List;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.filter.FilterDefinition;
import org.pm4j.core.pm.PmAttr;

/**
 * Interface for classes that may provide a set of configurable filter-by
 * definitions.<br>
 * E.g. a table may provide its specific filter definitions that can be handled.
 *
 * @author olaf boede
 */
public interface FilterSetProvider<T_FILTERSET_BEAN extends FilterSet> {

  /**
   * Provides the set of filter compare definitions that can be
   * specified/modified for this object.<br>
   * Provides an empty collection if there is no filter definition. Never
   * <code>null</code>.
   *
   * @return The set of filter compare definitions.
   */
  List<FilterDefinition> getAvailablePmFilterCompareDefinitions();

  /**
   * Provides the set of currently active filters.
   *
   * @return the current filter set.
   */
  T_FILTERSET_BEAN getActivePmFilterSet();

  /**
   * Defines a new filter set to apply.
   *
   * @param filterSet the new filter set.
   */
  void setActivePmFilterSet(T_FILTERSET_BEAN filterSet);


 /**
  * Provides a corresponding pm attribute to a bean type.   
  */
  interface FilterByValuePmAttrFactory {

    /**
     * Creates a filter attribute type specific PM for entering the attribute value.
     * <p>
     * The concrete attribute PM provides:
     * <ul>
     * <li>the type specific attribute provides the string conversion</li>
     * <li>validations and</li>
     * <li>options</li>
     * </ul>
     * 
     * @param parentPm
     *            the FilterItemPm
     * @param fd
     *            the filter-by field definition.
     * @param co
     *            the selected compare operator.
     * @return the corresponding attribute PM.
     */
    PmAttr<?> makeValueAttrPm(FilterItemPm<?> parentPm, FilterDefinition fd, CompOp co);

    /**
     * Sets enum options to restrict enum lists for special types. 
     * @param type the type for the enum list.
     * @param enums the enums for this type.
     */
    <T extends Enum<?>> void setEnumOptions(Class<T> type, List<T> enums);
    
    /**
     * Sets enum options to restrict enum lists for special types. 
     * @param type the type for the enum list.
     * @param enums the enums for this type.
     */
    <T extends Enum<?>> void setEnumOptions(Class<T> type, T... enums);
  }

  /**
   * Used to override the default FilterByFalue pm creation factory.
   * @param factory
   */
  void setFilterByValuePmAttrFactory(FilterByValuePmAttrFactory factory);
  
  /**
   * Returns the FilterByFalue pm creation factory.
   * @return the FilterByFalue pm creation factory.
   */
  FilterByValuePmAttrFactory getFilterByValuePmAttrFactory();
}
