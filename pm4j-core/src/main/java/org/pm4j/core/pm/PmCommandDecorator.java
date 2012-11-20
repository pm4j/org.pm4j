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

}
