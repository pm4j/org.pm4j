package org.pm4j.common.pageable.querybased.idquery;

public class MaxQueryResultsViolationException extends RuntimeException {
  
  /**
   * Serialization id.
   */
  private static final long serialVersionUID = 1L;
  
  private long foundResults;

  private long maxResults;

  public MaxQueryResultsViolationException(String message, long maxResults, long foundResults) {
    super(message);
    
    init(maxResults, foundResults);
  }

  public MaxQueryResultsViolationException(String message, Throwable cause, long maxResults, long foundResults) {
    super(message, cause);
    
    init(maxResults, foundResults);
  }

  public MaxQueryResultsViolationException(Throwable cause, long maxResults, long foundResults) {
    super(cause);
    
    init(maxResults, foundResults);
  }
  
  /**
   * @return number if items found by a query.
   */
  public long getFoundResults() {
    return foundResults;
  }

  /**
   * @return maximum number allowed to find by a query.
   */
  public long getMaxResults() {
    return maxResults;
  }

  private void init(long maxResults, long foundResults) {
    this.maxResults = maxResults;
    this.foundResults = foundResults;
  }
}
