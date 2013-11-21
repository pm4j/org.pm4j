package org.pm4j.common.converter.string.joda;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * Multi format string converter for Joda {@link LocalDateTime}.
 *
 * @author Olaf Boede
 */
public class LocalDateTimeStringConverter extends JodaStringConverterBase<LocalDateTime> {
  @Override
  protected LocalDateTime parseJodaType(DateTimeFormatter fmt, String stringValue) {
    return fmt.parseLocalDateTime(stringValue);
  }

  @Override
  protected String printJodaType(DateTimeFormatter fmt, LocalDateTime value) {
    return fmt.print(value);
  }
}
