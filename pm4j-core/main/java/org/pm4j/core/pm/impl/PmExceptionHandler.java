package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmCommand;
import org.pm4j.navi.NaviLink;

/**
 * Interface for application specific exception handling.
 *
 * @author olaf boede
 */
public interface PmExceptionHandler {

  /**
   * Will be called whenever the execution of a command failed with an
   * exception.
   *
   * @param failedCommand
   *          The command.
   * @param throwable
   *          The exception thrown during command execution.
   * @param inNaviContext
   *          Indicates if the returned navigation string may be considered by
   *          the framework in the current exception situation.
   *
   * @return A navigation string or <code>null</code> for no special
   *         navigation.
   */
  NaviLink onException(PmCommand failedCommand, Throwable throwable, boolean inNaviContext);

}
