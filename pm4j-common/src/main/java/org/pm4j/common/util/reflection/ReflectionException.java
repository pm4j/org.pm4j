package org.pm4j.common.util.reflection;

/**
 * A runtime exception that signals reflection problems.  
 */
public class ReflectionException extends RuntimeException {

  /**
   * Serialization id. 
   */
  private static final long serialVersionUID = 1L;

  public ReflectionException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReflectionException(String message) {
    super(message);
  }

  public ReflectionException(Throwable cause) {
    super(cause);
  }

}
