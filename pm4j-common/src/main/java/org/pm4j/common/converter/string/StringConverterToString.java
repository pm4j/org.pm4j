package org.pm4j.common.converter.string;

/**
 * One-way T-to-String converter.
 *
 * Used as default itemConverter of generic PmAttrListImpl
 *
 * This converter does not support conversion from String to value of type T
 *
 * @param <T>
 *          Type of the value to convert.
 *
 * @author jhetmans
 */
public class StringConverterToString<T> extends StringConverterBase<T, StringConverterCtxt> {

  public static final StringConverterToString<Object> INSTANCE = new StringConverterToString<Object>();

  @Override
  protected T stringToValueImpl(StringConverterCtxt ctxt, String s) throws Exception {
    throw new Exception("Operation not supported");
  }
}
