package org.pm4j.core.pm.impl.converter;

public class PmConverterLong extends PmConverterNumber<Long> {

  public static final PmConverterLong INSTANCE = new PmConverterLong();

  @Override
  protected String getDefaultNumberFormatPattern() {
    return "#";
  }

  @Override
  protected Long convertParseResultToType(Number parse) {
    return parse.longValue();
  }
}