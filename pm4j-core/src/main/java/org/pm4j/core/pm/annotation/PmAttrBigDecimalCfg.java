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
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrBigDecimalCfg {

  /**
   * @deprecated please use minValue instead.
   * 
   */
  @Deprecated
  String minValueString() default "";

  /**
   * @deprecated please use maxValue instead.
   */
  @Deprecated
  String maxValueString() default "";
 
  
  /**
   * @return The String representation of the maximum value according to the BigDecimal string representation.
   */
  String maxValue() default "";

  
  /**
   * @return The String representation of the minimum value according to the BigDecimal string representation .
   */
  String minValue() default "";
  
  
  /**
   * @deprecated please use roundingMode instead.
   */
  @Deprecated
  RoundingMode stringConversionRoundingMode() default RoundingMode.UNNECESSARY;
  
  
  /**
   * Rounding mode when converting to PM value. Changing this will allow to set
   * more fraction digits than specified in the format. Those additional digits
   * will then be rounded.
   * <p>
   * Default value is {@link PmAttrNumber#ROUNDINGMODE_DEFAULT}.
   * 
   * @return the rounding mode.
   */
  RoundingMode roundingMode() default RoundingMode.UNNECESSARY;
}
