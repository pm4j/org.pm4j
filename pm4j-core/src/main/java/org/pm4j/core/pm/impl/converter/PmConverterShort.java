package org.pm4j.core.pm.impl.converter;

public class PmConverterShort extends PmConverterNumber<Short> {

  public static final PmConverterShort INSTANCE = new PmConverterShort();

  public PmConverterShort() {
    super(Short.class);
  }
}