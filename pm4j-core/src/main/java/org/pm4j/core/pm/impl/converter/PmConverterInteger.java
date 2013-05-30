package org.pm4j.core.pm.impl.converter;

/**
 * A number converter for type {@link Integer}.
 *
 * @author olaf boede
 */
public class PmConverterInteger extends PmConverterNumber<Integer> {

  public static final PmConverterInteger INSTANCE = new PmConverterInteger();

  public PmConverterInteger() {
    super(Integer.class);
  }

}