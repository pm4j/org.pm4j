package org.pm4j.core.pm;

import org.pm4j.common.query.SortOrder;

/**
 * Indicates a column sort order.
 *
 * @author olaf boede
 *
 * @deprecated new enum: {@link SortOrder}
 */
public enum PmSortOrder {
  /** Ascending sort order. */
  ASC,
  /** Descending sort order. */
  DESC,
  /**
   * Sort order is not defined. Natural sort order or a sort order defined by a
   * parent PM may be applied.
   */
  NEUTRAL
}
