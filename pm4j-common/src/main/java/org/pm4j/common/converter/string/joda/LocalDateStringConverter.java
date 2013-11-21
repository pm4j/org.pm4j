package org.pm4j.common.converter.string.joda;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

/**
 * Multi format string converter for Joda {@link LocalDate}.
 *
 * @author Olaf Boede
 */
public class LocalDateStringConverter extends JodaStringConverterBase<LocalDate> {

  @Override
  protected LocalDate parseJodaType(DateTimeFormatter fmt, String stringValue) {
    return fmt.parseLocalDate(stringValue);
  }

  @Override
  protected String printJodaType(DateTimeFormatter fmt, LocalDate value) {
    return fmt.print(value);
  }

}
