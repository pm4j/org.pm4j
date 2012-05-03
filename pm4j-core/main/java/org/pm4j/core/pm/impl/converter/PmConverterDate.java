package org.pm4j.core.pm.impl.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmResourceRuntimeException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmUtil;

public class PmConverterDate extends PmConverterSerializeableBase<Date> {

  /**
   * The format pattern that is used when no format pattern resource was found
   * for the current PM locale.
   */
  public static final String FALLBACK_FORMAT_PATTERN = "dd.MM.yyyy";

  public static final PmConverterDate INSTANCE = new PmConverterDate();

  private static final Log LOG = LogFactory.getLog(PmConverterDate.class);

  /** The separator string used in case of a multi-format resource string specification. */
  protected String formatSplitString = ";";

  boolean timeZoneAware = true;
  
  @Override
  public Date stringToValue(PmAttr<?> pmAttr, String s) {
    if (StringUtils.isBlank(s)) {
      return null;
    }

    Locale locale = pmAttr.getPmConversation().getPmLocale();
    TimeZone timeZone = getTimeZone(pmAttr);
    for (String format : getParseFormats(pmAttr)) {
      try {
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        sdf.setTimeZone(timeZone);

        // we will not allow dates of the form 00.00.08 etc, hence lenient is set to false
        sdf.setLenient(false);

        return sdf.parse(s);
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

  @Override
  public String valueToString(PmAttr<?> pmAttr, Date value) {
    String outputFormat = getOutputFormat(pmAttr);
    try {
      TimeZone timeZone = getTimeZone(pmAttr);
      return FastDateFormat.getInstance(outputFormat, timeZone, pmAttr.getPmConversation().getPmLocale()).format(value);
    }
    catch (RuntimeException e) {
      throw new PmRuntimeException(pmAttr, "Unable to apply format '" +
                                         outputFormat + "' to value '" + value + "'.");
    }
  }

  private TimeZone getTimeZone(PmAttr<?> pmAttr) {
    return timeZoneAware ? pmAttr.getPmConversation().getPmTimeZone() : TimeZone.getTimeZone("UTC");
  }

  /**
   * Translates the (optionally semicolon delimited) language specific format
   * value in an array set of formats that is used to parse date strings.
   * <p>
   * The last item will be used as string output format.
   * <p>
   * When the language specific format is not defined or empty, the
   * {@link PmConverterDate#FALLBACK_FORMAT_PATTERN} will be returned.
   *
   * @return The accepted input formats.
   */
  public String[] getParseFormats(PmAttr<?> pmAttr) {
    String formatString = StringUtils.defaultIfEmpty(pmAttr.getFormatString(), FALLBACK_FORMAT_PATTERN);
    String[] formats = formatString.split(formatSplitString);
    return formats;
  }

  /**
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
   * @return true, if time zone conversion base on {@link PmConversation#getPmTimeZone()} is activated
   */
  public boolean isTimeZoneAware() {
    return timeZoneAware;
  }

  /**
   * @param timeZoneAware the timeZoneAware to set
   */
  public void setTimeZoneAware(boolean timeZoneAware) {
    this.timeZoneAware = timeZoneAware;
  }
  
  

}