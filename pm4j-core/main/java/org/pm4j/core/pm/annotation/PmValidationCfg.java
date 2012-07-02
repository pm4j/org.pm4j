package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.impl.PmBeanBase;

/**
 * Validation definition annotation.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PmValidationCfg {

  /**
   * This annotation defines if the JSF-303 validation will be called for the
   * beans that are bound to the PM's. It has usually an effect on the
   * validation execution of classes that inherit from {@link PmBeanBase}.
   *
   * @return <code>TRUE</code> if the subtree should use JSR-303 bean
   *         validations.<br>
   *         <code>FALSE</code> if the subtree should not use JSR-303 bean
   *         validations.<br>
   *         <code>UNDEFINED</code> does not define anything. The definition of
   *         the parent will be used.
   */
  PmBoolean useJavaxValidationForBeans() default PmBoolean.UNDEFINED;

}
