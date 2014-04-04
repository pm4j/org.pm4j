package org.pm4j.common.pageable.querybased;

/**
 * This exception indicates that a method detected that an expected persistent item
 * can't be found in a data store.
 *
 * @author Olaf Boede
 */
public class NoItemForKeyFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message The exception message.
   */
  public NoItemForKeyFoundException(String message) {
    super(message);
  }
}
