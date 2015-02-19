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
public @interface PmAttrLongCfg {

  /**
   * @return minimum value.
   */
  long minValue() default Long.MIN_VALUE;
  
  /**
   * @return maximum value.
   */
  long maxValue() default Long.MAX_VALUE;
  
}
