package org.pm4j.core.pm.impl.converter;

public class PmConverterInteger extends PmConverterNumber<Integer> {

  public static final PmConverterInteger INSTANCE = new PmConverterInteger();

  public PmConverterInteger() {
    super(Integer.class);
  }

}