package org.pm4j.core.exception;

import org.pm4j.common.converter.string.StringConverterParseException;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmLocalizeApi;

/**
 * Reports usually a problem occurred while reading data entered by the user.
 *
 * @author oboede
 */
public class PmConverterException extends PmValidationException {

  private static final long serialVersionUID = 7800618523480806486L;

  private StringConverterParseException parseException;

  /**
   * Creates an exception that reports a resource key based message.
   *
   * @param pm The PM context.
   * @param msgKey The message key.
   * @param msgArgs The set of message arguments.
   */
  public PmConverterException(PmObject pm, String msgKey, Object... msgArgs) {
    super(pm, msgKey, msgArgs);
  }

  /**
   * Creates an exception based on a {@link StringConverterParseException}.
   *
   * @param pm The PM context.
   * @param ex The string converter issue to report.
   */
  public PmConverterException(PmObject pm, StringConverterParseException ex) {
    super(pm, PmConstants.MSGKEY_FIRST_MSG_PARAM, getParseExceptionMessage(pm, ex));
    this.parseException = ex;
  }

  /**
   * Reads the converter exception message.
   */
  private static String getParseExceptionMessage(PmObject pm, StringConverterParseException ex) {
    return ex.getMessageString() != null
        ? ex.getMessageString()
        : PmLocalizeApi.localize(pm, PmConstants.MSGKEY_VALIDATION_CONVERSION_FROM_STRING_FAILED, pm);
  }

  /**
   * @return the parseException
   */
  public StringConverterParseException getParseException() {
    return parseException;
  }

}
