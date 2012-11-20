package org.pm4j.core.pm.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmResourceData;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.exception.PmUserMessageException;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmMessageUtil;
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


  /* (non-Javadoc)
   * @see org.pm4j.core.pm.impl.PmExceptionHandler#onException(org.pm4j.core.pm.PmObject, java.lang.Throwable, boolean)
   */
  public NaviLink onException(PmObject pmObject, Throwable throwable,
      boolean inNaviContext) {
    if(pmObject instanceof PmCommand) {
      if (throwable instanceof PmUserMessageException) {
        PmResourceData resData = ((PmUserMessageException)throwable).getResourceData();
        if (resData != null) {
          PmMessage message = PmMessageUtil.makeMsg(pmObject, Severity.ERROR, resData.msgKey, resData.msgArgs);

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