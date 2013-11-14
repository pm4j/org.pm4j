package org.pm4j.common.converter.string;



public interface StringConverter2 <T> {

  /**
   * Converts the string value representation to the (external) attribute value type.
   *
   * @param pmAttr
   * @param s
   * @return
   * @throws PmConverterException
   */
  T stringToValue(StringConverterCtxt ctxt, String s);

  /**
   * Converts a typed attribute value to its string representation.
   *
   * @param pmAttr
   * @param v
   * @return
   */
  String valueToString(StringConverterCtxt ctxt, T v);

}
