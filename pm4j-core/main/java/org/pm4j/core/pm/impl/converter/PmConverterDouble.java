package org.pm4j.core.pm.impl.converter;


public class PmConverterDouble extends PmConverterNumber<Double> {

  public static final PmConverterDouble INSTANCE = new PmConverterDouble();

  @Override
  protected String getDefaultNumberFormatPattern() {
    return "#.##";
  }

  @Override
  protected Double convertParseResultToType(Number parse) {
    return parse.doubleValue();
  }

}