package org.pm4j.standards.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of filter items that is used to express combined filter criteria.
 *
 * @author olaf boede
 */
public class FilterSet {

  private List<FilterItem> filterItems = new ArrayList<FilterItem>();

  public boolean isEmpty() {
    return filterItems.isEmpty();
  }

  public List<FilterItem> getFilterItems() {
    return filterItems;
  }
  public void addFilterItem(FilterItem fi) {
    filterItems.add(fi);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("FilterSet for filters [");
    for (FilterItem filter : getFilterItems()) {
      sb.append("\n\t").append(filter.toString());      
    }
    sb.append("]");
    return sb.toString();
  }
}
