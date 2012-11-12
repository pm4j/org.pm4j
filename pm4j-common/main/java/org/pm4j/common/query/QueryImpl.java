package org.pm4j.common.query;

import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;

public class QueryImpl extends PropertyChangeSupportedBase implements QueryParams {

  private static final long serialVersionUID = 1L;

  private SortOrder sortOrder;
  private SortOrder defaultSortOrder;
  private FilterExpression filterExpression;
  private Map<String, Object> baseQueryParams = new HashMap<String, Object>();

  /**
   * @param defaultSortOrder the default sort order.
   */
  public QueryImpl(SortOrder defaultSortOrder) {
    this.defaultSortOrder = defaultSortOrder;
  }

  @Override
  public QueryImpl clone() {
    try {
      QueryImpl clone = (QueryImpl)super.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  @Override
  public void setSortOrder(SortOrder sortOrder) {
    SortOrder oldEffectiveOrder = getEffectiveSortOrder();

    this.sortOrder = sortOrder;

    SortOrder newEffectiveOrder = getEffectiveSortOrder();
    firePropertyChange(PROP_EFFECTIVE_SORT_ORDER, oldEffectiveOrder, newEffectiveOrder);
  }

  @Override
  public SortOrder getSortOrder() {
    return sortOrder;
  }

  @Override
  public void setDefaultSortOrder(SortOrder defaultSortOrder) {
    SortOrder oldEffectiveOrder = getEffectiveSortOrder();

    this.defaultSortOrder = defaultSortOrder;

    SortOrder newEffectiveOrder = getEffectiveSortOrder();
    firePropertyChange(PROP_EFFECTIVE_SORT_ORDER, oldEffectiveOrder, newEffectiveOrder);
  }

  @Override
  public SortOrder getDefaultSortOrder() {
    return defaultSortOrder;
  }

  @Override
  public SortOrder getEffectiveSortOrder() {
    return sortOrder != null
        ? sortOrder
        : defaultSortOrder;
  }

  @Override
  public void setFilterExpression(FilterExpression predicate) {
    FilterExpression old = this.filterExpression;
    this.filterExpression = predicate;

    firePropertyChange(PROP_EFFECTIVE_FILTER, old, predicate);
  }

  @Override
  public FilterExpression getFilterExpression() {
    return filterExpression;
  }

  @Override
  public Object getBaseQueryParam(String name) {
    return baseQueryParams.get(name);
  }

  @Override
  public void setBaseQueryParam(String name, Object baseQueryParam) {
    Object oldBaseQueryParam = this.baseQueryParams.get(name);
    this.baseQueryParams.put(name, baseQueryParam);
    firePropertyChange(PROP_EFFECTIVE_FILTER, oldBaseQueryParam, baseQueryParam);
  }

}
