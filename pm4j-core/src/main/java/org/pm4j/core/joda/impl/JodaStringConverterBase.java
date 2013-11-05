package org.pm4j.core.joda.impl;

import java.text.ParseException;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmWithTimeZone;
import org.pm4j.core.pm.impl.converter.MultiFormatParserBase;
import org.pm4j.core.pm.impl.converter.PmConverterBase;

/**
 * Base class for joda date-time converters, capable to parse multiple input formats.
 *
 * @param T The joda type to support.
 *
 * @author Harm Gnoyke
 * @author oboede
 */
public abstract class JodaStringConverterBase<T> extends PmConverterBase<T> {

  /** A helper instance that supports the multi-format feature. */
  private MultiFormatParserBase<T> multiFormatParser = new MultiFormatParserBase<T>() {
    @Override
    protected T parseValue(String input, String format, Locale locale, PmAttr<?> pmAttr) throws ParseException {
      // Each attribute may provide it's specific time zone by implementing WithPmTimeZone.
      TimeZone tz = (pmAttr instanceof PmWithTimeZone)
          ? ((PmWithTimeZone) pmAttr).getPmTimeZone()
          : pmAttr.getPmConversation().getPmTimeZone();
      DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
      DateTimeFormatter fmt = getDateTimeFormatter(format, locale, dtz);
      try {
        return parseJodaType(fmt, input);
      } catch (IllegalArgumentException e) {
        ParseException pe = new ParseException(e.getMessage(), 0);
        pe.initCause(e);
        throw pe;
      }
    }
  };

  /**
   * Type specific parse implementation.
   *
   * @param fmt The formatter to use.
   * @param stringValue The string to parse. Never <code>null</code>.
   * @return The parse result.
   */
  protected abstract T parseJodaType(DateTimeFormatter fmt, String stringValue);

  /**
   * Type specific to-string operation.
   *
   * @param fmt The formatter. It knows the output format.
   * @param value The value to convert. Never <code>null</code>.
   * @return The formatted string.
   */
  protected abstract String printJodaType(DateTimeFormatter fmt, T value);

  @Override
  public T stringToValue(PmAttr<?> pmAttr, String input) {
    T v = multiFormatParser.parseString(pmAttr, input);
    return v;
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, T v) {
    String outputFormat = multiFormatParser.getOutputFormat(pmAttr);
    PmConversation conversation = pmAttr.getPmConversation();
    Locale locale = conversation.getPmLocale();
    DateTimeFormatter fmt = DateTimeFormat.forPattern(outputFormat).withLocale(locale);
    return printJodaType(fmt, v);
  }

  /**
   * Provides the formatter to use.
   * <p>
   * Sub classes may override this. This can allow to support a different {@link Chronology} etc.
   *
   * @param format The format string.
   * @param locale The locale to use.
   * @param dtz Time zone information to use.
   * @return The corresponding formatter.
   */
  protected DateTimeFormatter getDateTimeFormatter(String format, Locale locale, DateTimeZone dtz) {
    return DateTimeFormat.forPattern(format).withLocale(locale).withZone(dtz);
  }
}
