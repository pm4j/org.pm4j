package org.pm4j.common.exception;

/**
 * A simple runtimeexception that is intended to wrap checked exceptions
 * like sax- or io-exception in genereric contexts (iterators, visitors etc.)
 * that should not know about each special businessexception that might
 * be thrown. 
 * <p>
 * The hightest calling code can catch this kind of exceptionwrapper to un-wrap
 * the cause. - That shortens a little bit the stacktrace without removing
 * any relevant information.  
 * 
 * @author Olaf Boede
 */
public class CheckedExceptionWrapper extends RuntimeException {

  /**
   * An id to provide serialization support:
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param cause The checked exception to wrap.
   */
  public CheckedExceptionWrapper(Throwable cause) {
    super(cause.getMessage(), cause);
  }
  
  /**
   * @return The checked exception wrapped by this instance.
   */
  public Throwable getWrappedException() {
    return (Throwable) getCause();
  }

  /**
   * Throws the given exception as a runtimeexception. Wraps it if is not 
   * a {@link RuntimeException}.
   * @param e The exception to throw (and optionally wrap).
   */
  public static void throwAsRuntimeException(Throwable e) {
    if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    }
    else {
      throw new CheckedExceptionWrapper(e);
    }
  }
}
