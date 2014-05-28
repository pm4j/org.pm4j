package org.pm4j.common.converter.string.joda;

import java.text.ParseException;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pm4j.common.converter.string.MultiFormatParserBase;
import org.pm4j.common.converter.string.StringConverterBase;
import org.pm4j.common.converter.string.StringConverterCtxt;
import org.pm4j.common.converter.string.StringConverterUtil;

/**
 * Base class for joda date-time converters, capable to parse multiple input formats.
 *
 * @param T The joda type to support.
 *
 * @author Harm Gnoyke
 * @author Olaf Boede
 */
public abstract class JodaStringConverterBase<T> extends StringConverterBase<T, StringConverterCtxt> {

  /** A helper instance that supports the multi-format feature. */
  private MultiFormatParserBase<T> multiFormatParser = new MultiFormatParserBase<T>() {
    @Override
    protected T parseValue(StringConverterCtxt ctxt, String input, String format) throws ParseException {
      Locale locale = ctxt.getConverterCtxtLocale();
      DateTimeZone dtz = DateTimeZone.forTimeZone(ctxt.getConverterCtxtTimeZone());
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
  protected T stringToValueImpl(StringConverterCtxt ctxt, String input) throws Exception {
    return (input != null)
        ? multiFormatParser.parseString(ctxt, input)
        : null;
  }

  @Override
  protected String valueToStringImpl(StringConverterCtxt ctxt, T v) {
    if (v == null) {
      return null;
    }
    String outputFormat = StringConverterUtil.getOutputFormat(ctxt);
    Locale locale = ctxt.getConverterCtxtLocale();
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
