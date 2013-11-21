package org.pm4j.common.converter.value;

/**
 * The default value converter just passes the instances through.
 *
 * @author Olaf Boede
 */
public class ValueConverterDefault<T> implements ValueConverter<T, T> {

  public static ValueConverterDefault<Object> INSTANCE = new ValueConverterDefault<Object>();

  @Override
  public T toExternalValue(ValueConverterCtxt ctxt, T i) {
    return i;
  }

  @Override
  public T toInternalValue(ValueConverterCtxt ctxt, T e) {
    return e;
  }

}
