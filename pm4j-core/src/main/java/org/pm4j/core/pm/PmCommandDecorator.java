package org.pm4j.core.pm;

/**
 * Interface for command logic that may be added to commands just by adding decorator
 * instances to the command execution logic.
 *
 * @author olaf boede
 */
public interface PmCommandDecorator {

  /**
   * This method will be called before command execution.
   * <p>
   * This method may prevent the execution of the command by returning
   * <code>false</code>.
   *
   * @param cmd
   *          The command that is about to be executed.
   * @return <code>true</code> allows the command to be executed.
   *         <code>false</code> prevents the command logic execution.
   */
  boolean beforeDo(PmCommand cmd);

  /**
   * This method will be called after successful command execution.
   *
   * @param cmd
   *          The command that has been executed.
   */
  void afterDo(PmCommand cmd);

  /**
   * Gets called whenever the command execution fails with an exception.
   */
  public interface WithExceptionHandling extends PmCommandDecorator {

    /**
     * Gets called whenever the execution of <code>doItImpl()</code> fails with
     * an exception.
     * <p>
     * If this method itself throws an exception, the following will be done:
     * <ul>
     *   <li>an info log message for the original exception will be written.</li>
     *   <li>the new exception thrown by this method will be passed to the standard command
     *       exception handling logic.</li>
     *   <li>in case of multiple exception handling decorators, no further exception handling
     *       decorator will be called.</li>
     * </ul>
     * <p>
     * Handling of multiple decorators:
     * <p>
     * If there are muliple exception handling decorators to process, the
     * {@link #onException(PmCommand, Exception)} method will be called for all of them
     * if none throws an own exception.
     * <p>
     * If at least one of them returned <code>false</code>, the default command error handling
     * will be skipped.
     *
     * @param cmd
     *          the command that failed.
     * @param exception
     *          the catched exception.
     * @return <code>true</code> if the standard command exception handling
     *         should be called.<br>
     *         <code>false</code> if the standard command exception handling can
     *         be skipped.
     */
    boolean onException(PmCommand cmd, Exception exception);

  }

}
