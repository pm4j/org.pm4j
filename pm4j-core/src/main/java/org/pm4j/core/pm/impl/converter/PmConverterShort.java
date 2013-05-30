package org.pm4j.core.pm.impl.converter;

/**
 * A number converter for type {@link Short}.
 *
 * @author olaf boede
 */
public class PmConverterShort extends PmConverterNumber<Short> {

  public static final PmConverterShort INSTANCE = new PmConverterShort();

  public PmConverterShort() {
    super(Short.class);
  }
}