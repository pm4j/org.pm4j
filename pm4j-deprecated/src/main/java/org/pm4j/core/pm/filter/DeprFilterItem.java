package org.pm4j.core.pm.filter;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Provides the filter data entered by the user.
 *
 * @author olaf boede
 */
@Deprecated
public class DeprFilterItem {

  private Collection<? extends DeprFilterByDefinition> filterByOptions = new ArrayList<DeprFilterByDefinition>();
  private DeprFilterByDefinition filterBy;
  private DeprCompOp compOp;
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

  public Collection<? extends DeprFilterByDefinition> getFilterByOptions() { return filterByOptions; }
  public void setFilterByOptions(Collection<? extends DeprFilterByDefinition> filterByOptions) { this.filterByOptions = filterByOptions; }

  public DeprFilterByDefinition getFilterBy() { return filterBy; }
  public void setFilterBy(DeprFilterByDefinition filterItemDefinition) { this.filterBy = filterItemDefinition; }

  public DeprCompOp getCompOp() { return compOp; }
  public void setCompOp(DeprCompOp compOp) { this.compOp = compOp; }

  public Object getFilterByValue() { return filterByValue; }
  public void setFilterByValue(Object filterByValue) { this.filterByValue = filterByValue; }

}
