package org.pm4j.core.pm;


/**
 * Indicates a column sort order.
 *
 * @author Olaf Boede
 */
public enum PmSortOrder {
  /**
   * Sort order is not defined. Natural sort order or a sort order defined by a
   * parent PM may be applied.
   */
  NEUTRAL("pmSortOrder.NEUTRAL"),
    /** Ascending sort order. */
  ASC("pmSortOrder.ASC"),
  /** Descending sort order. */
  DESC("pmSortOrder.DESC");

  public final String resKey;
  public final String resKeyIcon;
  public final String resKeyIconDisabled;

  private PmSortOrder(String resKey) {
    this.resKey = resKey;
    this.resKeyIcon = resKey + PmConstants.RESKEY_POSTFIX_ICON;
    this.resKeyIconDisabled = resKey + PmConstants.RESKEY_POSTFIX_ICON_DISABLED;
  }
}
