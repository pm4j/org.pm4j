package org.pm4j.common.pageable.querybased.idquery;

public class MaxResultsViolationException extends RuntimeException {

  /**
   * Serialization id.
   */
  private static final long serialVersionUID = 1L;

  public MaxResultsViolationException(String message) {
    super(message);
  }

  public MaxResultsViolationException(String message, Throwable cause) {
    super(message, cause);
  }

  public MaxResultsViolationException(Throwable cause) {
    super(cause);
  }
}
