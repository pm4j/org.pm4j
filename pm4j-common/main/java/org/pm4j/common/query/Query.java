package org.pm4j.common.query;

import java.io.Serializable;

import org.pm4j.common.util.beanproperty.PropertyChangeSupported;

/**
 * Interface for query specifications.
 * <p>
 * This interface is limited to support only some basic filter and sort order defintions.<br>
 * Because of that it can be used for various implementations. E.g. for in-memory or db-based
 * queries.
 *
 * @author olaf boede
 */
public interface Query extends PropertyChangeSupported, Cloneable, Serializable {

  /** Identifier of the property change event that gets fired if the effective sort order gets changed. */
  public static final String PROP_EFFECTIVE_SORT_ORDER = "effectiveSortOrder";

  /** Identifier of the property change event that gets fired if the effective query filter gets changed. */
  public static final String PROP_EFFECTIVE_FILTER = "effectiveFilter";


  /**
   * Sorts the items based on the given {@link SortOrder} definition.
   * <p>
   * <code>null</code> may be passed to switch back to the initial sort order.
   *
   * @param sortOrder
   *          the sort order to use.
   */
  void setSortOrder(SortOrder sortOrder);

  /**
   * Provides the value that was set with {@link #setSortOrder(SortOrder)}.
   *
   * @return the value.
   */
  SortOrder getSortOrder();

  /**
   * Defines the (optional) initial sort order.
   * <p>
   * You may pass <code>null</code> to switch initial sorting off.
   *
   * @param sortOrder
   *          the initial sort order to use.
   */
  void setDefaultSortOrder(SortOrder sortOrder);

  /**
   * Provides the default sort order definition that is usually used if no other sort order was
   * defined by a {@link #setSortSpec(SortOrder)} call.
   * <p>
   * May be <code>null</code> if there is no sort order defined.
   *
   * @return the default sort order.
   */
  SortOrder getDefaultSortOrder();

  /**
   * Provides the currently applied sort order.<br>
   * If no value was set using {@link #setPmSortOrder(SortOrder)}, the current default sort order
   * will be used.<br>
   * May be <code>null</code> if the items are actually not sorted.
   *
   * @return the current sort order.
   */
  SortOrder getEffectiveSortOrder();

  /**
   * Defines a new filter expression to be used.
   *
   * @param expr the new expression. May be <code>null</code> if filtering should be switched off.
   */
  void setFilterExpression(FilterExpression expr);

  /**
   * Provides the current filter expression.
   *
   * @return the current filter expression. May be <code>null</code>.
   */
  FilterExpression getFilterExpression();

  /**
   * Provides the (optional) parameter object for a query the filter is based on.
   *
   * @return the value set by {@link #setBaseQueryParam(Object)}.
   */
  Object getBaseQueryParam(String name);

  /**
   * Sets an optional parameter object and fires a property change event for
   * {@link #PROP_EFFECTIVE_FILTER}.
   * <p>
   * This parameter may contain a simple DTO that contains parameters to be used
   * within the query service. This way the query implementation may combine a
   * manual query implementation with generic code that evaluates the filter
   * conditions.
   * <p>
   * If the query needs to be serialized, the query parameter should be
   * serializeable too.
   *
   * @param baseQueryParam
   *          the new parameter. May be <code>null</code>.
   */
  void setBaseQueryParam(String name, Object baseQueryParam);
}
