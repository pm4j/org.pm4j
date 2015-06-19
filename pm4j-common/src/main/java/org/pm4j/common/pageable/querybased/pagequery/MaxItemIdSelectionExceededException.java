package org.pm4j.common.pageable.querybased.pagequery;

/**
 * Reports that an id selection consists of too many ids.  
 * This can happen if the user selects too many items or a large selection is inverted.
 *
 * @author MHOENNIG
 */
public class MaxItemIdSelectionExceededException extends RuntimeException {

  /** Serialization id. */
  private static final long serialVersionUID = 1L;

  private final Long foundResults;
  private final long maxResults;

  /**
   * Provides a message <i>The selection comprises more than ... entries.</i>
   *
   * @param maxResults
   *          The violated item id selection count limit.
   * @param foundResults
   *          The number of items comprised by the selection.<br>
   *          May be <code>null</code> if it's just known that the number is
   *          higher than then <code>maxResults</code>.
   */
  public MaxItemIdSelectionExceededException(long maxResults, Long foundResults) {
    super("The query returns more than " + maxResults + " entries.");
    this.maxResults = maxResults;
    this.foundResults = foundResults;
  }

  /**
   * @return the number of selected items.<br>
   *         May be <code>null</code> if it's
   *         just known that the number is higher than then
   *         <code>maxResults</code>.
   */
  public Long getFoundResults() {
    return foundResults;
  }

  /**
   * @return maximum number of items in a selection.
   */
  public long getMaxResults() {
    return maxResults;
  }

}
