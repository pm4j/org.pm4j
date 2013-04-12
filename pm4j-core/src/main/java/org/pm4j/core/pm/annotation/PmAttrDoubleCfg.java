package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;

import org.pm4j.core.pm.PmAttrNumber;

/**
 * Attribute constraints.
 * 
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface PmAttrDoubleCfg {

  /**
   * @return minimum value.
   */
  double minValue() default Double.MIN_VALUE;

  /**
   * @return maximum value.
   */
  double maxValue() default Double.MAX_VALUE;

  /**
   * Rounding mode when converting to PM value. Changing this will allow to set
   * more fraction digits than specified in the format. Those additional digits
   * will then be rounded.
   * <p>
   * Default value is {@link PmAttrNumber#ROUNDINGMODE_DEFAULT}.
   * 
   * @return the rounding mode.
   */
  RoundingMode stringConversionRoundingMode() default RoundingMode.UNNECESSARY;
}
