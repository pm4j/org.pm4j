package org.pm4j.core.pm;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PM attribute for {@link BigDecimal} values.
 *
 * @author olaf boede
 */
public interface PmAttrBigDecimal extends PmAttrNumber<BigDecimal> {
  /**
   * @return rounding mode when converting to pm value. Changing this to a value
   *         different than RoundingMode.UNNECESSARY will allow to set more
   *         fraction digits than specified in the format. Those additional
   *         digits will then be rounded.
   */
  RoundingMode getStringConversionRoundingMode();
}