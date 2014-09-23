package org.pm4j.common.converter.value;

import java.util.Locale;
import java.util.TimeZone;

/**
 * A default converter context that holds configuration data as local fields.
 *
 * @author Olaf Boede
 */
public class ValueConverterCtxtImpl implements ValueConverterCtxt {

  private TimeZone timeZone;
  private Locale locale;

  public ValueConverterCtxtImpl() {
    this(Locale.getDefault());
  }

  public ValueConverterCtxtImpl(Locale locale) {
    this(TimeZone.getDefault(), locale);
  }

  public ValueConverterCtxtImpl(TimeZone timeZone) {
    this(timeZone, Locale.getDefault());
  }

  public ValueConverterCtxtImpl(TimeZone timeZone, Locale locale) {
    this.timeZone = timeZone;
    this.locale = locale;
  }

  @Override
  public TimeZone getConverterCtxtTimeZone() {
    return timeZone;
  }

  @Override
  public Locale getConverterCtxtLocale() {
    return locale;
  }

  public void setConverterCtxtTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  public void setConverterCtxtLocale(Locale locale) {
    this.locale = locale;
  }

}
