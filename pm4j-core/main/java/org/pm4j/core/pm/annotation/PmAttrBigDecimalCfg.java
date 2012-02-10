package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Attribute constraints.
 *
 * @author olaf boede
 */
// TODO: missing min-max implementation for big decimals.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrBigDecimalCfg {

  /**
   * @return minimum value.
   */
//  String minValueString() default "";

  /**
   * @return maximum value.
   */
//  String maxValueString() default "";

}
