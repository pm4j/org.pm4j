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

  /**
   * @param id The key without matching item.
   * @param usedService The service that was used to retrieve the item.
   */
  public NoItemForKeyFoundException(Object id, QueryService<?, ?> usedService) {
    this("No item found for ID: " + id +
        ". It may have been deleted by a concurrent operation." +
        "\n\tUsed query service: " + usedService);
  }
}
