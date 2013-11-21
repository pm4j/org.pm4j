package org.pm4j.common.converter.string;


/**
 * Converts between typed values theirs corresponding string values.
 *
 * @author Olaf Boede
 *
 * @param <T> The value type.
 */
public interface StringConverter <T> {

  /**
   * Converts the string value representation to the (external) attribute value type.
   *
   * @param pmAttr
   * @param s
   * @return
   * @throws StringConverterParseException
   */
  T stringToValue(StringConverterCtxt ctxt, String s) throws StringConverterParseException;

  /**
   * Converts a typed attribute value to its string representation.
   *
   * @param pmAttr
   * @param v
   * @return
   */
  String valueToString(StringConverterCtxt ctxt, T v);

}
