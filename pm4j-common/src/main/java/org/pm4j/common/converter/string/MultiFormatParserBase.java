/**
 *
 */
package org.pm4j.common.converter.string;

import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation to be used by converters to support parsing of multiple input formats.
 * <p>
 * The input formats will be checked one-by-one, the first matching format will be used.
 *
 * @author Harm Gnoyke
 *
 * @param <T> Type of the value to convert.
 */
public abstract class MultiFormatParserBase<T> {

  private static final Logger LOG = LoggerFactory.getLogger(MultiFormatParserBase.class);

  /**
   * Support subsequent parsing of the input String using multiple input formats.
   *
   * @param ctxt Provides parse context information
   * @param s The String to parse
   * @return The parsed value
   * @throws ParseException if no format did match.s
   */
  public T parseString(StringConverterCtxt ctxt, String s) throws ParseException {
    if (StringUtils.isBlank(s)) {
      return null;
    }

    String[] parseFormats = StringConverterUtil.getParseFormats(ctxt);
    ParseException lastFormatParseException = null;

    for (String format : parseFormats) {
      try {
        return parseValue(ctxt, s, format);
      } catch (ParseException e) {
        // ignore it and try the next format.
        lastFormatParseException = e;
        if (LOG.isTraceEnabled()) {
          LOG.trace("Format '" + format + "' not applicable for value '" + s +
                    "'. Context: " +ctxt +
                    "\nCause: " + e.getCause());
        }
      }
    }

    // no format match
    throw (lastFormatParseException != null)
        ? lastFormatParseException
        : new ParseException(s, 0);
  }

  /**
   * Try to parse the value in the provided format. If something goes wrong while parsing the values a
   * {@link ParseException} must be thrown by the implementation.
   *
   * @param s The String to parse.
   * @param format The format to be used.
   * @param ctxt The converter context.
   * @return
   * @throws ParseException In case of errors in parsing.
   */
  protected abstract T parseValue(StringConverterCtxt ctxt, String s, String format) throws ParseException;

}
