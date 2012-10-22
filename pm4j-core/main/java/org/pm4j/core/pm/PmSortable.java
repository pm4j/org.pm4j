package org.pm4j.core.pm;

import org.pm4j.common.query.SortOrder;


/**
 * Interface for classes that support sorted items.
 * <p>
 * The method names have a 'Pm' prefix. This is done to prevent name conflicts between
 * framework and business identifiers.
 *
 * @author olaf boede
 */
public interface PmSortable {

  /**
   * Provides the currently applied sort order.<br>
   * If no value was set using {@link #setPmSortOrder(SortOrder)}, the current default sort order
   * will be used.<br>
   * May be <code>null</code> if the items are actually not sorted.
   *
   * @return the current sort order.
   */
  SortOrder getPmEffectiveSortOrder();

  /**
   * Sorts the items based on the given {@link SortOrder} definition.
   * <p>
   * <code>null</code> may be passed to switch back to the initial sort order.
   *
   * @param sortOrder
   *          the sort order to use.
   */
  void setPmSortOrder(SortOrder sortOrder);

  /**
   * Defines the (optional) initial sort order.
   * <p>
   * You may pass <code>null</code> to switch initial sorting off.
   *
   * @param sortOrder
   *          the initial sort order to use.
   */
  void setPmDefaultSortOrder(SortOrder sortOrder);

}
