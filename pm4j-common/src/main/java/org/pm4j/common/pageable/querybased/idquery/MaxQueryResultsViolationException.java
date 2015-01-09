package org.pm4j.common.pageable.querybased.idquery;

/**
 * Reports that a query provides more results than allowed.
 *
 * @author MHELLER
 */
public class MaxQueryResultsViolationException extends RuntimeException {

  /** Serialization id. */
  private static final long serialVersionUID = 1L;

  private final Long foundResults;
  private final long maxResults;

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
    this("The query returns more than " + maxResults + " entries.", maxResults, foundResults);
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
    super(message);

    this.maxResults = maxResults;
    this.foundResults = foundResults;
  }

  /**
   * @return the number of items found query results.<br>
   *         May be <code>null</code> if it's
   *         just known that the number is higher than then
   *         <code>maxResults</code>.
   */
  public Long getFoundResults() {
    return foundResults;
  }

  /**
   * @return maximum number allowed to find by a query.
   */
  public long getMaxResults() {
    return maxResults;
  }

}
