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
   * The set of supported injection modes.
   */
  public enum Mode {
    /**
     * The expression within the attriute 'value' gets used. If that's not
     * configured the name of the annotate property will be used as the name
     * of a named object to find.
     */
    EXPRESSION,
    /**
     * A parent PM that has the field type (interface or class) will be serched.
     */
    PARENT_OF_TYPE
  }

  /**
   * @return the injection mode.
   */
  Mode mode() default Mode.EXPRESSION;

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

  /**
   * Defines if <code>null</code> is an accepted value for the injected field.
   * <p>
   * Default value: <code>false</code>.
   */
  boolean nullAllowed() default false;
}
