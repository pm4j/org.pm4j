package org.pm4j.common.converter.value;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Provides context information needed for several string converter operations.
 *
 * @author Olaf Boede
 */
public interface ValueConverterCtxt {

  /**
   * @return The context time zone to consider.
   */
  TimeZone getConverterCtxtTimeZone();

  /**
   * @return The context {@link Locale} to consider.
   */
  Locale getConverterCtxtLocale();

}
