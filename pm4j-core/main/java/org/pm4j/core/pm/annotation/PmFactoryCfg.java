package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration of the bean PM factory to be used in the annotated context.
 * 
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface PmFactoryCfg {

  /**
   * Provides a set of bean presentation models to be used for their associated
   * bean classes. used within this session.
   * <p>
   * If the annotated PM is used in a PM context with an existing
   * {@link PmFactoryCfg}, it may omit presentation model definitions that
   * are provided by the PM parent hierarchy.<br>
   * Definitions of subelements override definitions provided by parent
   * elements.
   * 
   * @return The set of bean PMs to be used here in addition/substitution to the
   *         context PM factory definitions.
   */
  Class<?>[] beanPmClasses();

  /**
   * Parent reference injection is sometimes useful tree structures where 
   * the presentation of the leafs needs some information from the parent objects.
   * 
   * @return Defines if the attribute should inject the element that owns this attribute
   * as parent pm reference.
   */
  boolean injectParentPmRef() default false;
}
