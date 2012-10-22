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

  public static final String PROP_EFFECTIVE_SORT_ORDER = "effectiveSortOrder";
  public static final String PROP_DEFAULT_SORT_ORDER = "defaultSortOrder";
  public static final String PROP_SORT_ORDER = "sortOrder";

  public static final String PROP_EFFECTIVE_FILTER = "effectiveFilter";
  public static final String PROP_FIX_FILTER = "fixFilter";
  public static final String PROP_DYN_FILTER = "dynFilter";


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

  void setFilterExpression(FilterExpression expr);

  FilterExpression getFilterExpression();

}
