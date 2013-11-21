package org.pm4j.common.converter.string;


/**
 * String converter for {@link Boolean} values.
 *
 * @author Olaf Boede
 */
public class StringConverterBoolean extends StringConverterBase<Boolean, StringConverterCtxt> {

  public static final StringConverterBoolean INSTANCE = new StringConverterBoolean();

  @Override
  protected Boolean stringToValueImpl(StringConverterCtxt ctxt, String s) {
    return (s != null)
        ? Boolean.valueOf(s)
        : null;
  }

}
