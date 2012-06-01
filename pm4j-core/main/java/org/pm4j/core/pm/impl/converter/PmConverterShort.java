package org.pm4j.core.pm.impl.converter;

public class PmConverterShort extends PmConverterNumber<Short> {

  public static final PmConverterShort INSTANCE = new PmConverterShort();

  @Override
  protected String getDefaultNumberFormatPattern() {
    return "#";
  }

  @Override
  protected Short convertParseResultToType(Number parse) {
    return parse.shortValue();
  }
}