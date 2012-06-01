package org.pm4j.core.pm.impl.converter;

import java.math.BigDecimal;

public class PmConverterBigDecimal extends PmConverterNumber<BigDecimal> {

  public static final PmConverterBigDecimal INSTANCE = new PmConverterBigDecimal();

  @Override
  protected String getDefaultNumberFormatPattern() {
    return "#.###";
  }

  @Override
  protected BigDecimal convertParseResultToType(Number parse) {
    return (BigDecimal) parse;
  }

}