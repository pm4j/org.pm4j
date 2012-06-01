package org.pm4j.core.pm.impl.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;

public class PmConverterDate extends PmConverterSerializeableBase<Date> {

  /**
   * The format pattern that is used when no format pattern resource was found
   * for the current PM locale.
   */
  public static final String FALLBACK_FORMAT_PATTERN = "dd.MM.yyyy";

  public static final PmConverterDate INSTANCE = new PmConverterDate();

  /** The separator string used in case of a multi-format resource string specification. */
  protected String formatSplitString = ";";

  boolean timeZoneAware = true;

  /**
   * Implementation of converter capable of handling multiple input formats.
   */
  private MultiFormatParserBase<Date> multiFormatParser = new MultiFormatParserBase<Date>() {

    @Override
    protected Date parseValue(String s, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException {
      SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
      sdf.setTimeZone(getTimeZone(pmAttr));

      // we will not allow dates of the form 00.00.08 etc, hence lenient is set to false
      sdf.setLenient(false);

      return sdf.parse(s);
    }

    @Override
    protected String getDefaultFormatPattern() {
      return FALLBACK_FORMAT_PATTERN;
    }
  };

  @Override
  public Date stringToValue(PmAttr<?> pmAttr, String s) {
    return multiFormatParser.parseString(pmAttr, s);
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, Date value) {
    String outputFormat = multiFormatParser.getOutputFormat(pmAttr);
    try {
      TimeZone timeZone = getTimeZone(pmAttr);
      return FastDateFormat.getInstance(outputFormat, timeZone, pmAttr.getPmConversation().getPmLocale()).format(value);
    }
    catch (RuntimeException e) {
      throw new PmRuntimeException(pmAttr, "Unable to apply format '" +
                                         outputFormat + "' to value '" + value + "'.");
    }
  }

  public String getOutputFormat(PmAttr<?> pmAttr) {
    return multiFormatParser.getOutputFormat(pmAttr);
  }

  private TimeZone getTimeZone(PmAttr<?> pmAttr) {
    return timeZoneAware ? pmAttr.getPmConversation().getPmTimeZone() : TimeZone.getTimeZone("UTC");
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