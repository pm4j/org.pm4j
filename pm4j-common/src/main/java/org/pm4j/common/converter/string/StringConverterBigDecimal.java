package org.pm4j.common.converter.string;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A number converter for type {@link BigDecimal}.
 *
 * @author dietmar zabel
 */
public class StringConverterBigDecimal extends StringConverterNumber<BigDecimal> {

  public static final StringConverterBigDecimal INSTANCE = new StringConverterBigDecimal();

  public StringConverterBigDecimal() {
    super(BigDecimal.class);
  }

  @Override
  protected NumberFormat getNumberFormat(StringConverterCtxt ctxt, String formatString) {
    NumberFormat nf = super.getNumberFormat(ctxt, formatString);
    if (nf instanceof DecimalFormat) {
      ((DecimalFormat)nf).setParseBigDecimal(true);
    }
    return nf;
  }

}