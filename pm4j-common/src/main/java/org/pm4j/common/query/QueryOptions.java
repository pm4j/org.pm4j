package org.pm4j.common.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pm4j.common.query.filter.FilterDefinition;


/**
 * Provides the set of query restrictions that can be used to configure a {@link QueryParams}.
 *
 * @author olaf boede
 */
public class QueryOptions {

  // XXX olaf: it would be nice to have the default configurable.
  /** By default a {@link Long} attribute with the name 'id' is used. */
  static final QueryAttr DEFAULT_ID_ATTR = new QueryAttr("id", Long.class);

  private Map<String, SortOrder>        nameToSortOrderMap = new HashMap<String, SortOrder>();
  private SortOrder                     defaultSortOrder;
  private List<FilterDefinition> filterCompareDefinitions = new ArrayList<FilterDefinition>();

  /**
   * An optional definition of the ID attribute used for filters related to item ID's.
   * <p>
   * Is in most cases irrelevant for in-memory queries.<br>
   * Is used in query based collections that define constraints in relation to item ID's.
   */
  private QueryAttr                          idAttribute = DEFAULT_ID_ATTR;

  /** Default constructor. */
  public QueryOptions() {
  }

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

  /**
   * Adds a sort order option.
   *
   * @param name a unique name for the sort order.
   * @param sortOrder the sort order definition.
   */
  public void addSortOrder(String name, SortOrder sortOrder) {
    nameToSortOrderMap.put(name, sortOrder);
  }

  /**
   * Adds a sort order definition for the given attribute.<br>
   * The new entry uses the name of the attribute as key.
   *
   * @param sortByAttr the attribute to be able to sort by.
   */
  public void addSortOrder(QueryAttr sortByAttr) {
    nameToSortOrderMap.put(sortByAttr.getName(), new SortOrder(sortByAttr));
  }

  /**
   * Provides the default sort order.
   *
   * @return the default sort order or <code>null</code> is none is defined.
   */
  public SortOrder getDefaultSortOrder() {
    return defaultSortOrder;
  }

  /**
   * Defines the default sort order to use.
   * @param defaultSortOrder the default sort order. <code>null</code> removes the default sort order.
   */
  public void setDefaultSortOrder(SortOrder defaultSortOrder) {
    this.defaultSortOrder = defaultSortOrder;
  }

  /**
   * Defines the default sort order to use. It will sort by the given attribute(s) in ascending order.
   *
   * @param sortAttrs the attribute(s) to sort by.
   */
  public void setDefaultSortOrder(QueryAttr... sortAttrs) {
    this.defaultSortOrder = new SortOrder(sortAttrs);
  }

  /**
   * Provides the set of available compare definitions.
   * <p>
   * Usually this is used to provide the filter options the user may configure.
   *
   * @return the filter definition. Returns never <code>null</code>.
   */
  public List<FilterDefinition> getCompareDefinitions() {
    return filterCompareDefinitions;
  }

  public void addFilterCompareDefinition(FilterDefinition... definitions) {
    this.filterCompareDefinitions.addAll(Arrays.asList(definitions));
  }

  /**
   * @return the definition of the ID attribute used for filters related to item ID's.
   */
  public QueryAttr getIdAttribute() {
    return idAttribute;
  }

  /**
   * @param idAttribute the definition of the ID attribute used for filters related to item ID's.
   */
  public void setIdAttribute(QueryAttr idAttribute) {
    this.idAttribute = idAttribute;
  }

}
