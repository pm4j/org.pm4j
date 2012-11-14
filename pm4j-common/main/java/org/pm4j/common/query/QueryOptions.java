package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Provides the set of query restrictions that can be used to configure a {@link QueryParams}.
 *
 * @author olaf boede
 */
public class QueryOptions {

  private Map<String, SortOrder> nameToSortOrderMap = new HashMap<String, SortOrder>();
  private SortOrder defaultSortOrder;
  private List<FilterCompareDefinition> filterCompareDefinitions = new ArrayList<FilterCompareDefinition>();

  /**
   * Provides the sort order for the given attribute.
   * <p>
   * Is usually used by a column that asks if it is sortable.
   *
   * @param attrName name
   * @return the corresponding sort order definition or <code>null</code>.
   */
  public SortOrder getSortOrder(String attrName) {
    return nameToSortOrderMap.get(attrName);
  }

  public void addSortOrder(String name, SortOrder sortOrder) {
    nameToSortOrderMap.put(name, sortOrder);
  }

  /**
   * Provides the default sort order.
   *
   * @return the default sort order or <code>null</code> is none is defined.
   */
  public SortOrder getDefaultSortOrder() {
    return defaultSortOrder;
  }

  public void setDefaultSortOrder(SortOrder defaultSortOrder) {
    this.defaultSortOrder = defaultSortOrder;
  }

  /**
   * Provides the set of available compare definitions.
   * <p>
   * Usually this is used to provide the filter options the user may configure.
   *
   * @return the filter definition. Returns never <code>null</code>.
   */
  public List<FilterCompareDefinition> getCompareDefinitions() {
    return filterCompareDefinitions;
  }

  public void addFilterCompareDefinition(FilterCompareDefinition... definitions) {
    this.filterCompareDefinitions.addAll(Arrays.asList(definitions));
  }

}
