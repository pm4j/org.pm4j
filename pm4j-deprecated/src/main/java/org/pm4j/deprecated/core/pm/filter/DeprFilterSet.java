package org.pm4j.deprecated.core.pm.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of filter items that may be used to express combined filter criteria.
 *
 * @deprecated please use {@link org.pm4j.standards.filter.FilterSet}
 * @author olaf boede
 */
@Deprecated
public class DeprFilterSet {

  private List<DeprFilterItem> filterItems = new ArrayList<DeprFilterItem>();
  private DeprCombinedBy combindedBy = DeprCombinedBy.AND;

  public List<DeprFilterItem> getFilterItems() { return filterItems; }
  public void setFilterItems(List<DeprFilterItem> filterItems) { this.filterItems = filterItems; }

  public DeprCombinedBy getCombindedBy() { return combindedBy;  }
  public void setCombindedBy(DeprCombinedBy conditionLogOp) { this.combindedBy = conditionLogOp; }

}
