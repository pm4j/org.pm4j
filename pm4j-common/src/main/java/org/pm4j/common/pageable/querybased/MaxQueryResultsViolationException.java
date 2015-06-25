package org.pm4j.common.pageable.querybased;

/**
 * Reports that a query provides more results than allowed.
 *
 * @author MHOENNIG
 */
// XXX move implementation from deprecated superclass when it's not used anymore
public class MaxQueryResultsViolationException extends org.pm4j.common.pageable.querybased.idquery.MaxQueryResultsViolationException {

  /** Serialization id. */
  private static final long serialVersionUID = 1L;

 /**
   * Provides a message <i>The query returns more than XYZ entries.</i>
   *
   * @param maxResults
   *          The violated result item number limit.
   * @param foundResults
   *          The number of items found query results.<br>
   *          May be <code>null</code> if it's just known that the number is
   *          higher than then <code>maxResults</code>.
   */
  public MaxQueryResultsViolationException(long maxResults, Long foundResults) {
    super(maxResults, foundResults);
  }

  /**
   * @param message
   *          The specific exception message.
   * @param maxResults
   *          The violated result item number limit.
   * @param foundResults
   *          The number of items found query results.<br>
   *          May be <code>null</code> if it's just known that the number is
   *          higher than then <code>maxResults</code>.
   */
  public MaxQueryResultsViolationException(String message, long maxResults, Long foundResults) {
    super(message, maxResults, foundResults);
  }
}
