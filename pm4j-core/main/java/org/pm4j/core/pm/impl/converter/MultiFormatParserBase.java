/**
 *
 */
package org.pm4j.core.pm.impl.converter;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmResourceRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * Base implementation to be used by converters to support parsing of multiple input formats.
 *
 * The input formats will be checked one-by-one, the first matching format will be used.
 *
 * @author Harm Gnoyke
 *
 * @param <T> Type of the value to convert.
 */
public abstract class MultiFormatParserBase<T extends Serializable> {

  private static final Log LOG = LogFactory.getLog(MultiFormatParserBase.class);

  /** The default separator string used in case of a multi-format resource string specification. */
  private static final String DEFAULT_FORMAT_SPLIT_STRING = ";";

  /**
   * Implementation of {@link PmAttr.Converter#stringToValue(PmAttr, String)} to be used to support the subsequent
   * parsing of the input String with multiple input formats.
   *
   * @param pmAttr The attribute
   * @param s The String to parse
   * @return The parsed value
   */
  public T parseString(PmAttr<?> pmAttr, String s) {
    if (StringUtils.isBlank(s)) {
      return null;
    }

    Locale locale = pmAttr.getPmConversation().getPmLocale();
    for (String format : getParseFormats(pmAttr)) {
      try {
        return parseValue(s, format, locale, pmAttr);
      } catch (ParseException e) {
        // ignore it and try the next format.
        if (LOG.isDebugEnabled()) {
          LOG.debug("Format '" + format + "' not applicable for value '" + s +
                    "'. Attribute context: " + PmUtil.getPmLogString(pmAttr));
        }
      }
    }

    // no format match
    throw new PmResourceRuntimeException(pmAttr, PmConstants.MSGKEY_VALIDATION_FORMAT_FAILURE,
                           pmAttr.getPmShortTitle(), getOutputFormat(pmAttr), s);
  }

  /**
   * Try to parse the value in the provided format. If something goes wrong while parsing the values a
   * {@link ParseException} must be thrown by the implementation.
   *
   * @param s The String to parse.
   * @param format The format to be used.
   * @param locale Locale provided by the PmAttr.
   * @param pmAttr The pmAttr currently handled.
   * @return
   * @throws ParseException In case of errors in parsing.
   */
  protected abstract T parseValue(String s, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException;

  /**
   * Default pattern to be used in case no format String is defined.
   * @return Default pattern.
   */
  protected abstract String getDefaultFormatPattern();

  /**
   * Default implementation, may be overridden by implementations:
   * The last format definition returned by {@link #getParseFormats(PmAttr)}.
   * <p>
   * Is used for the method {@link PmAttr#getValueAsString()}.
   * <p>
   * It is also intended to be used by UI help constructs such as calendar
   * popups which provide their data as strings.
   *
   * @return The last item of the result of {@link #getParseFormats(PmAttr)}
   */
  public String getOutputFormat(PmAttr<?> pmAttr) {
    String[] formats = getParseFormats(pmAttr);
    return formats[formats.length-1];
  }

  /**
   * Default format split string, may be overridden by implementations (must be overridden if
   * formats contain the default format split string).
   * Default is a single semicolon.
   * @return The format split string.
   */
  public String getFormatSplitString() {
    return DEFAULT_FORMAT_SPLIT_STRING;
  }

  /**
   * Builds an array of all format strings for the <code>pmAttr</code>.
   * @param pmAttr The pmAttr.
   * @return Format strings.
   */
  public String[] getParseFormats(PmAttr<?> pmAttr) {
    String formatString = StringUtils.defaultIfEmpty(pmAttr != null ? pmAttr.getFormatString() : null, getDefaultFormatPattern());
    String[] formats = formatString.split(getFormatSplitString());
    return formats;
  }

}
