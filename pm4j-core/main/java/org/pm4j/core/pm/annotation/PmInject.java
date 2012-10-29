package org.pm4j.core.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pm4j.core.pm.PmObject;

/**
 * Marks a field to be injected by the PM framework.
 * <p>
 * TODO: define and describe protocol and scope specifications.
 *
 * @author olaf boede
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.METHOD })
public @interface PmInject {

  /**
   * A PM-EL expression that provides the content for the annotated field.<br>
   * If the name is not specified, the name of the annotated field will be used
   * to provide the PM-EL expression.
   * <p>
   * Attention: In difference to PM-EL expressions used in
   * {@link PmObject#findPmProperty(String)}, this expression does not
   * address the field itself. This allows to use injected fields that uses the
   * same name as a variable that provides the injection content.
   */
  String value() default "";

  boolean parentByType() default false;

  /**
   * Defines if <code>null</code> is an accepted value for the injected field.
   * <p>
   * Default value: <code>false</code>.
   */
  boolean nullAllowed() default false;
}
