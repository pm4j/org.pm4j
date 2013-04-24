package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmObject;
import org.pm4j.navi.NaviLink;

/**
 * Interface for application specific exception handling.
 *
 * @author olaf boede
 */
public interface PmExceptionHandler {

  /**
   * Will be called whenever the execution of an action failed with an
   * exception.
   *
   * @param pmObject
   *          On this object the action was executed.
   * @param throwable
   *          The exception thrown during action execution.
   * @param inNaviContext
   *          Indicates if the returned navigation string may be considered by
   *          the framework in the current exception situation.
   *
   * @return A navigation string or <code>null</code> for no special
   *         navigation.
   */
  NaviLink onException(PmObject pmObject, Throwable throwable, boolean inNaviContext);

  /**
   * Gets called if a validation throws a runtime exception.
   * <p>
   * The default implementaion translates the exception into an error message that is related
   * to the PM to validate.
   * <p>
   * Specific implementations may override this method to translate domain specific exceptions
   * to validation messages.
   *
   * @param pmToValidate the PM the validation was called for.
   * @param exception the exception that occured within the validation call.
   */
  void onExceptionInPmValidation(PmObject pmToValidate, RuntimeException exception);

}
