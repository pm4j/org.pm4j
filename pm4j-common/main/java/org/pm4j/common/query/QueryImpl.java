package org.pm4j.common.query;

import org.apache.commons.lang.ObjectUtils;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.util.beanproperty.PropertyChangeSupportedBase;

public class QueryImpl extends PropertyChangeSupportedBase implements Query {

  private static final long serialVersionUID = 1L;

  private SortOrder sortOrder;
  private SortOrder defaultSortOrder;
  private FilterExpression filterExpression;

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
    SortOrder oldOrder = this.sortOrder;
    SortOrder oldEffectiveOrder = getEffectiveSortOrder();

    this.sortOrder = sortOrder;

    if (! ObjectUtils.equals(sortOrder, oldOrder)) {
      firePropertyChange(PROP_DEFAULT_SORT_ORDER, oldOrder, sortOrder);
    }

    SortOrder newEffectiveOrder = getEffectiveSortOrder();
    if (! ObjectUtils.equals(newEffectiveOrder, oldEffectiveOrder)) {
      firePropertyChange(PROP_EFFECTIVE_SORT_ORDER, oldEffectiveOrder, newEffectiveOrder);
    }
  }

  @Override
  public SortOrder getSortOrder() {
    return sortOrder;
  }

  @Override
  public void setDefaultSortOrder(SortOrder defaultSortOrder) {
    SortOrder oldDefaultOrder = this.defaultSortOrder;
    SortOrder oldEffectiveOrder = getEffectiveSortOrder();

    this.defaultSortOrder = defaultSortOrder;

    if (! ObjectUtils.equals(defaultSortOrder, oldDefaultOrder)) {
      firePropertyChange(PROP_DEFAULT_SORT_ORDER, oldDefaultOrder, defaultSortOrder);
    }

    SortOrder newEffectiveOrder = getEffectiveSortOrder();
    if (! ObjectUtils.equals(newEffectiveOrder, oldEffectiveOrder)) {
      firePropertyChange(PROP_EFFECTIVE_SORT_ORDER, oldEffectiveOrder, newEffectiveOrder);
    }
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

    if (! ObjectUtils.equals(old, predicate)) {
      firePropertyChange(PROP_EFFECTIVE_FILTER, old, predicate);
    }
  }

  @Override
  public FilterExpression getFilterExpression() {
    return filterExpression;
  }

}
