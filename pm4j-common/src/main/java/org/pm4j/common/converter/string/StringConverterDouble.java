package org.pm4j.common.converter.string;


/**
 * A number converter for type {@link Double}.
 *
 * @author Olaf Boede
 */
public class StringConverterDouble extends StringConverterNumber<Double> {

  public static final StringConverterDouble INSTANCE = new StringConverterDouble();

  public StringConverterDouble() {
    super(Double.class);
  }
}