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
public class OneWayToStringConverter<T> extends StringConverterBase<T, StringConverterCtxt> {

  @Override
  protected T stringToValueImpl(StringConverterCtxt ctxt, String s) throws Exception {
    throw new Exception("Operation not supported");
  }
}
