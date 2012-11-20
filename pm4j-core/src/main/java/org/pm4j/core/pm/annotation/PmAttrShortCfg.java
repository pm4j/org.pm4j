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
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmAttrShortCfg {

  /**
   * @return minimum value.
   */
  short minValue() default Short.MIN_VALUE;

  /**
   * @return maximum value.
   */
  short maxValue() default Short.MAX_VALUE;

}
