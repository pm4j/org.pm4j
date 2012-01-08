package org.pm4j.core.pm.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmUserMessageException;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.navi.NaviLink;
import org.pm4j.navi.NaviRuleLink;

/**
 * TODOC:
 *
 * @author olaf boede
 */
public class PmExceptionHandlerImpl implements PmExceptionHandler {

  private static final Log LOG = LogFactory.getLog(PmExceptionHandlerImpl.class);

  private String errorPageNaviString = "to_error_page";

  public PmExceptionHandlerImpl() {
  }

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
  public NaviLink onException(PmCommand failedCommand, Throwable throwable,
      boolean inNaviContext) {
    if (throwable instanceof PmUserMessageException) {
      PmResourceData resData = ((PmUserMessageException)throwable).getResourceData();
      if (resData != null) {
        PmConversationImpl sessionCtxt = (PmConversationImpl) failedCommand.getPmConversation();
        PmMessage message = new PmMessage(failedCommand, Severity.ERROR, resData.msgKey, resData.msgArgs);
        sessionCtxt.addPmMessage(message);

        if (LOG.isInfoEnabled()) {
          LOG.info("Exception with resource key '" + resData.msgKey +
                    "' was used to create the user error message '" + message.getTitle() + "'.", throwable);
        }

        // no navigation
        return null;
      }
    }

    // No exception with human readable message:
    String ruleString = onNonPmException(failedCommand, throwable, inNaviContext);
    return ruleString != null
              ? new NaviRuleLink(ruleString)
              : null;
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