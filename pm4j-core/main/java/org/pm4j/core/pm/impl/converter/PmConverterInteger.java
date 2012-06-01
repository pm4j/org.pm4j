package org.pm4j.core.pm.impl.converter;

public class PmConverterInteger extends PmConverterNumber<Integer> {

  public static final PmConverterInteger INSTANCE = new PmConverterInteger();

  @Override
  protected String getDefaultNumberFormatPattern() {
    return "#";
  }

  @Override
  protected Integer convertParseResultToType(Number parse) {
    return parse.intValue();
  }
}