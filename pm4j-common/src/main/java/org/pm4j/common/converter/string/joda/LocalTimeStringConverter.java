package org.pm4j.common.converter.string.joda;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * Multi format string converter for Joda {@link LocalTime}.
 *
 * @author Olaf Boede
 */
public class LocalTimeStringConverter extends JodaStringConverterBase<LocalTime> {
  @Override
  protected LocalTime parseJodaType(DateTimeFormatter fmt, String stringValue) {
    return fmt.parseLocalTime(stringValue);
  }

  @Override
  protected String printJodaType(DateTimeFormatter fmt, LocalTime value) {
    return fmt.print(value);
  }
}

