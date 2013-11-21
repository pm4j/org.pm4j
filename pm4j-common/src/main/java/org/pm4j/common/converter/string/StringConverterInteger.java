package org.pm4j.common.converter.string;

/**
 * A number converter for type {@link Integer}.
 *
 * @author Olaf Boede
 */
public class StringConverterInteger extends StringConverterNumber<Integer> {

  public static final StringConverterInteger INSTANCE = new StringConverterInteger();

  public StringConverterInteger() {
    super(Integer.class);
  }

}