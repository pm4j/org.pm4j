package org.pm4j.common.converter.string;

/**
 * A number converter for type {@link Short}.
 *
 * @author Olaf Boede
 */
public class StringConverterShort extends StringConverterNumber<Short> {

  public static final StringConverterShort INSTANCE = new StringConverterShort();

  public StringConverterShort() {
    super(Short.class);
  }
}