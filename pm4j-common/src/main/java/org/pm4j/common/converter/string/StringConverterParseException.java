package org.pm4j.common.converter.string;

import java.util.Arrays;

/**
 * Reports a string parse problem.
 *
 * @author Olaf Boede
 */
public class StringConverterParseException extends Exception {

  private static final long serialVersionUID = 1L;
  private final String messageString;
  private final StringConverterCtxt ctxt;
  private final String stringToParse;
  private final String[] formats;
  private final Throwable cause;

  public StringConverterParseException(String messageString, StringConverterCtxt ctxt, String string, String... formats) {
    this(messageString, ctxt, null, string, formats);
  }

  public StringConverterParseException(String messageString, StringConverterCtxt ctxt, Throwable cause, String string, String... formats) {
    super(messageString != null
        ? messageString
        : "Unable to parse '" + string + "'. Supported formats: + " + Arrays.asList(formats) + " Context: " + ctxt);
    this.messageString = messageString;
    this.ctxt = ctxt;
    this.stringToParse = string;
    this.formats = formats;
    this.cause = cause;
  }

  public StringConverterParseException(StringConverterCtxt ctxt, String string, String... formats) {
    this(null, ctxt, string, formats);
  }

  /**
   * @return the ctxt
   */
  public StringConverterCtxt getCtxt() {
    return ctxt;
  }

  /**
   * @return the string
   */
  public String getStringToParse() {
    return stringToParse;
  }

  /**
   * @return the set of accepted formats
   */
  public String[] getFormats() {
    return formats;
  }

  /**
   * @return the messageString
   */
  public String getMessageString() {
    return messageString;
  }

  /**
   * @return the cause
   */
  public Throwable getCause() {
    return cause;
  }

}
