package org.pm4j.standards.filter;

import java.util.ArrayList;
import java.util.Collection;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.filter.FilterDefinition;

/**
 * Provides the filter data entered by the user.
 * 
 * @author olaf boede
 */
public class FilterItem {

  private Collection<? extends FilterDefinition> filterByOptions = new ArrayList<FilterDefinition>();
  private FilterDefinition filterBy;
  private CompOp compOp;
  private Object filterByValue;

  /**
   * Returns <code>true</code> if the filter item data express a real filter
   * condition.
   * 
   * @return <code>true</code> if the item can be used to filter items.
   */
  public boolean isEffective() {
    return filterBy != null && compOp != null && compOp.isEffectiveFilterValue(filterByValue);
  }

  // -- getter/setter --

  public Collection<? extends FilterDefinition> getFilterByOptions() {
    return filterByOptions;
  }

  public void setFilterByOptions(Collection<? extends FilterDefinition> filterByOptions) {
    this.filterByOptions = filterByOptions;
  }

  public FilterDefinition getFilterBy() {
    return filterBy;
  }

  public void setFilterBy(FilterDefinition filterItemDefinition) {
    this.filterBy = filterItemDefinition;
  }

  public CompOp getCompOp() {
    return compOp;
  }

  public void setCompOp(CompOp compOp) {
    this.compOp = compOp;
  }

  public Object getFilterByValue() {
    return filterByValue;
  }

  public void setFilterByValue(Object filterByValue) {
    this.filterByValue = filterByValue;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
    sb.append("[").append(filterBy.getAttrTitle()).append(" - ");
    if(getCompOp() != null) {
      sb.append(getCompOp().getName());
    } else {
      sb.append("[no CompOp set]");
    }
    sb.append(" - ");
    if(getFilterByValue() != null) {
      sb.append(getFilterByValue().toString());
    } else {
      sb.append("null");
    }
    sb.append("]");
    return sb.toString();
  }

}
