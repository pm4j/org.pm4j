package org.pm4j.common.converter.string;

import org.pm4j.common.converter.value.ValueConverterCtxt;

/**
 * Provides context information needed for several string converter operations.
 *
 * @author Olaf Boede
 */
public interface StringConverterCtxt extends ValueConverterCtxt {

  /**
   * @return The (optional) string format. Needed for numeric and date/time conversions.
   */
  String getConverterCtxtFormatString();

  /**
   * A call back that allows to create context specific error messages.
   *
   * @param valueToConvert The string value that can't be parsed.
   * @param exception An optional exception that reports the technical problem.
   * @param formats The set of accepted formats.
   * @return The generated exception. May have additional context specific information.
   */
  StringConverterParseException createStringConverterParseException(String valueToConvert, Throwable exception, String... formats);

}
