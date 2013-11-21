package org.pm4j.common.converter.string;

/**
 * A number converter for type {@link Long}.
 *
 * @author Olaf Boede
 */
public class StringConverterLong extends StringConverterNumber<Long> {

  public static final StringConverterLong INSTANCE = new StringConverterLong();

  public StringConverterLong() {
    super(Long.class);
  }
}