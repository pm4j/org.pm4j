package org.pm4j.core.pm.impl.converter;

import java.text.NumberFormat;
import java.text.ParseException;

import org.pm4j.core.exception.PmResourceRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmConstants;

public class PmConverterDouble extends PmConverterNumber<Double> {

  public static final PmConverterDouble INSTANCE = new PmConverterDouble();

  public PmConverterDouble() {
    super(Double.class);
  }

  @Override
  public Double stringToValue(PmAttr<?> pmAttr, String s) {
    NumberFormat f = getNumberFormat(pmAttr);
    try {
      return f.parse(s).doubleValue();
    } catch (ParseException e) {
      String formatString = pmAttr.getFormatString();
      if (formatString != null) {
        throw new PmResourceRuntimeException(pmAttr, PmConstants.MSGKEY_VALIDATION_FORMAT_FAILURE,
            pmAttr.getPmShortTitle(), formatString, s);
      } else {
        throw new PmResourceRuntimeException(pmAttr, PmConstants.MSGKEY_VALIDATION_CONVERSION_FROM_STRING_FAILED,
            pmAttr.getPmShortTitle(), s);
      }
    }
  }

  @Override
  public String valueToString(PmAttr<?> pmAttr, Double value) {
    NumberFormat f = getNumberFormat(pmAttr);
    return f.format(value.doubleValue());
  }
}