package org.pm4j.standards.filter;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.filter.CombinedBy;

/**
 * A set of filter items that is used to express combined filter criteria.
 *
 * @author olaf boede
 */
public class FilterSet {

  private List<FilterItem> filterItems = new ArrayList<FilterItem>();
  private CombinedBy combinedBy = CombinedBy.AND;

  public boolean isEmpty() {
    return filterItems.isEmpty();
  }

  public List<FilterItem> getFilterItems() {
    return filterItems;
  }
  public void addFilterItem(FilterItem fi) {
    filterItems.add(fi);
  }

  public CombinedBy getCombinedBy() {
    return combinedBy;
  }
  public void setCombinedBy(CombinedBy conditionLogOp) {
    this.combinedBy = conditionLogOp;
  }

}
