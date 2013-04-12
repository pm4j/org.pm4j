package org.pm4j.core.pm;

import java.math.RoundingMode;

public interface PmAttrDouble extends PmAttrNumber<Double> {
  /**
   * @return rounding mode when converting to pm value. Changing this to a value
   *         different than RoundingMode.UNNECESSARY will allow to set more
   *         fraction digits than specified in the format. Those additional
   *         digits will then be rounded.
   */
  RoundingMode getStringConversionRoundingMode();

}