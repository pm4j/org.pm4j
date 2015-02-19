package org.pm4j.core.pm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmUserMessageException;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmMessageApi;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviRuleLink;

/**
 * A default {@link PmExceptionHandler} implementation.
 * <p>
 * TODO olaf:
 * a) Cleanup {@link #onException(PmObject, Throwable, boolean)} signature.
 * b) Check usage within set and get operations.
 * c) Make it more powerful by adding an exception to handler map.
 *
 * @author olaf boede
 */
public class PmExceptionHandlerImpl implements PmExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PmExceptionHandlerImpl.class);

  private String errorPageNaviString = "to_error_page";

  public PmExceptionHandlerImpl() {
  }

  @Override
  public NaviLink onException(PmObject pmObject, Throwable throwable,
      boolean inNaviContext) {
    if(pmObject instanceof PmCommand) {
      if (throwable instanceof PmUserMessageException) {
        PmResourceData resData = ((PmUserMessageException)throwable).getResourceData();
        if (resData != null) {
          PmMessage message = PmMessageApi.addMessage(pmObject, Severity.ERROR, resData.msgKey, resData.msgArgs);

          if (LOG.isInfoEnabled()) {
            LOG.info("Exception with resource key '" + resData.msgKey +
                      "' was used to create the user error message '" + message.getTitle() + "'.", throwable);
          }

          // no navigation
          return null;
        }
      }

      // No exception with human readable message:
      String ruleString = onNonPmException((PmCommand) pmObject, throwable, inNaviContext);
      return ruleString != null
                ? new NaviRuleLink(ruleString)
                : null;
    }
    // default: rethrow exception
    throw new PmRuntimeException(pmObject, throwable);
  }

  /**
   * The default implementation just throws the exception to the caller.
   * <p>
   * If you have business exceptions that should be considered as validation errors,
   * please override this method to implement your business specific logic.
   */
  @Override
  public void onExceptionInPmValidation(PmObject pmToValidate, RuntimeException exception) {
    throw exception;
  }

  /**
   * This default implementation throws a {@link PmRuntimeException} for the given
   * throwable.
   * <p>
   * Subclasses may provide different behavior.
   *
   * @param failedCommand
   * @param throwable
   * @return
   */
  protected String onNonPmException(PmCommand failedCommand, Throwable throwable, boolean inNaviContext) {
    if (inNaviContext) {
      String exMsg = throwable.getMessage();
      LOG.error("Failed to execute '" + PmUtil.getPmLogString(failedCommand) + "'." +
          (exMsg != null ? (" Error message: " + exMsg) : "")
          , throwable);
      return getErrorPageNaviString();
    }
    else {
      throw PmRuntimeException.asPmRuntimeException(failedCommand, throwable);
    }
  }

  public String getErrorPageNaviString() {
    return errorPageNaviString;
  }

  public void setErrorPageNaviString(String errorPageNaviString) {
    this.errorPageNaviString = errorPageNaviString;
  }

}
