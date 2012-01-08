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
public @interface PmAttrStringCfg {

  static int DEFAULT_MAXLEN = 255;
  
  /**
   * @return maximal string length.
   */
  int maxLen() default DEFAULT_MAXLEN;
  
  /**
   * @return minimal string length.
   */
  int minLen() default 0;
  
  /**
   * @return defines if the input string should be timmed.
   */
  boolean trim() default true;

  /**
   * @return <code>true</code> if the attribute should be represented using a
   *         kind of text area control.
   */
  boolean multiLine() default false;
}
