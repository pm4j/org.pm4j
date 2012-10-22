package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryOptionsImpl implements QueryOptions {

  private Map<String, SortOrder> nameToSortOrderMap = new HashMap<String, SortOrder>();
  private SortOrder defaultSortOrder;
  private List<FilterCompareDefinition> filterCompareDefinitions = new ArrayList<FilterCompareDefinition>();

  @Override
  public SortOrder getSortOrder(String attrName) {
    return nameToSortOrderMap.get(attrName);
  }

  public void addSortOrder(String name, SortOrder sortOrder) {
    nameToSortOrderMap.put(name, sortOrder);
  }

  @Override
  public List<FilterCompareDefinition> getCompareDefinitions() {
    return filterCompareDefinitions;
  }

  public void addFilterCompareDefinition(FilterCompareDefinition... definitions) {
    this.filterCompareDefinitions.addAll(Arrays.asList(definitions));
  }

  @Override
  public SortOrder getDefaultSortOrder() {
    return defaultSortOrder;
  }

  public void setDefaultSortOrder(SortOrder defaultSortOrder) {
    this.defaultSortOrder = defaultSortOrder;
  }

}
