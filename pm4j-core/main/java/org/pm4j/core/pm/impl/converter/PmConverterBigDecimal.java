package org.pm4j.core.pm.impl.converter;

import java.math.BigDecimal;

public class PmConverterBigDecimal extends PmConverterNumber<BigDecimal> {

  public static final PmConverterBigDecimal INSTANCE = new PmConverterBigDecimal();

  public PmConverterBigDecimal() {
    super(BigDecimal.class);
    setDefaultPattern("#0.00");
  }

// TODO: Implement multi format for numbers.
//  @Override
//  public BigDecimal stringToValue(PmAttr<?> pmAttr, String s) {
//    return super.stringToValue2(pmAttr, s);
//  }
//
//  @Override
//  public String valueToString(PmAttr<?> pmAttr, BigDecimal v) {
//    return super.valueToString2(pmAttr, v);
//  }

}