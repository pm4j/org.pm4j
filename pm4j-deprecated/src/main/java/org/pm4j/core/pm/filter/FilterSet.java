package org.pm4j.core.pm.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of filter items that may be used to express combined filter criteria.
 *
 * @deprecated please use {@link org.pm4j.standards.filter.FilterSet}
 * @author olaf boede
 */
@Deprecated
public class FilterSet {

  private List<FilterItem> filterItems = new ArrayList<FilterItem>();
  private CombinedBy combindedBy = CombinedBy.AND;

  public List<FilterItem> getFilterItems() { return filterItems; }
  public void setFilterItems(List<FilterItem> filterItems) { this.filterItems = filterItems; }

  public CombinedBy getCombindedBy() { return combindedBy;  }
  public void setCombindedBy(CombinedBy conditionLogOp) { this.combindedBy = conditionLogOp; }

}
