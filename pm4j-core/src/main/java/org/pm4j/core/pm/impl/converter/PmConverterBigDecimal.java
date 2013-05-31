package org.pm4j.core.pm.impl.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrBigDecimal;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.impl.PmAttrBigDecimalImpl;

/**
 * A number converter for type {@link BigDecimal}.
 *
 * @author dietmar zabel
 */
public class PmConverterBigDecimal extends PmConverterNumber<BigDecimal> {

  public static final PmConverterBigDecimal INSTANCE = new PmConverterBigDecimal();

  public PmConverterBigDecimal() {
    super(BigDecimal.class);
  }

  @Override
  protected NumberFormat getNumberFormat(Locale locale, String formatString, PmAttr<?> pmAttr) {
    DecimalFormat decimalFormat = new DecimalFormat(formatString, new DecimalFormatSymbols(locale));
    if(pmAttr instanceof PmAttrBigDecimal) {
      PmAttrBigDecimalImpl pmAttrBigDecimal = (PmAttrBigDecimalImpl) pmAttr;
      decimalFormat.setRoundingMode(pmAttrBigDecimal.getRoundingMode());
    } else {
      // Fall back for special attributes sometimes representing a BigDecimal value 
      decimalFormat.setRoundingMode(PmAttrNumber.ROUNDINGMODE_DEFAULT);
    }
    
    decimalFormat.setParseBigDecimal(true);
    return decimalFormat;
  }

}