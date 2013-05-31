package org.pm4j.core.pm.impl.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrDouble;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.impl.PmAttrDoubleImpl;

/**
 * A number converter for type {@link Double}.
 *
 * @author olaf boede
 */
public class PmConverterDouble extends PmConverterNumber<Double> {

  public static final PmConverterDouble INSTANCE = new PmConverterDouble();

  public PmConverterDouble() {
    super(Double.class);
  }

  @Override
  protected NumberFormat getNumberFormat(Locale locale, String formatString, PmAttr<?> pmAttr) {
    DecimalFormat decimalFormat = new DecimalFormat(formatString, new DecimalFormatSymbols(locale));
    if(pmAttr instanceof PmAttrDouble) {
      PmAttrDoubleImpl pmAttrDouble = (PmAttrDoubleImpl) pmAttr;
      decimalFormat.setRoundingMode(pmAttrDouble.getRoundingMode());
    } else {
      // Fall back for special attributes sometimes representing a Double value 
      decimalFormat.setRoundingMode(PmAttrNumber.ROUNDINGMODE_DEFAULT);
    }
    return decimalFormat;
  }
}