package org.pm4j.core.pm.filter;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Provides the filter data entered by the user.
 *
 * @author olaf boede
 */
public class FilterItem {

  private Collection<? extends FilterByDefinition> filterByOptions = new ArrayList<FilterByDefinition>();
  private FilterByDefinition filterBy;
  private CompOp compOp;
  private Object filterByValue;

  /**
   * Returns <code>true</code> if the filter item data express a real filter condition.
   *
   * @return <code>true</code> if the item can be used to filter items.
   */
  public boolean isEffective() {
    return  filterBy != null &&
            filterBy.isEffectiveFilterItem(compOp, filterByValue);
  }

  // -- getter/setter --

  public Collection<? extends FilterByDefinition> getFilterByOptions() { return filterByOptions; }
  public void setFilterByOptions(Collection<? extends FilterByDefinition> filterByOptions) { this.filterByOptions = filterByOptions; }

  public FilterByDefinition getFilterBy() { return filterBy; }
  public void setFilterBy(FilterByDefinition filterItemDefinition) { this.filterBy = filterItemDefinition; }

  public CompOp getCompOp() { return compOp; }
  public void setCompOp(CompOp compOp) { this.compOp = compOp; }

  public Object getFilterByValue() { return filterByValue; }
  public void setFilterByValue(Object filterByValue) { this.filterByValue = filterByValue; }

}
