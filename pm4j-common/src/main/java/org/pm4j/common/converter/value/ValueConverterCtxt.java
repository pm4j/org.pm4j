package org.pm4j.common.converter.value;

import java.util.Locale;
import java.util.TimeZone;

public interface ValueConverterCtxt {

  TimeZone getTimeZone();

  Locale getLocale();

}
