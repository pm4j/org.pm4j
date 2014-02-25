package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;

/**
 * Validation definition annotation.
 * <p>
 * XXX is very rudimentary. Needs to be extended to control validation groups.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PmValidationCfg {

  /**
   * This annotation defines if the JSR-303 validation, defined for the whole bean,
   * will be called for the beans that are bound to the PM's. It has an effect on the
   * validation execution of classes that inherit from {@link PmBean}.
   * <p>
   * Please notice that JSR-303 validations defined for fields are not affected by this
   * definition. They are always considered for each field that is handled by an enabled
   * {@link PmAttr}.
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
