package org.pm4j.common.converter.string;

import java.util.Locale;
import java.util.TimeZone;


/**
 * Converts between typed values and their corresponding string representations.
 *
 * @author Olaf Boede
 *
 * @param <T> The value type.
 */
public interface StringConverter <T> {

  /**
   * Converts a string value the corresponding typed value.
   *
   * @param ctxt Provides context information about formats, {@link Locale} and {@link TimeZone}.
   * @param s The string to convert to a value. May be <code>null</code>.
   * @return The converted value.
   * @throws StringConverterParseException in case of string format mismatches.
   */
  T stringToValue(StringConverterCtxt ctxt, String s) throws StringConverterParseException;

  /**
   * Converts a typed attribute value to its string representation.
   *
   * @param ctxt Provides context information about formats, {@link Locale} and {@link TimeZone}.
   * @param v The value to convert. May be <code>null</code>.
   * @return The generated string representation.
   */
  String valueToString(StringConverterCtxt ctxt, T v);

}
