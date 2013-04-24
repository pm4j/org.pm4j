package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;


/**
 * Attribute constraints.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrBigDecimalCfg {

  /**
   * @return The String representation of the minimum value according to the BigDecimal string representation .
   */
  String minValueString() default "";

  /**
   * @return The String representation of the max value according to the BigDecimal string representation.
   */
  String maxValueString() default "";
 
  /**
   * @return rounding mode when converting to pm value. Changing this will allow
   *         to set more fraction digits than specified in the format. Those
   *         additional digits will then be rounded.
   */
  RoundingMode stringConversionRoundingMode() default RoundingMode.UNNECESSARY;
}
