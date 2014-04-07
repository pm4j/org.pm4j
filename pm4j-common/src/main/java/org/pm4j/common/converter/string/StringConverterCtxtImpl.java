package org.pm4j.common.converter.string;

import java.util.Locale;
import java.util.TimeZone;


/**
 * A basic default {@link StringConverterCtxt}.
 *
 * @author Olaf Boede
 */
public class StringConverterCtxtImpl implements StringConverterCtxt {

  private String format;
  private Locale locale;
  private TimeZone timeZone;

  /**
   * Creates a context without any format, {@link Locale} and {@link TimeZone} information.
   */
  public StringConverterCtxtImpl() {
    this(null, null, null);
  }

  /**
   * Creates a context without {@link Locale} and {@link TimeZone} information.
   *
   * @param format The format to use.
   */
  public StringConverterCtxtImpl(String format) {
    this(format, null, null);
  }

  /**
   * Creates a context without {@link TimeZone} information.
   *
   * @param format The format to use.
   * @param locale The {@link Locale} to use.
   */
  public StringConverterCtxtImpl(String format, Locale locale) {
    this(format, locale, null);
  }

  /**
   * @param format The format to use.
   * @param locale The {@link Locale} to use.
   * @param timeZone The {@link TimeZone} to use.
   */
  public StringConverterCtxtImpl(String format, Locale locale, TimeZone timeZone) {
    this.format = format;
    this.locale = locale;
    this.timeZone = timeZone;
  }

  /**
   * Provides the configured {@link TimeZone}.<br>
   * If none is configure it provides the result of {@link TimeZone#getDefault()}.
   */
  @Override
  public TimeZone getConverterCtxtTimeZone() {
    return timeZone != null
            ? timeZone
            : TimeZone.getDefault();
  }

  /**
   * Provides the configured {@link Locale}.<br>
   * If none is configure it provides the result of {@link Locale#getDefault()}.
   */
  @Override
  public Locale getConverterCtxtLocale() {
    return locale != null
            ? locale
            : Locale.getDefault();
  }

  @Override
  public String getConverterCtxtFormatString() {
    return format;
  }

  @Override
  public StringConverterParseException createStringConverterParseException(String valueToConvert, Throwable exception,
      String... formats) {
    return new StringConverterParseException(null, this, exception, valueToConvert, formats);
  }

}
