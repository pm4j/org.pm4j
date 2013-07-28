package org.pm4j.core.pm;


/**
 * Indicates a column sort order.
 *
 * @author olaf boede
 *
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
