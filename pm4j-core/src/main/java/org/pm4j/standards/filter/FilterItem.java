package org.pm4j.standards.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.FilterCompareDefinition;


/**
 * Provides the filter data entered by the user.
 *
 * @author olaf boede
 */
public class FilterItem {

  private Collection<? extends FilterCompareDefinition> filterByOptions = new ArrayList<FilterCompareDefinition>();
  private FilterCompareDefinition filterBy;
  private CompOp compOp;
  private Object filterByValue;

  /**
   * Returns <code>true</code> if the filter item data express a real filter condition.
   *
   * @return <code>true</code> if the item can be used to filter items.
   */
  public boolean isEffective() {
    return  filterBy != null &&
            compOp != null &&
            compOp.isEffectiveFilterValue(filterByValue);
  }

  // -- getter/setter --

  public Collection<? extends FilterCompareDefinition> getFilterByOptions() { return filterByOptions; }
  public void setFilterByOptions(Collection<? extends FilterCompareDefinition> filterByOptions) { this.filterByOptions = filterByOptions; }

  public FilterCompareDefinition getFilterBy() { return filterBy; }
  public void setFilterBy(FilterCompareDefinition filterItemDefinition) { this.filterBy = filterItemDefinition; }

  public CompOp getCompOp() { return compOp; }
  public void setCompOp(CompOp compOp) { this.compOp = compOp; }

  public Object getFilterByValue() { return filterByValue; }
  public void setFilterByValue(Object filterByValue) { this.filterByValue = filterByValue; }

}
